package unify;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Computer;

public class TestCompoundNameVariable {
	
	private static CompoundNameMatchingConstraintSolver solver;
	private static CompoundNameVariable[] vars;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		solver = new CompoundNameMatchingConstraintSolver();
		vars = (CompoundNameVariable[]) solver.createVariables(1);
	}

	@Test
	public void testSetName() {
		vars[0].setName("On", "mug1", "counter1");
		assertTrue(vars[0].getHeadName().equals("On"));
		assertTrue(vars[0].toString().equals("On(mug1 counter1)"));
	}

	@Test
	public void testSetFullName() {
		vars[0].setFullName("On(mug1 counter1)");
		assertTrue(vars[0].getHeadName().equals("On"));
		assertTrue(vars[0].toString().equals("On(mug1 counter1)"));
	}

	@Test
	public void testPossibleMatch() {
		vars[0].setFullName("On(mug1 counter1)");
		assertTrue(vars[0].possibleMatch("On", new String[]{"mug1", "counter1"}));
		assertFalse(vars[0].possibleMatch("On", new String[]{"mug1"}));
		assertFalse(vars[0].possibleMatch("On", new String[]{"mug2", "counter1"}));
		assertTrue(vars[0].possibleMatch("On", new String[]{"?mug", "counter1"}));
		
	}

	@Test
	public void testGetArgumentsSize() {
		vars[0].setFullName("On(mug1 counter1)");
		assertTrue(vars[0].getArgumentsSize() == 2);
	}

}
