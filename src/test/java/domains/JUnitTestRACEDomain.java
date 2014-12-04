package domains;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;

import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;

public class JUnitTestRACEDomain {
	
	private static String[][] symbols;
	private static int[] ingredients;
	
	private PFD0Planner planner;
	private FluentNetworkSolver fluentSolver;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		symbols = RACEProblemsSingle.createSymbols();
		ingredients = RACEProblemsSingle.createIngredients();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		HybridDomain dom;
		try {
			dom = new HybridDomain(planner, "domains/race_domain.ddl");
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = dom.getFluentResourceUsages();
		selectionConstraint.setResourceUsages(fluentResourceUsages);
		planner.addMetaConstraint(selectionConstraint);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testMoveBase() {
		RACEProblemsSingle.createProblemMoveBase(fluentSolver);
		assertTrue(planner.backtrack());
		// TODO analyze plan
	}
	
	@Test
	public void testMoveBaseBlind() {
		RACEProblemsSingle.createProblemMoveBaseBlind(fluentSolver);
		assertTrue(planner.backtrack());
		// TODO analyze plan
	}
	
	@Test
	public void testTuckArms() {
		RACEProblemsSingle.createProblemTuckArms(fluentSolver);
		assertTrue(planner.backtrack());
		// TODO analyze plan
	}


}
