package unify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.metacsp.framework.Constraint;

import unify.NameMatchingConstraint.Type;

public class JUnitTestNameMatchingConstraintSolver {
	
	private NameMatchingConstraintSolver solver;
	private static String[] symbols = new String[] {"mug1", "mug2", "mug3"};

	@Before
	public void setUp() throws Exception {
		solver = new NameMatchingConstraintSolver(symbols);
	}

	@Test
	public void testUnaryConstraints() {
		NameVariable[] vars = (NameVariable[]) solver.createVariables(1);
		NameMatchingConstraint con11 = new NameMatchingConstraint(Type.UNARYEQUALS);
		con11.setFrom(vars[0]);
		con11.setTo(vars[0]);
		con11.setUnaryValues(new int[] {1});
		assertTrue(solver.addConstraint(con11));
		System.out.println(vars[0]);
		assertTrue(vars[0].isGround());
		assertTrue(vars[0].getPossibleSymbols()[0].equals("mug2"));
		
		NameMatchingConstraint con11Diff = new NameMatchingConstraint(Type.UNARYDIFFERENT);
		con11Diff.setFrom(vars[0]);
		con11Diff.setTo(vars[0]);
		con11Diff.setUnaryValues(new int[] {1});
		assertFalse(solver.addConstraint(con11Diff));
	}
	
	@Test
	public void test() {
		NameVariable[] vars = (NameVariable[]) solver.createVariables(5);
		vars[0].setConstant("mug1");
		vars[3].setConstant("mug2");
		NameMatchingConstraint con01 = new NameMatchingConstraint(Type.EQUALS);
		con01.setFrom(vars[0]);
		con01.setTo(vars[1]);
		assertTrue("Add constraint between 'vars[0]' and 'vars[1]' should succeed", 
				solver.addConstraint(con01));
		assertTrue("Domain of previous 'vars[1]' should be changed to 'mug1",
				vars[1].getPossibleSymbols().length == 1);
		assertTrue("Domain of previous 'vars[1]' should be changed to 'mug1",
				vars[1].getPossibleSymbols()[0].equals("mug1"));
		
		NameMatchingConstraint con12 = new NameMatchingConstraint(Type.EQUALS);
		con12.setFrom(vars[1]);
		con12.setTo(vars[2]);
		NameMatchingConstraint con23 = new NameMatchingConstraint(Type.EQUALS);
		con23.setFrom(vars[2]);
		con23.setTo(vars[3]);
		assertFalse(solver.addConstraints(new Constraint[] {con12, con23}));
		
		NameMatchingConstraint con03 = new NameMatchingConstraint(Type.DIFFERENT);
		con03.setFrom(vars[0]);
		con03.setTo(vars[3]);
		assertTrue(solver.addConstraints(new Constraint[] {con03}));
		
		NameMatchingConstraint con34 = new NameMatchingConstraint(Type.DIFFERENT);
		con34.setFrom(vars[3]);
		con34.setTo(vars[4]);
		assertTrue(solver.addConstraints(new Constraint[] {con34}));
		assertTrue("Domain of previous 'vars[1]' should be changed to 'mug1",
				vars[4].getPossibleSymbols().length == 2);
		assertTrue("Domain of previous 'vars[1]' should be changed to ['mug1','mug3]",
				vars[4].getPossibleSymbols()[0].equals("mug1"));
		assertTrue("Domain of previous 'vars[1]' should be changed to ['mug1','mug3']",
				vars[4].getPossibleSymbols()[1].equals("mug3"));
		
	}
	
	
	
	

}
