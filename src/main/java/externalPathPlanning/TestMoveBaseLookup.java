package externalPathPlanning;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.metacsp.utility.logging.MetaCSPLogging;

import examples.TestRACEDomain;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestMoveBaseLookup {
	

	public static void main(String[] args) {
		
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
		
		String[][] symbols = TestRACEDomain.createSymbols();
		int[] ingredients = TestRACEDomain.createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		HybridDomain domain = TestRACEDomain.initPlanner(planner, "domains/race_domain.ddl");
		
		pp.createState(fluentSolver, domain);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
		planner.createInitialMeetsFutureConstraints();
		TestRACEDomain.plan(planner, fluentSolver);
		TestRACEDomain.extractPlan(fluentSolver);
	}


}
