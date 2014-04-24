package pfd0;

import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;

import unify.NameMatchingConstraint;
import unify.NameMatchingConstraintSolver;

public class FluentNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5831971530237352714L;
	
	public FluentNetworkSolver(long origin, long horizon) {
		super(new Class[] {NameMatchingConstraint.class, SimpleBooleanValueConstraint.class}, Fluent.class, createConstraintSolvers(origin, horizon, -1), new int[] {1, 1});
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxFluents) {
		ConstraintSolver[] ret = new ConstraintSolver[] {
				new NameMatchingConstraintSolver(), new SimpleBooleanValueConstraintSolver(origin, horizon)};
		return ret;
	}

}
