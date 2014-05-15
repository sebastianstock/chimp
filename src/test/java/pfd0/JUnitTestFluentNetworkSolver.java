package pfd0;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metacsp.booleanSAT.BooleanDomain;
import org.metacsp.framework.Constraint;

import pfd0.SimpleBooleanValueConstraint.Type;

public class JUnitTestFluentNetworkSolver {
	
	private FluentNetworkSolver solver;
	private Fluent[] fluents;

	@Before
	public void setUp() throws Exception {
		solver = new FluentNetworkSolver(0, 500);
		fluents = (Fluent[]) solver.createVariables(5);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testGetOpenFluents() {
		fluents[0].setMarking(PFD0MetaConstraint.markings.OPEN);
		fluents[1].setMarking(PFD0MetaConstraint.markings.CLOSED);
		fluents[2].setMarking(PFD0MetaConstraint.markings.UNPLANNED);
		fluents[3].setMarking(PFD0MetaConstraint.markings.OPEN);
		fluents[4].setMarking(PFD0MetaConstraint.markings.PLANNED);
		assertArrayEquals(new Fluent[] {fluents[0],  fluents[3]}, solver.getOpenFluents());
	}

	@Test
	public void testGetConstraintsTo() {
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
		
		assertArrayEquals(solver.getConstraints(), solver.getConstraintsTo(fluents[1]));
		
		assertArrayEquals(new Constraint[] {fcon1}, solver.getConstraintsTo(fluents[1]));
		
		FluentConstraint fcon2 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon2.setFrom(fluents[1]);
		fcon2.setTo(fluents[0]);
		solver.addConstraint(fcon2);
		
		assertArrayEquals(new Constraint[] {fcon1}, solver.getConstraintsTo(fluents[1]));
		
		FluentConstraint fcon3 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon3.setFrom(fluents[3]);
		fcon3.setTo(fluents[1]);
		solver.addConstraint(fcon3);
		
		Vector<Constraint> res = 
				new Vector<Constraint>(Arrays.asList(solver.getConstraintsTo(fluents[1])));
		assertTrue(res.contains(fcon1));
		assertTrue(res.contains(fcon3));
		Assert.assertEquals(2,  res.size());
	}
	
	@Test
	public void test() {
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
		
		fluents[0].setName("On(mug1 counter1)");
		fluents[1].setName("On(?mug counter1)");
		
//		SimpleBooleanValueConstraint con1 = new SimpleBooleanValueConstraint(Type.EQUALS);
//		con1.setFrom(fluents[0]);
//		con1.setTo(fluents[1]);
////		logger.info("Added con1? " + bsolver.addConstraint(con1));
		
//		CompoundNameMatchingConstraint ncon0 = 
//				new CompoundNameMatchingConstraint(CompoundNameMatchingConstraint.Type.MATCHES);
//		ncon0.setFrom(fluents[0].getCompoundNameVariable());
//		ncon0.setTo(fluents[1].getCompoundNameVariable());
//		CompoundNameMatchingConstraintSolver nsolver = 
//				(CompoundNameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
//		logger.info("Added ncon0? " + nsolver.addConstraint(ncon0));
		
		FluentConstraint fcon = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon.setFrom(fluents[0]);
		fcon.setTo(fluents[1]);
		assertTrue(solver.addConstraint(fcon));
		
		// boolean value of fluents[1] should be changed to true
		BooleanDomain bdomainf1 = 
				(BooleanDomain) fluents[1].getSimpleBooleanValueVariable().getBooleanVariable().getDomain();
		assertTrue(bdomainf1.canBeTrue());
		assertFalse(bdomainf1.canBeFalse());
		
		// name of '?mug' should be changed to 'mug1'
		assertTrue("Name of '?mug' should be changed to 'mug1'",
				fluents[1].getCompoundNameVariable().getName().equals("On(mug1 counter1)"));
		
		
	}

}
