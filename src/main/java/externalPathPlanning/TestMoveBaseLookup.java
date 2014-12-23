package externalPathPlanning;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.PFD0Planner;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestMoveBaseLookup {
	

	public static void main(String[] args) {
		
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
		
		String[][] symbols = TestProblemParsing.createSymbols();
		int[] ingredients = TestProblemParsing.createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		PFD0Planner planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		TestProblemParsing.initPlanner(planner, "domains/race_domain.ddl");
		
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
		TestProblemParsing.plan(planner, fluentSolver);
		TestProblemParsing.extractPlan(fluentSolver);
	}


}
