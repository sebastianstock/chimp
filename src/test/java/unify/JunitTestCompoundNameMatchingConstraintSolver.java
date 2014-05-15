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

		vars[0].setName("On", "mug1", "table1"); // "On" in world state
		vars[1].setName("On", "?mug", "?table"); // "On" as precondition
		vars[2].setName("get_mug",  "?mug");     // "get_mug" task
		vars[3].setName("get_mug", "?mug");      // "grap_mug" task
		vars[4].setName("Holding", "?mug", "rightarm1"); // "holding" effect
		
		nameSolver = (NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
	}


	@Test
	public void test() {
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

}
