package sensing;

import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import tmpDispatching.DummyInferenceCallback;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestFluentDispatching {

	public static void main(String[] args) {
		
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");

		
		String[][] symbols = TestProblemParsing.createSymbols();
		int[] ingredients = TestProblemParsing.createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		HTNPlanner planner = new HTNPlanner(0,  600,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		TestProblemParsing.initPlanner(planner, "domains/race_domain.ddl");
		
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
		
		plan(planner, fluentSolver);
		
		TestProblemParsing.extractPlan(fluentSolver);
		
		System.out.println("Starting Dispatching");
		
		DummyInferenceCallback cb = new DummyInferenceCallback(planner);  // does nothing in doInference()
		FluentConstraintNetworkAnimator animator = new FluentConstraintNetworkAnimator(fluentSolver, 1000, cb);
	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		return result;
	}


}
