package domains;

import static org.junit.Assert.assertTrue;
import hybridDomainParsing.ProblemParser;
import unify.CompoundSymbolicVariableConstraintSolver;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;

public class JUnitTestRACEDomain {
	
	private static String[][] symbols;
	private static int[] ingredients;
	private static final Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
	
	private PFD0Planner planner;
	private FluentNetworkSolver fluentSolver;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		symbols = RACEProblemsSingle.createSymbols();
		ingredients = RACEProblemsSingle.createIngredients();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);

		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		TestProblemParsing.initPlanner(planner, "domains/race_domain.ddl");
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testOpTuckArms() {
		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
	
	@Test
	public void testOpMoveBase() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}

	@Test
	public void testOpMoveBaseBlind() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
	
	@Test
	public void testOpPickUpObject() {
		ProblemParser pp = new ProblemParser("problems/test_op_pick_up_object.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
	
		@Test
	public void testOpPlaceObject() {
		ProblemParser pp = new ProblemParser("problems/test_op_place_object.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
		
			@Test
	public void testOpMoveArmToSide() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_arm_to_side.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
			
				@Test
	public void testMoveArmsToCarryposture() {
		ProblemParser pp = new ProblemParser("problems/test_op_move_arms_to_carryposture.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}
				
					@Test
	public void testOpObserveObjectsOnArea() {
		ProblemParser pp = new ProblemParser("problems/test_op_observe_objects_on_area.pdl");
		pp.createState(fluentSolver);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
	}


}
