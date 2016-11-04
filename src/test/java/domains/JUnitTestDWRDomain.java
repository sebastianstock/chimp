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

public class JUnitTestDWRDomain {
	
	private String[][] symbols;
	private int[] ingredients;
	private HybridDomain domain;

	@Before
	public void setUp() throws Exception {
		try {
			domain = new HybridDomain("domains/dwr/dwr.ddl");
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
	public void test1() {
		testPlanning("domains/dwr/test/test_op_leave.pdl");
	}
	
	@Test
	public void test2() {
		testPlanning("domains/dwr/test/test_op_enter.pdl");
	}

	@Test
	public void test3() {
		testPlanning("domains/dwr/test/test_op_move.pdl");
	}

	@Test
	public void test4() {
		testPlanning("domains/dwr/test/test_op_stack.pdl");
	}

	@Test
	public void test5() {
		testPlanning("domains/dwr/test/test_op_unstack.pdl");
	}

	@Test
	public void test6() {
		testPlanning("domains/dwr/test/test_op_put.pdl");
	}

	@Test
	public void test7() {
		testPlanning("domains/dwr/test/test_op_take.pdl");
	}

	@Test
	public void test8() {
		testPlanning("domains/dwr/test/test_m_load.pdl");
	}
	
	@Test
	public void test9() {
		testPlanning("domains/dwr/test/test_m_unload.pdl");
	}

	@Test
	public void test10() {
		testPlanning("domains/dwr/test/test_m_uncover0.pdl");
	}

	@Test
	public void test11() {
		testPlanning("domains/dwr/test/test_m_uncover0.pdl");
	}
	
	@Test
	public void test12() {
		testPlanning("domains/dwr/test/test_m_uncover1.pdl");
	}

	@Test
	public void test13() {
		testPlanning("domains/dwr/test/test_m_navigate0.pdl");
	}

	@Test
	public void test14() {
		testPlanning("domains/dwr/test/test_m_navigate1.pdl");
	}

	@Test
	public void test15() {
		testPlanning("domains/dwr/test/test_m_goto0.pdl");
	}

	@Test
	public void test16() {
		testPlanning("domains/dwr/test/test_m_goto1.pdl");
	}
	
	@Test
	public void test17() {
		testPlanning("domains/dwr/test/test_m_bring0.pdl");
	}

	@Test
	public void test18() {
		testPlanning("domains/dwr/test/test_m_bring1.pdl");
	}

	@Test
	public void test19() {
		testPlanning("domains/dwr/test/test_m_bring2.pdl");
	}

}
