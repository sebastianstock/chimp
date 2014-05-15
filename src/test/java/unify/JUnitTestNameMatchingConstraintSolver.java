package unify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.metacsp.framework.Constraint;

public class JUnitTestNameMatchingConstraintSolver {
	
	private NameMatchingConstraintSolver solver;
	private NameVariable[] vars;

	@Before
	public void setUp() throws Exception {
		solver = new NameMatchingConstraintSolver();
		vars = (NameVariable[]) solver.createVariables(4);
	}

	@Test
	public void test() {
		NameMatchingConstraint con0 = new NameMatchingConstraint();
		vars[0].setName("mug1");
		vars[1].setName("?m1");
		vars[2].setName("?m2");
		vars[3].setName("mug2");
		con0.setFrom(vars[0]);
		con0.setTo(vars[1]);
		assertTrue("Add constraint between 'mug1' and '?m1' should succeed", 
				solver.addConstraint(con0));
		assertTrue("Name of previous '?m1' should be changed to 'mug1",
				vars[1].getName().equals("mug1"));
		
		NameMatchingConstraint con1 = new NameMatchingConstraint();
		con1.setFrom(vars[1]);
		con1.setTo(vars[2]);
		NameMatchingConstraint con2 = new NameMatchingConstraint();
		con2.setFrom(vars[2]);
		con2.setTo(vars[3]);
		assertFalse(solver.addConstraints(new Constraint[] {con1, con2}));
		
	}

}
