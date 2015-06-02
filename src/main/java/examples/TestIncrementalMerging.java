package examples;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.TaskApplicationMetaConstraint.markings;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicVariableConstraintSolver;

public class TestIncrementalMerging {
	

	public static void main(String[] args) {
		
		// IROS
		ProblemParser pp = new ProblemParser("problems/iros/incrementa_merging.pdl");
		
		String[][] symbols = TestProblemParsing.createSymbols();
		int[] ingredients = TestProblemParsing.createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		HybridDomain domain = TestProblemParsing.initPlanner(planner, "domains/ordered_domain.ddl");

		pp.createState(fluentSolver, domain);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
//		MetaCSPLogging.setLevel(Level.INFO);
		
		plan(planner, fluentSolver);
		
		// Add another task
		String name = "move_object(milkPot1 placingAreaNorthLeftTable2)";
		String component;
		if (name.startsWith("!")) {
			component = "Activity";
		} else {
			component = "Task";
		}
		Variable var = fluentSolver.createVariable(component);
		((Fluent) var).setName(name);
		var.setMarking(markings.UNPLANNED);
		
//		planner.clearResolvers();
		plan(planner, fluentSolver);
		
		printActivities(fluentSolver);
		
//		TestProblemParsing.extractPlan(fluentSolver);
		
		////////////////
		
	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		
//		ConstraintNetwork cn = new ConstraintNetwork(null);
//		for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
//			if (con instanceof FluentConstraint) {
//				FluentConstraint fc = (FluentConstraint) con;
//				if (fc.getType() == FluentConstraint.Type.MATCHES) {
//					fc.getFrom().setMarking(markings.UNIFIED);
//					cn.addConstraint(fc);
//				} else if (fc.getType() == FluentConstraint.Type.DC) {
//					cn.addConstraint(fc);
//				}
//			}
//		}
//		ConstraintNetwork.draw(cn);
//		
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

//		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		
//		System.out.println("AGAIN");
//		startTime = System.nanoTime();
//		result = planner.backtrack();
//		System.out.println("Found a plan? " + result);
//		endTime = System.nanoTime();
//
//		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//		System.out.println("Finished");
		
		return result;
	}


	private static void printActivities(FluentNetworkSolver fluentSolver) {
		Variable[] acts = fluentSolver.getVariables();
		ArrayList<Variable> plan = new ArrayList<Variable>();
		for (Variable var : acts) {
			if (var.getComponent() == null) {
				plan.add(var);
			}
			else if (var.getComponent().equals("Activity")) {
				plan.add(var);
			}
		}
		Variable[] planVector = plan.toArray(new Variable[plan.size()]);
		Arrays.sort(planVector, new Comparator<Variable>() {
			@Override
			public int compare(Variable o1, Variable o2) {
				Fluent f1 = (Fluent)o1;
				Fluent f2 = (Fluent)o2;
				return ((int)f1.getTemporalVariable().getEST()-(int)f2.getTemporalVariable().getEST());
			}
		});
		
		int c = 0;
		for (Variable act : planVector) {
			System.out.println(c++ +".\t" + act);
		}
	}
	
}
