package unify;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metacsp.framework.ConstraintNetwork;

import unify.CompoundNameMatchingConstraint.Type;

public class JunitTestCompoundNameMatchingConstraintSolver {
	
	private CompoundNameMatchingConstraintSolver solver;
	private CompoundNameVariable[] vars;
	private NameMatchingConstraintSolver nameSolver;

	@Before
	public void setUp() throws Exception {
		solver = new CompoundNameMatchingConstraintSolver();
		vars = (CompoundNameVariable[]) solver.createVariables(5);
		
		nameSolver = (NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
	}


	@Test
	public void testMatches() {
		vars[0].setName("On", "mug1", "table1"); // "On" in world state
		vars[1].setName("On", "?mug", "?table"); // "On" as precondition
		vars[2].setName("get_mug",  "?mug");     // "get_mug" task
		vars[3].setName("get_mug", "?mug");      // "grap_mug" task
		vars[4].setName("Holding", "?mug", "rightarm1"); // "holding" effect
		
		CompoundNameMatchingConstraint cc01 = new CompoundNameMatchingConstraint(Type.MATCHES);
		cc01.setFrom(vars[0]);
		cc01.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc01));
		
		NameMatchingConstraint nc12 = new NameMatchingConstraint();
		nc12.setFrom(vars[1].getInternalVariables()[1]);
		nc12.setTo(vars[2].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc12));
		
		
		NameMatchingConstraint nc32 = new NameMatchingConstraint();
		nc32.setFrom(vars[3].getInternalVariables()[1]);
		nc32.setTo(vars[2].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc32));
		
		NameMatchingConstraint nc342 = new NameMatchingConstraint();
		nc342.setFrom(vars[3].getInternalVariables()[1]);
		nc342.setTo(vars[4].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc342));
		System.out.println(vars[4].getName());
		assertTrue("'mug1' should be propageted to first parameter of 'holding'.", 
				vars[4].getName().equals("Holding(mug1 rightarm1)"));
	}
	
	@Test
	public void testSubmatches() {
		vars[0].setName("On", "mug1", "table1"); 
		vars[1].setName("get_mug", "?mug", "?table");
		vars[2].setName("get_mug",  "mug2"); 
		vars[3].setName("get_mug", "?mug");
		
		CompoundNameMatchingConstraint cc01 = new CompoundNameMatchingConstraint(Type.SUBMATCHES, 
				new int[] {0, 0, 1, 1});
		cc01.setFrom(vars[0]);
		cc01.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc01));
		System.out.println("Var1: " + vars[1].getName());
		assertTrue(vars[1].getName().equals("get_mug(mug1 table1)"));
		
		CompoundNameMatchingConstraint cc02 = new CompoundNameMatchingConstraint(Type.SUBMATCHES, 
				new int[] {0, 0});
		cc02.setFrom(vars[0]);
		cc02.setTo(vars[2]);
		assertFalse(solver.addConstraint(cc02));
		
		CompoundNameMatchingConstraint cc03 = new CompoundNameMatchingConstraint(Type.SUBMATCHES, 
				new int[] {0, 0});
		cc03.setFrom(vars[0]);
		cc03.setTo(vars[3]);
		assertTrue(solver.addConstraint(cc03));
		System.out.println("Var3: " + vars[3].getName());
		assertTrue(vars[3].getName().equals("get_mug(mug1)"));

	}


}
