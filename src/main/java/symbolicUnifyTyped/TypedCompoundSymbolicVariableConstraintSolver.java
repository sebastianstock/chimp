package symbolicUnifyTyped;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

public class TypedCompoundSymbolicVariableConstraintSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7769305914879144221L;
	protected static int MAX_VARIABLES = 500;


	public TypedCompoundSymbolicVariableConstraintSolver(String[][] symbols) {
		super(new Class[] {TypedCompoundSymbolicValueConstraint.class}, TypedCompoundSymbolicVariable.class,
				createConstraintSolvers(symbols), createInternals(symbols.length));
	}
	
	private static int[] createInternals(int length) {
		int[] internals = new int[length];
		for (int i = 0; i < internals.length; i++) {
			internals[i] = 1;
		}
		return internals;
	}

	protected static ConstraintSolver[] createConstraintSolvers(String[][] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[symbols.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new SymbolicVariableConstraintSolver(symbols[i], MAX_VARIABLES);
			
		}
		return ret;
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void propagateAllSub() {
		for (ConstraintSolver symbolicsolver : getConstraintSolvers()) {
			((SymbolicVariableConstraintSolver) symbolicsolver).getConstraintSolvers()[0].propagate();
		}
	}

}
