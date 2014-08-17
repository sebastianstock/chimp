package symbolicUnifyTyped;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metacsp.framework.ConstraintNetwork;

import symbolicUnifyTyped.TypedCompoundSymbolicValueConstraint.Type;

public class JunitTestCompoundNameMatchingConstraintSolver {
	
	private TypedCompoundSymbolicVariableConstraintSolver solver;


	@Before
	public void setUp() throws Exception {
		String[] symbolsPredicates = {"on", "robotat", "get_mug", "grasp_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		solver = new TypedCompoundSymbolicVariableConstraintSolver(symbols);
	}


	@Test
	public void test() {
		TypedCompoundSymbolicVariable[] vars = (TypedCompoundSymbolicVariable[]) solver.createVariables(5);
		vars[0].setName("on", "mug1", "pl1", "none", "none"); // "on" in world state
		vars[1].setName("on", "?mug", "?pl", "none", "none"); // "on" as precondition
		vars[2].setName("get_mug",  "?mug", "?pl", "none", "none");     // "get_mug" task
		vars[3].setName("grasp_mug", "?mug", "pl1", "none", "none");      // "grasp_mug" task
		vars[4].setName("robotat", "none", "pl1", "none", "none");
		
		TypedCompoundSymbolicValueConstraint mc01 = new TypedCompoundSymbolicValueConstraint(Type.MATCHES);
		mc01.setFrom(vars[0]);
		mc01.setTo(vars[1]);
		
		TypedCompoundSymbolicValueConstraint sc12 = 
				new TypedCompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {0, 0, 1, 1});
		sc12.setFrom(vars[1]);
		sc12.setTo(vars[2]);
		
		TypedCompoundSymbolicValueConstraint sc32 = 
				new TypedCompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {0, 0, 1, 1});
		sc32.setFrom(vars[3]);
		sc32.setTo(vars[2]);
		
		TypedCompoundSymbolicValueConstraint sc34 = 
				new TypedCompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {1, 1});
		sc34.setFrom(vars[3]);
		sc34.setTo(vars[4]);
		
		assertTrue(solver.addConstraints(mc01, sc12, sc32, sc34));
		
		System.out.println(vars[0].getName());
		System.out.println(vars[1].getName());
		System.out.println(vars[2].getName());
		System.out.println(vars[3].getName());
		System.out.println(vars[4].getName());
//		assertTrue("'mug1' should be propageted to first parameter of 'holding'.", 
//				vars[4].getName().equals("Holding(mug1 rightarm1)"));
		
		// TODO test names
		
	}


}
