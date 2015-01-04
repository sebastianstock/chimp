package sensing;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicVariableConstraintSolver;
import dispatching.FluentDispatchingFunction;

public class TestFluentDispatching {

	public static void main(String[] args) {
		
		// init planner
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");

		String[][] symbols = TestProblemParsing.createSymbols();
		int[] ingredients = TestProblemParsing.createIngredients();
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
//		final long origin = Calendar.getInstance().getTimeInMillis();
		final long origin = 0L;

		HTNPlanner planner = new HTNPlanner(origin, origin + 100000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fns = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		TestProblemParsing.initPlanner(planner, "domains/race_domain.ddl");
		pp.createState(fns);
		
		((CompoundSymbolicVariableConstraintSolver) fns.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
			
		plan(planner, fns);
		
		TestProblemParsing.extractPlan(fns);
		
		// Dispatch the plan
		System.out.println("Starting Dispatching");
		
		FluentConstraintNetworkAnimator animator = new FluentConstraintNetworkAnimator(fns, 1000);
		
		final Vector<Fluent> executingActs = new Vector<Fluent>();
		
		FluentDispatchingFunction df = new FluentDispatchingFunction("Activity") {
			
			@Override
			public boolean skip(Fluent act) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void dispatch(Fluent act) {
				executingActs.addElement(act);
				System.out.println("This is a call to ROS to do " + act);
			}
		};
		
		animator.addDispatchingFunctions(fns, df);
		
		
	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

//		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		return result;
	}


}
