package pfd0;

import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;

public class SimpleBooleanValueConstraintSolver extends MultiConstraintSolver {

	private static final long serialVersionUID = -8105880522155296176L;
	
	public SimpleBooleanValueConstraintSolver(long origin, long horizon) {
		super(new Class[] {SimpleBooleanValueConstraint.class}, SimpleBooleanValueVariable.class, createConstraintSolvers(origin, horizon, -1), new int[] {1});
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {
		ConstraintSolver[] ret = new ConstraintSolver[] {new BooleanSatisfiabilitySolver()};
		return ret;
	}
	
	

}
