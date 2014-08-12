package symbolicUnifyTyped;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

public class TypedCompoundSymbolicVariableConstraintSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7769305914879144221L;
	protected static int MAX_VARIABLES = 500;


	public TypedCompoundSymbolicVariableConstraintSolver(String[] symbols0, String[] symbols1,
			String[] symbols2, String[] symbols3, String[] symbols4) {
		super(new Class[] {SymbolicValueConstraint.class, SymbolicValueConstraint.class}, TypedCompoundSymbolicVariable.class,
				createConstraintSolvers(symbols0, symbols1, symbols2, symbols3, symbols4), new int[] {1, 1, 1, 1, 1});
		// TODO Auto-generated constructor stub
	}

	protected static ConstraintSolver[] createConstraintSolvers(String[] symbols0, String[] symbols1,
			String[] symbols2, String[] symbols3, String[] symbols4) {
		ConstraintSolver[] ret = new ConstraintSolver[] {
				new SymbolicVariableConstraintSolver(symbols0, MAX_VARIABLES), 
				new SymbolicVariableConstraintSolver(symbols1, MAX_VARIABLES),
				new SymbolicVariableConstraintSolver(symbols2, MAX_VARIABLES), 
				new SymbolicVariableConstraintSolver(symbols3, MAX_VARIABLES),
				new SymbolicVariableConstraintSolver(symbols4, MAX_VARIABLES)
				};
		return ret;
	}



	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
