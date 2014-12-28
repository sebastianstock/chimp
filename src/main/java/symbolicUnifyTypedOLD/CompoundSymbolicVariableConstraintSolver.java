package symbolicUnifyTypedOLD;

import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

public class CompoundSymbolicVariableConstraintSolver extends MultiConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7769305914879144221L;
	protected static int MAX_VARIABLES = 500;
	private int[] ingredients;
	private int[] varIndex2solverIndex;
	

	public CompoundSymbolicVariableConstraintSolver(String[][] symbols, int[] ingredients) {
		super(new Class[] {CompoundSymbolicValueConstraint.class}, CompoundSymbolicVariable.class,
				createConstraintSolvers(symbols), ingredients);
		this.ingredients = ingredients;
		this.createVarIndex2SolverIndex();
	}

	protected static ConstraintSolver[] createConstraintSolvers(String[][] symbols) {
		ConstraintSolver[] ret = new ConstraintSolver[symbols.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new SymbolicVariableConstraintSolver(symbols[i], MAX_VARIABLES);
			
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
		for (ConstraintSolver symbolicsolver : getConstraintSolvers()) {
			((SymbolicVariableConstraintSolver) symbolicsolver).getConstraintSolvers()[0].propagate();
		}
	}
	
//	public void propagateAllSubFull() {
//		for (ConstraintSolver symbolicsolver : getConstraintSolvers()) {
//			((BooleanSatisfiabilitySolver) ((SymbolicVariableConstraintSolver) symbolicsolver).getConstraintSolvers()[0]).propagateFull();
//		}
//	}
//	
//	public void propagatePredicateNames() {
//		BooleanSatisfiabilitySolver bs = (BooleanSatisfiabilitySolver) ((SymbolicVariableConstraintSolver) getConstraintSolvers()[0]).getConstraintSolvers()[0];
//		bs.propagateFull();
//	}
	
	
	

	public int[] getIngredients() {
		return ingredients;
	}


	public int[] getVarIndex2solverIndex() {
		return varIndex2solverIndex;
	}
}
