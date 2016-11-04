package domains;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import examples.chimp.TestRACEDomain;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import unify.CompoundSymbolicVariableConstraintSolver;

public class JUnitTestRACEDomain {
	
	private String[][] symbols;
	private int[] ingredients;
	private HybridDomain domain;

	@Before
	public void setUp() throws Exception {
		try {
			domain = new HybridDomain("domains/ordered_domain.ddl");
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		ingredients = new int[] {1, domain.getMaxArgs()};
		symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
	}
	
	public void testPlanning(String problemPath) {
		ProblemParser pp = new ProblemParser(problemPath);
		
		symbols[1] = pp.getArgumentSymbols();
		Map<String, String[]> typesInstancesMap = pp.getTypesInstancesMap();
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		try {
			TestRACEDomain.initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		assertTrue(planner.backtrack());
		
		TestRACEDomain.extractPlan(fluentSolver);
	}

	@Test
	public void testOpTuckArms() {
		testPlanning("problems/test_op_tuck_arms.pdl");
	}
	
	@Test
	public void testOpMoveBase() {
		testPlanning("problems/test_op_move_base.pdl");
	}
	
	@Test
	public void testOpMoveTorso() {
		testPlanning("problems/test_op_move_torso.pdl");
	}

	@Test
	public void testOpMoveBaseBlind() {
		testPlanning("problems/test_op_move_base_blind.pdl");
	}
	
	@Test
	public void testOpPickUpObject() {
		testPlanning("problems/test_op_pick_up_object.pdl");
	}
	
	@Test
	public void testOpPlaceObject() {
		testPlanning("problems/test_op_place_object.pdl");
	}
		
	@Test
	public void testOpMoveArmToSide() {
		testPlanning("problems/test_op_move_arm_to_side.pdl");
	}
			
	@Test
	public void testMoveArmsToCarryposture() {
		testPlanning("problems/test_op_move_arms_to_carryposture.pdl");
	}
				
	@Test
	public void testOpObserveObjectsOnArea() {
		testPlanning("problems/test_op_observe_objects_on_area.pdl");
	}

}
