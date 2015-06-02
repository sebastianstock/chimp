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

public class JUnitTestRACEDomainMethods {
	
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
	public void testMAdaptTorso() {
		testProblem("problems/test_m_adapt_torso.pdl");
	}
	
	@Test
	public void testMTorsoAssumeDrivingPose0() {
		testProblem("problems/test_m_torso_assume_driving_pose0.pdl");
	}

	@Test
	public void testMTorsoAssumeDrivingPose1() {
		testProblem("problems/test_m_torso_assume_driving_pose1.pdl");
	}

	@Test
	public void testMAdaptArms0() {
		testProblem("problems/test_m_adapt_arms_0.pdl");
	}

	@Test
	public void testMAdaptArms1() {
		testProblem("problems/test_m_adapt_arms_1.pdl");
	}

	@Test
	public void testMAdaptArms2() {
		testProblem("problems/test_m_adapt_arms_2.pdl");
	}

	@Test
	public void testMArmsAssumeDrivingPose0() {
		testProblem("problems/test_m_arms_assume_driving_pose0.pdl");
	}

	@Test
	public void testMArmsAssumeDrivingPose1() {
		testProblem("problems/test_m_arms_assume_driving_pose1.pdl");
	}
	
	@Test
	public void testMDriveRobot0() {
		testProblem("problems/test_m_drive_robot_0.pdl");
	}

	@Test
	public void testMDriveRobot1() {
		testProblem("problems/test_m_drive_robot_1.pdl");
	}

	@Test
	public void testMDriveRobot2() {
		testProblem("problems/test_m_drive_robot_2.pdl");
	}
	
	@Test
	public void testMMoveBothArmsToSide1() {
		testProblem("problems/test_m_move_both_arms_to_side_1.pdl");
	}

	@Test
	public void testMMoveBothArmsToSide2() {
		testProblem("problems/test_m_move_both_arms_to_side_2.pdl");
	}

	@Test
	public void testMMoveBothArmsToSide3() {
		testProblem("problems/test_m_move_both_arms_to_side_3.pdl");
	}

	@Test
	public void testMMoveBothArmsToSide4() {
		testProblem("problems/test_m_move_both_arms_to_side_4.pdl");
	}

	@Test
	public void testMMoveBothArmsToSide5() {
		testProblem("problems/test_m_move_both_arms_to_side_5.pdl");
	}
	
	@Test
	public void testMAssumeManipulationPose1() {
		testProblem("problems/test_m_assume_manipulation_pose_1.pdl");
	}

	@Test
	public void testMAssumeManipulationPose2() {
		testProblem("problems/test_m_assume_manipulation_pose_2.pdl");
	}

	@Test
	public void testMAssumeManipulationPose3() {
		testProblem("problems/test_m_assume_manipulation_pose_3.pdl");
	}
	
	@Test
	public void testMLeaveManipulationPose1() {
		testProblem("problems/test_m_leave_manipulation_pose_1.pdl");
	}
	
	@Test
	public void testMGraspObjectWArm1() {
		testProblem("problems/test_m_grasp_object_w_arm_1.pdl");
	}

	@Test
	public void testMGetObjectWArm2() {
		testProblem("problems/test_m_get_object_w_arm_2.pdl");
	}

	@Test
	public void testMGetObjectWArm3() {
		testProblem("problems/test_m_get_object_w_arm_3.pdl");
	}
	
	@Test
	public void testMPutObject1() {
		testProblem("problems/test_m_put_object_1.pdl");
	}
	
	@Test
	public void testMPutObject1a() {
		testProblem("problems/test_m_put_object_1a.pdl");
	}
	
	@Test
	public void testMPutObject2() {
		testProblem("problems/test_m_put_object_2.pdl");
	}
	
	@Test
	public void testMPutObject3() {
		testProblem("problems/test_m_put_object_3.pdl");
	}

	@Test
	public void testMMoveObject1() {
		testProblem("problems/test_m_move_object_1.pdl");
	}

	@Test
	public void testMMoveObject2() {
		testProblem("problems/test_m_move_object_2.pdl");
	}

	@Test
	public void testMMoveObject3() {
		testProblem("problems/test_m_move_object_3.pdl");
	}

	@Test
	public void testScenario_3_2_3() {
		testProblem("problems/test_scenario_3_2_3.pdl");
	}
	
	
	private void testProblem(String problemPath) {
		ProblemParser pp = new ProblemParser(problemPath);
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}


}
