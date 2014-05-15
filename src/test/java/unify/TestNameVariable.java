package unify;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestNameVariable {
	
	private static NameMatchingConstraintSolver solver;
	private static NameVariable[] vars;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		solver = new NameMatchingConstraintSolver();
		vars = (NameVariable[]) solver.createVariables(1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testSetName() {
		vars[0].setName("mug1");
		assertTrue(vars[0].getName().equals("mug1"));
		vars[0].setName("?mug");
		assertTrue(vars[0].getName().equals("?mug"));
		vars[0].setName(null);
		assertTrue(vars[0].getName().equals(""));
	}

	@Test
	public void testIsGround() {
		vars[0].setName("mug1");
		assertTrue(vars[0].isGround());
		vars[0].setName("?mug");
		assertFalse(vars[0].isGround());
		vars[0].setName(null);
		assertTrue(vars[0].isGround());
	}

}
