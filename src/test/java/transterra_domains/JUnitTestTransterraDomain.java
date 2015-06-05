package transterra_domains;

import static org.junit.Assert.assertTrue;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.TestProblemParsing;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import transterra.TestTransTerraProblems;
import unify.CompoundSymbolicVariableConstraintSolver;

public class JUnitTestTransterraDomain {
	
	private static String[][] symbols;
	private static int[] ingredients;
	private static Map<String, String[]> typesInstancesMap;
	
	private HTNPlanner planner;
	private FluentNetworkSolver fluentSolver;
	private HybridDomain domain;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		symbols = TestTransTerraProblems.createSymbols();
		ingredients = TestTransTerraProblems.createIngredients();
		typesInstancesMap = TestTransTerraProblems.createTypesInstances();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);

		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		domain = TestProblemParsing.initPlanner(planner, "domains/transterra_v1.ddl");
	}

	@After
	public void tearDown() throws Exception {}
	
	private void testProblem(String problemPath) {
		ProblemParser pp = new ProblemParser(problemPath);
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		planner.createInitialMeetsFutureConstraints();
		assertTrue(planner.backtrack());
		TestProblemParsing.extractPlan(fluentSolver);
	}
	

	@Test
	public void testOpMoveTo() {
		testProblem("problems/transterra_problems_v1/test_op_move_to.pdl");
	}

	@Test
	public void testOpSample() {
		testProblem("problems/transterra_problems_v1/test_op_sample_regolith.pdl");
	}

	@Test
	public void testOpTransferSample() {
		testProblem("problems/transterra_problems_v1/test_op_transfer_sample.pdl");
	}

	@Test
	public void testOpTransferPayload() {
		testProblem("problems/transterra_problems_v1/test_op_transfer_payload.pdl");
	}

	@Test
	public void testOpPickupBasecamp() {
		testProblem("problems/transterra_problems_v1/test_op_pickup_basecamp.pdl");
	}

	@Test
	public void testOpPickupBasecamp2() {
		testProblem("problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl");
	}

	@Test
	public void testOpPlaceBasecamp() {
		testProblem("problems/transterra_problems_v1/test_op_place_basecamp.pdl");
	}

	@Test
	public void testMDeployBasecamp1() {
		testProblem("problems/transterra_problems_v1/test_m_deploy_basecamp_1.pdl");
	}

	@Test
	public void testMDeployBasecamp2() {
		testProblem("problems/transterra_problems_v1/test_m_deploy_basecamp_2.pdl");
	}

	@Test
	public void testMTakeSamples1() {
		testProblem("problems/transterra_problems_v1/test_m_take_samples_1.pdl");
	}

	@Test
	public void testMTakeSamples2() {
		testProblem("problems/transterra_problems_v1/test_m_take_samples_2.pdl");
	}

	@Test
	public void testMGetBasecamp1() {
		testProblem("problems/transterra_problems_v1/test_m_get_basecamp_1.pdl");
	}

	@Test
	public void testMGetBasecamp2() {
		testProblem("problems/transterra_problems_v1/test_m_get_basecamp_2.pdl");
	}
	
	@Test
	public void testMTransferFilled() {
		testProblem("problems/transterra_problems_v1/test_m_transfer_filled.pdl");
	}
}
