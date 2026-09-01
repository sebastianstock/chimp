package postprocessing;

import java.io.*;
import java.util.*;

import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.fluent.FluentStoreImpl;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentSignature;
import chimp.model.FluentStore;
import chimp.model.FluentType;
import chimp.model.FluentView;
import chimp.model.TemporalInterval;

public class PlanExtractor {
	
	private final FluentStore fluentStore;
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public PlanExtractor(FluentNetworkSolver fluentSolver) {
		this.fluentStore = new FluentStoreImpl(fluentSolver);
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
		for (FluentView action : getActions()) {
			writer.write(formatActionToROSPlan(action));
			writer.append(LINE_SEPARATOR);
		}
	}

	public void writeActions(Writer writer) {
		for (FluentView action : getActions()) {
			try {
				activityToYaml(action, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private List<FluentView> getActions() {
		List<FluentView> actions = new ArrayList<>(fluentStore.fluents(FluentType.ACTIVITY));
		actions.sort(Comparator.comparingLong(action -> action.temporalInterval().earliestStart()));
		return actions;
	}

	/**
	 * Formats the action to ROSPlans action format:
	 * EST: (TASKNAME ARG1 ... ARGN) [Duration]
	 * @param action
	 * @return
	 */
	private String formatActionToROSPlan(FluentView action) {
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

	private void activityToYaml(FluentView activity, Writer w) throws IOException {
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
		List<FluentView> plannedFluents = filterPlannedFluents();
		
		List<FluentView> rootTasks = filterRootTasks(plannedFluents);

		System.out.println("#######  PLAN ######");
		for (FluentView root : rootTasks) {
			printDecomposition(root, "");
		}
		
	}	
	
	public void printAllFluents() {
		for (FluentView fluent : fluentStore.fluents()) {
			System.out.println(formatFluent(fluent));
		}
	}
	
	private void printDecomposition(FluentView task, String indentation) {
		System.out.println(indentation + formatFluent(task));
		
		String nextIndentation = indentation + "  ";
		
		List<FluentView> subtasks = new ArrayList<>();
		for (ChimpConstraint decomposition
				: fluentStore.outgoing(task.id(), ConstraintKind.DECOMPOSITION)) {
			subtasks.add(requireFluent(decomposition.to()));
		}
		
		Collections.sort(subtasks, new Comparator<FluentView>() {
			@Override
			public int compare(final FluentView obj1, final FluentView obj2) {
				if (hasBefore(obj1.id(), obj2.id())) {
					return -1;
				} else if (hasBefore(obj2.id(), obj1.id())) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		for (FluentView sub : subtasks) {
			printDecomposition(sub, nextIndentation);
		}
		
		for (int i = 0; i < subtasks.size(); i++) {
			FluentView from = subtasks.get(i);
			for (int j = i+1; j < subtasks.size(); j++) {
				FluentView to = subtasks.get(j);
				if (hasBefore(from.id(), to.id())) {
					StringBuilder sb = new StringBuilder();
					sb.append(nextIndentation);
					sb.append("Before: ");
					sb.append(from.id().value());
					sb.append(" --> ");
					sb.append(to.id().value());
					System.out.println(sb.toString());
				}
			}
		}
		
	}
	
	private List<FluentView> filterPlannedFluents() {
		List<FluentView> ret = new ArrayList<>();
		ret.addAll(fluentStore.fluents(FluentType.ACTIVITY));
		ret.addAll(fluentStore.fluents(FluentType.TASK));
		return ret;
	}
	
	private List<FluentView> filterRootTasks(List<FluentView> tasks) {
		List<FluentView> ret = new ArrayList<>();
		for (FluentView task : tasks) {
			if (fluentStore.incoming(task.id(), ConstraintKind.DECOMPOSITION).isEmpty()) {
				ret.add(task);
			}
		}
		return ret;
	}
	
	private boolean hasBefore(FluentId from, FluentId to) {
		for (ChimpConstraint before : fluentStore.outgoing(from, ConstraintKind.BEFORE)) {
			if (before.to().equals(to)) {
				return true;
			}
		}
		return false;
	}

	private FluentView requireFluent(FluentId id) {
		return fluentStore.fluent(id)
				.orElseThrow(() -> new IllegalStateException(
						"Constraint references unknown fluent ID: " + id.value()));
	}

	private String formatFluent(FluentView fluent) {
		return fluent.toString();
	}

	
}
