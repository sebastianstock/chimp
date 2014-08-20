package pfd0Symbolic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metacsp.booleanSAT.BooleanDomain;
import org.metacsp.framework.Constraint;

import simpleBooleanValueCons.SimpleBooleanValueConstraint;
import simpleBooleanValueCons.SimpleBooleanValueConstraint.Type;
import simpleBooleanValueCons.SimpleBooleanValueConstraintSolver;

public class JUnitTestFluentNetworkSolver {
	
	private FluentNetworkSolver solver;

	@Before
	public void setUp() throws Exception {
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		solver = new FluentNetworkSolver(0, 500, symbols);
	}

	@After
	public void tearDown() throws Exception {
		solver = null;
	}

	@Test
	public void testGetOpenFluents() {
		Fluent[] fluents = (Fluent[]) solver.createVariables(5);
		fluents[0].setMarking(TaskApplicationMetaConstraint.markings.OPEN);
		fluents[1].setMarking(TaskApplicationMetaConstraint.markings.CLOSED);
		fluents[2].setMarking(TaskApplicationMetaConstraint.markings.UNPLANNED);
		fluents[3].setMarking(TaskApplicationMetaConstraint.markings.OPEN);
		fluents[4].setMarking(TaskApplicationMetaConstraint.markings.PLANNED);
		assertArrayEquals(new Fluent[] {fluents[0],  fluents[3]}, solver.getOpenFluents());
	}

	@Test
	public void testGetConstraintsTo() {
		Fluent[] fluents = (Fluent[]) solver.createVariables(4);
		fluents[0].setName("on(mug1 pl1 none none)");
		fluents[1].setName("on(?mug pl1 none none)");
		fluents[2].setName("on(mug1 pl2 none none)");
		SimpleBooleanValueConstraintSolver bsolver = 
				(SimpleBooleanValueConstraintSolver) solver.getConstraintSolvers()[1];
		
		SimpleBooleanValueConstraint con00 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con00.setFrom(fluents[1].getSimpleBooleanValueVariable());
		con00.setTo(fluents[1].getSimpleBooleanValueVariable());
		bsolver.addConstraint(con00);
	
		FluentConstraint fcon1 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon1.setFrom(fluents[0]);
		fcon1.setTo(fluents[1]);
		solver.addConstraint(fcon1);
		
		assertTrue(Arrays.asList(solver.getConstraints()).equals(solver.getConstraintsTo(fluents[1])));
		
		assertTrue(Arrays.asList(new Constraint[] {fcon1}).equals(solver.getConstraintsTo(fluents[1])));
		
		FluentConstraint fcon2 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon2.setFrom(fluents[1]);
		fcon2.setTo(fluents[0]);
		solver.addConstraint(fcon2);
		
		assertTrue(Arrays.asList(new Constraint[] {fcon1}).equals(solver.getConstraintsTo(fluents[1])));
		
		FluentConstraint fcon3 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon3.setFrom(fluents[3]);
		fcon3.setTo(fluents[1]);
		solver.addConstraint(fcon3);
		
		List<Constraint> res = solver.getConstraintsTo(fluents[1]);
		assertTrue(res.contains(fcon1));
		assertTrue(res.contains(fcon3));
		Assert.assertEquals(2,  res.size());
	}
	
	
	@Test
	public void test() {
		Fluent[] fluents = (Fluent[]) solver.createVariables(3);
		
//		MetaCSPLogging.setLevel(Level.FINEST);
		SimpleBooleanValueConstraintSolver bsolver = 
				(SimpleBooleanValueConstraintSolver) solver.getConstraintSolvers()[1];
		
		SimpleBooleanValueConstraint con00 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con00.setFrom(fluents[0].getSimpleBooleanValueVariable());
		con00.setTo(fluents[0].getSimpleBooleanValueVariable());
		assertTrue(bsolver.addConstraint(con00));
		
		BooleanDomain bdomainf0 = 
				(BooleanDomain) fluents[0].getSimpleBooleanValueVariable().getBooleanVariable().getDomain();
		assertTrue(bdomainf0.canBeTrue());
		assertFalse(bdomainf0.canBeFalse());
		
		fluents[0].setName("on(mug1 pl1 none none)");
		fluents[1].setName("on(?mug pl1 none none)");
		fluents[2].setName("on(mug1 pl2 none none)");
		
		
		FluentConstraint fcon01 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon01.setFrom(fluents[0]);
		fcon01.setTo(fluents[1]);
		assertTrue(solver.addConstraint(fcon01));
		
		// boolean value of fluents[1] should be changed to true
		BooleanDomain bdomainf1 = 
				(BooleanDomain) fluents[1].getSimpleBooleanValueVariable().getBooleanVariable().getDomain();
		assertTrue(bdomainf1.canBeTrue());
		assertFalse(bdomainf1.canBeFalse());
		
		// name of '?mug' should be changed to 'mug1'
		assertTrue("Name of '?mug' should be changed to 'mug1'",
				fluents[1].getCompoundSymbolicVariable().getName().equals("[on]([mug1] [pl1] [none] [none])"));
		
		FluentConstraint fcon02 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon02.setFrom(fluents[0]);
		fcon02.setTo(fluents[2]);
		assertFalse(solver.addConstraint(fcon02));
		
		
	}
	

}
