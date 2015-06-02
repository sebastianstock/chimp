package domains;

import static org.junit.Assert.assertTrue;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import unify.CompoundSymbolicVariableConstraintSolver;

public class JUnitTestRACEDomain {
	
	private static String[][] symbols;
	private static int[] ingredients;
	private static final Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
	
	private HTNPlanner planner;
	private FluentNetworkSolver fluentSolver;
	private HybridDomain domain;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		symbols = TestProblemParsing.createSymbols();
		ingredients = TestProblemParsing.createIngredients();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);

		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		domain = TestProblemParsing.initPlanner(planner, "domains/ordered_domain.ddl");
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testOpTuckArms() {
		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
	
	@Test
	public void testOpMoveBase() {
		fluentSolver.deplenish();
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
	
	@Test
	public void testOpMoveTorso() {
		fluentSolver.deplenish();
		ProblemParser pp = new ProblemParser("problems/test_op_move_torso.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}

	@Test
	public void testOpMoveBaseBlind() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
	
	@Test
	public void testOpPickUpObject() {
		ProblemParser pp = new ProblemParser("problems/test_op_pick_up_object.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
	
		@Test
	public void testOpPlaceObject() {
		ProblemParser pp = new ProblemParser("problems/test_op_place_object.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
		
			@Test
	public void testOpMoveArmToSide() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_arm_to_side.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
			
				@Test
	public void testMoveArmsToCarryposture() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_arms_to_carryposture.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
				
					@Test
	public void testOpObserveObjectsOnArea() {
		ProblemParser pp = new ProblemParser("problems/test_op_observe_objects_on_area.pdl");
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}


}
