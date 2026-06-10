package postprocessing;

import java.io.*;
import java.util.*;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

import chimp.adapter.metacsp.MetaCspModelAdapter;
import chimp.model.FluentNode;
import chimp.model.FluentSignature;
import chimp.model.FluentType;
import chimp.model.PlanSnapshot;
import chimp.model.TemporalInterval;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class PlanExtractor {
	
	private final FluentNetworkSolver fluentSolver;
	private final PlanSnapshot planSnapshot;
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private ListMultimap<Fluent, FluentConstraint> fluentsOutgoingDCsMultiMap;
	private Table<Fluent, Fluent, FluentConstraint> beforesTable;

	public PlanExtractor(FluentNetworkSolver fluentSolver) {
		this.fluentSolver = fluentSolver;
		this.planSnapshot = new MetaCspModelAdapter().toPlanSnapshot(fluentSolver);
	}

	public void printActions() {
		Writer out
		   = new BufferedWriter(new OutputStreamWriter(System.out));
		writeActions(out);
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writePlanToFile(boolean result, File outputFile) {
		if (outputFile != null) {
			Writer fw = null;
			try {
				fw = new FileWriter(outputFile);
				if (result) {
					fw.write("; Solution found");
					fw.write(LINE_SEPARATOR);
					writeROSPlanFormat(fw);
				} else {
					fw.write("; Could not find a solution");
				}
				fw.flush();
			}
			catch ( IOException e ) {
				System.err.println( "Could not create file" );
			}
			finally {
				if ( fw != null )
					try { fw.close(); } catch ( IOException e ) { e.printStackTrace(); }
			}
		}
	}

	public void writeROSPlanFormat(Writer writer) throws IOException {
		writer.write("; Actions:");
		writer.append(LINE_SEPARATOR);
		List<FluentNode> actions = getActions();
		for (FluentNode action : actions) {
			writer.write(formatActionToROSPlan(action));
			writer.append(LINE_SEPARATOR);
		}
	}

	public void writeActions(Writer writer) {
		for (FluentNode action : getActions()) {
			try {
				activityToYaml(action, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private List<FluentNode> getActions() {
		List<FluentNode> actions = new ArrayList<>(planSnapshot.fluents(FluentType.ACTIVITY));
		actions.sort(Comparator.comparingLong(action -> action.temporalInterval().earliestStart()));
		return actions;
	}

	/**
	 * Formats the action to ROSPlans action format:
	 * EST: (TASKNAME ARG1 ... ARGN) [Duration]
	 * @param action
	 * @return
	 */
	private String formatActionToROSPlan(FluentNode action) {
		TemporalInterval interval = action.temporalInterval();
		double estSeconds = ((double) interval.earliestStart()) / 1000;
		double minDurationSeconds = ((double) (interval.earliestEnd() - interval.earliestStart())) / 1000;

		StringBuilder sb = new StringBuilder();
		sb.append(estSeconds);
		sb.append(": (");
		FluentSignature signature = action.signature();
		sb.append(signature.predicate()).append(" ");
		sb.append(String.join(" ", signature.arguments()));
		sb.append(") [");
		sb.append(String.format(Locale.ROOT,"%.3f", minDurationSeconds));
		sb.append("]");
		return sb.toString();
	}

	private void activityToYaml(FluentNode activity, Writer w) throws IOException {
		FluentSignature signature = activity.signature();

		w.write("- id: " + activity.id().value());
		w.append(LINE_SEPARATOR);
		
		w.write("  name: \"" + signature.predicate() + '"');
		w.append(LINE_SEPARATOR);
		StringBuilder argsbuilder = new StringBuilder("  arguments: [");
		List<String> arguments = signature.arguments();
		if (!arguments.isEmpty()) {
			argsbuilder.append('"');
			argsbuilder.append(arguments.get(0));
			argsbuilder.append('"');
		}
		for (int i = 1; i < arguments.size(); i++) {
			argsbuilder.append(", \"");
			argsbuilder.append(arguments.get(i));
			argsbuilder.append('"');
		}
		argsbuilder.append(']');
		w.write(argsbuilder.toString());
		w.append(LINE_SEPARATOR);
		TemporalInterval interval = activity.temporalInterval();
		w.write("  time: [[" + interval.earliestStart() + ", " + interval.latestStart()
				+ "], [" + interval.earliestEnd() + ", " + interval.latestEnd() + "]]");
		w.append(LINE_SEPARATOR);
	}
	
	/** 
	 * Prints the current plan on the command line.
	 */
	public void printPlan() {
		List<Fluent> plannedFluents = filterPlannedFluents();
		
		List<Fluent> rootTasks = filterRootTasks(plannedFluents);
		
		createOutgoingDCMultiMap();

		System.out.println("#######  PLAN ######");
		for (Fluent root : rootTasks) {
			printDecomposition(root, "");
		}
		
	}	
	
	public void printAllFluents() {
		for (Variable var : fluentSolver.getVariables()) {
//			ret.add((Fluent) var);
			System.out.println(var);
		}
	}
	
	private void printDecomposition(Fluent task, String indentation) {
		System.out.println(indentation + task);
		
		String nextIndentation = indentation + "  ";
		
		List<FluentConstraint> dcs = fluentsOutgoingDCsMultiMap.get(task);
		List<Fluent> subtasks = new ArrayList<Fluent>();
		for (FluentConstraint dc : dcs) {
			subtasks.add((Fluent) dc.getTo());
		}
		
		Collections.sort(subtasks, new Comparator<Fluent>() {
			@Override
			public int compare(final Fluent obj1, final Fluent obj2) {
				if (beforesTable.contains(obj1, obj2)) {
					return -1;
				} else if (beforesTable.contains(obj2, obj1)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		for (Fluent sub : subtasks) {
			printDecomposition(sub, nextIndentation);
		}
		
		for (int i = 0; i < subtasks.size(); i++) {
			Fluent from = subtasks.get(i);
			for (int j = i+1; j < subtasks.size(); j++) {
				Fluent to = subtasks.get(j);
				if (beforesTable.contains(from, to)) {
					StringBuilder sb = new StringBuilder();
					sb.append(nextIndentation);
					sb.append("Before: ");
					sb.append(from.getID());
					sb.append(" --> ");
					sb.append(to.getID());
					System.out.println(sb.toString());
				}
			}
		}
		
	}
	
	private List<Fluent> filterPlannedFluents() {
		ArrayList<Fluent> ret = new ArrayList<Fluent>();
		for (Variable var : fluentSolver.getVariables("Activity")) {
			ret.add((Fluent) var);
		}
		for (Variable var : fluentSolver.getVariables("Task")) {
			ret.add((Fluent) var);
		}
		
		return ret;
	}
	
	private List<Fluent> filterRootTasks(List<Fluent> tasks) {
		ArrayList<Fluent> ret = new ArrayList<Fluent>();
		SetMultimap<Fluent, FluentConstraint> fluentsIncomingDCsMultiMap = HashMultimap.create();
		
		for (Constraint con : fluentSolver.getConstraints()) {
			if (con instanceof FluentConstraint) {
				FluentConstraint.Type type = ((FluentConstraint) con).getType();
				if (type.equals(FluentConstraint.Type.DC)) {
					fluentsIncomingDCsMultiMap.put((Fluent) ((FluentConstraint) con).getTo(), 
							(FluentConstraint) con); 
				}
			}
		}
		
		for (Fluent t : tasks) {
			if (! fluentsIncomingDCsMultiMap.containsKey(t)) {
				ret.add(t);
			}
		}
		
		return ret;
	}
	
	private void createOutgoingDCMultiMap() {
		fluentsOutgoingDCsMultiMap = ArrayListMultimap.create();
		beforesTable = HashBasedTable.create();
		
		for (Constraint con : fluentSolver.getConstraints()) {
			if (con instanceof FluentConstraint) {
				Fluent from = (Fluent) ((FluentConstraint) con).getFrom();
				Fluent to = (Fluent) ((FluentConstraint) con).getTo();
				FluentConstraint.Type type = ((FluentConstraint) con).getType();
				if (type.equals(FluentConstraint.Type.DC)) {
					fluentsOutgoingDCsMultiMap.put(from, (FluentConstraint) con); 
				}
				if (type.equals(FluentConstraint.Type.BEFORE)) {
					beforesTable.put(from, to, (FluentConstraint) con);
				}
			}
		}
	}

	
}
