package symbolicUnifyTyped;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

import symbolicUnifyTyped.TypedCompoundSymbolicValueConstraint.Type;

public class TestTypedCompoundSymbolicVariable {

	private static TypedCompoundSymbolicVariableConstraintSolver solver;
	private static TypedCompoundSymbolicVariable[] vars;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		solver = new TypedCompoundSymbolicVariableConstraintSolver(symbols);
	}


	@Test
	public void testSetName() {
		TypedCompoundSymbolicVariable var0 = (TypedCompoundSymbolicVariable) solver.createVariable();
		var0.setName("on", "mug1", "pl1", "none", "none");
		propagateAll();
//		System.out.println(var0);
		String[] possiblePredicateNames = var0.getPossiblePredicateNames();
		assertTrue(possiblePredicateNames.length == 1);
		assertTrue(possiblePredicateNames[0].equals("on"));
		assertTrue(var0.toString().equals("[on]([mug1] [pl1] [none] [none])"));
	}
	
	@Test
	public void testSetFullName() {
		TypedCompoundSymbolicVariable var = (TypedCompoundSymbolicVariable) solver.createVariable();
		var.setFullName("on(mug1 pl1 none none)");
		propagateAll();
//		System.out.println(var);
		String[] possiblePredicateNames = var.getPossiblePredicateNames();
		assertTrue(possiblePredicateNames.length == 1);
		assertTrue(possiblePredicateNames[0].equals("on"));
		assertTrue(var.toString().equals("[on]([mug1] [pl1] [none] [none])"));
	}
	
	public void propagateAll() {
		for (ConstraintSolver symbolicsolver : solver.getConstraintSolvers()) {
			((SymbolicVariableConstraintSolver) symbolicsolver).getConstraintSolvers()[0].propagate();
		}
	}
	

}
