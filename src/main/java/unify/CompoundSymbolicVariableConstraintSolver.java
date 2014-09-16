package unify;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;

public class CompoundSymbolicVariableConstraintSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1675831566052143377L;
	private int[] varIndex2solverIndex;


	public CompoundSymbolicVariableConstraintSolver(String[][] symbols, int[] ingredients) {
		super(new Class[] {CompoundSymbolicValueConstraint.class}, CompoundSymbolicVariable.class,
				createConstraintSolvers(symbols), ingredients);
		this.ingredients = ingredients;
		this.createVarIndex2SolverIndex();
	}

	private static ConstraintSolver[] createConstraintSolvers(String[][] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[symbols.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new NameMatchingConstraintSolver(symbols[i]);
		}
		return ret;
	}
	
	private void createVarIndex2SolverIndex() {
		int varCount = 0;
		for (int i = 0; i < ingredients.length; i++)
			varCount += ingredients[i];
		varIndex2solverIndex = new int[varCount];
		int varIndex = 0;
		for (int solverIndex = 0; solverIndex < ingredients.length; solverIndex++) {
			for (int j = 0; j < ingredients[solverIndex]; j++) {
				varIndex2solverIndex[varIndex++] = solverIndex;
			}
		}
	}


	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void propagateAllSub() {
		for (ConstraintSolver solver : getConstraintSolvers()) {
			solver.propagate();
		}
	}
	
	public int[] getVarIndex2solverIndex() {
		return varIndex2solverIndex;
	}

	/** Only needed to be compatible to equivalent in symbolicUnifyTyped
	 * 
	 */
	public void propagateAllSubFull() {
		this.propagateAllSub();
	}

}
