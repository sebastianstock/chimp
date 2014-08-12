package symbolicUnify;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

public class CompoundSymbolicVariableConstraintSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7769305914879144221L;
	protected static int MAX_VARIABLES = 500;


	public CompoundSymbolicVariableConstraintSolver(String[] symbols, int numVars) {
		super(new Class[] {CompoundSymbolicValueConstraint.class}, CompoundSymbolicVariable.class,
				createConstraintSolvers(symbols), new int[] {numVars});
		// TODO Auto-generated constructor stub
	}

	protected static ConstraintSolver[] createConstraintSolvers(String[] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new SymbolicVariableConstraintSolver(symbols, MAX_VARIABLES)};
		return ret;
	}



	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
