package unify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestNameVariable {
	
	private static NameMatchingConstraintSolver solver;
	private static String[] symbols = new String[] {"mug1", "mug2", "mug3"};


	@Before
	public void setUp() throws Exception {
		solver = new NameMatchingConstraintSolver(symbols);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testPossibleSymbols() {
		NameVariable[] vars = (NameVariable[]) solver.createVariables(2);
		vars[0].setConstant("mug2");
		String[] vars0Symbols = vars[0].getPossibleSymbols();
		assertTrue(vars0Symbols.length == 1);
		assertTrue(vars0Symbols[0].equals("mug2"));
		
		String[] vars1Symbols = vars[1].getPossibleSymbols();
		assertTrue(vars1Symbols.length == 3);
		assertTrue(vars1Symbols[0].equals("mug1"));
		assertTrue(vars1Symbols[1].equals("mug2"));
		assertTrue(vars1Symbols[2].equals("mug3"));
	}

	@Test
	public void testIsGround() {
		NameVariable[] vars = (NameVariable[]) solver.createVariables(3);
		vars[0].setConstant("mug2");
		assertTrue(vars[0].isGround());
		assertFalse(vars[1].isGround());
	}

}
