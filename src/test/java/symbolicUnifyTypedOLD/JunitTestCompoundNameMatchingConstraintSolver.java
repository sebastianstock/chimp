package symbolicUnifyTypedOLD;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metacsp.framework.ConstraintNetwork;

import old_dev.symbolicUnifyTypedOLD.CompoundSymbolicValueConstraint;
import old_dev.symbolicUnifyTypedOLD.CompoundSymbolicVariable;
import old_dev.symbolicUnifyTypedOLD.CompoundSymbolicVariableConstraintSolver;
import old_dev.symbolicUnifyTypedOLD.CompoundSymbolicValueConstraint.Type;

public class JunitTestCompoundNameMatchingConstraintSolver {
	
	private CompoundSymbolicVariableConstraintSolver solver;


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
		solver = new CompoundSymbolicVariableConstraintSolver(symbols, new int[] {1,1,1,1,1});
	}


	@Test
	public void test() {
		CompoundSymbolicVariable[] vars = (CompoundSymbolicVariable[]) solver.createVariables(5);
		vars[0].setName("on", "mug1", "pl1", "none", "none"); // "on" in world state
		vars[1].setName("on", "?mug", "?pl", "none", "none"); // "on" as precondition
		vars[2].setName("get_mug",  "?mug", "?pl", "none", "none");     // "get_mug" task
		vars[3].setName("grasp_mug", "?mug", "pl1", "none", "none");      // "grasp_mug" task
		vars[4].setName("robotat", "none", "pl1", "none", "none");
		
		CompoundSymbolicValueConstraint mc01 = new CompoundSymbolicValueConstraint(Type.MATCHES);
		mc01.setFrom(vars[0]);
		mc01.setTo(vars[1]);
		
		CompoundSymbolicValueConstraint sc12 = 
				new CompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {0, 0, 1, 1});
		sc12.setFrom(vars[1]);
		sc12.setTo(vars[2]);
		
		CompoundSymbolicValueConstraint sc32 = 
				new CompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {0, 0, 1, 1});
		sc32.setFrom(vars[3]);
		sc32.setTo(vars[2]);
		
		CompoundSymbolicValueConstraint sc34 = 
				new CompoundSymbolicValueConstraint(Type.SUBMATCHES, new int[] {1, 1});
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
		
		System.out.println("vars0: " + vars[0].getName());
		
		assertTrue(vars[1].getName().equals("[on]([mug1] [pl1] [none] [none])"));
		assertTrue(vars[2].getName().equals("[get_mug]([mug1] [pl1] [none] [none])"));
		assertTrue(vars[3].getName().equals("[grasp_mug]([mug1] [pl1] [none] [none])"));
		assertTrue(vars[4].getName().equals("[robotat]([none] [pl1] [none] [none])"));
		
		
	}


}
