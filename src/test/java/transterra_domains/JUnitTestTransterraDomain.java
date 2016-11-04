package transterra_domains;

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

public class JUnitTestTransterraDomain {
	
	private String[][] symbols;
	private int[] ingredients;
	private HybridDomain domain;

	@Before
	public void setUp() throws Exception {
		try {
			domain = new HybridDomain("domains/transterra_v1.ddl");
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		ingredients = new int[] {1, domain.getMaxArgs()};
		symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
	}
	
	private void testPlanning(String problemPath) {
		ProblemParser pp = new ProblemParser(problemPath);
		
		symbols[1] = pp.getArgumentSymbols();
		Map<String, String[]> typesInstancesMap = pp.getTypesInstancesMap();
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		
		try {
			TestRACEDomain.initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		planner.createInitialMeetsFutureConstraints();

		assertTrue(planner.backtrack());
		
		TestRACEDomain.extractPlan(fluentSolver);
	}
	

	@Test
	public void testOpMoveTo() {
		testPlanning("problems/transterra_problems_v1/test_op_move_to.pdl");
	}

	@Test
	public void testOpSample() {
		testPlanning("problems/transterra_problems_v1/test_op_sample_regolith.pdl");
	}

	@Test
	public void testOpTransferSample() {
		testPlanning("problems/transterra_problems_v1/test_op_transfer_sample.pdl");
	}

	@Test
	public void testOpTransferPayload() {
		testPlanning("problems/transterra_problems_v1/test_op_transfer_payload.pdl");
	}

	@Test
	public void testOpPickupBasecamp() {
		testPlanning("problems/transterra_problems_v1/test_op_pickup_basecamp.pdl");
	}

	@Test
	public void testOpPickupBasecamp2() {
		testPlanning("problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl");
	}

	@Test
	public void testOpPlaceBasecamp() {
		testPlanning("problems/transterra_problems_v1/test_op_place_basecamp.pdl");
	}

	@Test
	public void testMDeployBasecamp1() {
		testPlanning("problems/transterra_problems_v1/test_m_deploy_basecamp_1.pdl");
	}

	@Test
	public void testMDeployBasecamp2() {
		testPlanning("problems/transterra_problems_v1/test_m_deploy_basecamp_2.pdl");
	}

	@Test
	public void testMTakeSamples1() {
		testPlanning("problems/transterra_problems_v1/test_m_take_samples_1.pdl");
	}

	@Test
	public void testMTakeSamples2() {
		testPlanning("problems/transterra_problems_v1/test_m_take_samples_2.pdl");
	}

	@Test
	public void testMGetBasecamp1() {
		testPlanning("problems/transterra_problems_v1/test_m_get_basecamp_1.pdl");
	}

	@Test
	public void testMGetBasecamp2() {
		testPlanning("problems/transterra_problems_v1/test_m_get_basecamp_2.pdl");
	}
	
	@Test
	public void testMTransferFilled() {
		testPlanning("problems/transterra_problems_v1/test_m_transfer_filled.pdl");
	}
}
