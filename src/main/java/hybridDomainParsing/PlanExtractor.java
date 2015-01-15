package hybridDomainParsing;

import htn.TaskApplicationMetaConstraint.markings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class PlanExtractor {
	
	private final FluentNetworkSolver fluentSolver;
	
	private ListMultimap<Fluent, FluentConstraint> fluentsOutgoingDCsMultiMap;
	private Table<Fluent, Fluent, FluentConstraint> beforesTable;

	public PlanExtractor(FluentNetworkSolver fluentSolver) {
		this.fluentSolver = fluentSolver;
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
	
	private void printDecomposition(Fluent task, String indentation) {
		System.out.println(indentation + task);
		
		String nextIndentation = indentation + "  ";
		
		List<FluentConstraint> dcs = fluentsOutgoingDCsMultiMap.get(task);
		List<Fluent> subtasks = new ArrayList<Fluent>();
		for (FluentConstraint dc : dcs) {
			subtasks.add((Fluent) dc.getTo());
		}
		
		Collections.sort(subtasks, new Comparator<Fluent>() {
			public int compare(final Fluent obj1, final Fluent obj2) {
				if (beforesTable.contains((Fluent) obj1, (Fluent) obj2)) {
					return -1;
				} else if (beforesTable.contains((Fluent) obj2, (Fluent) obj1)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		for (Fluent sub : subtasks) {
			printDecomposition((Fluent) sub, nextIndentation);
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
