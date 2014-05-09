package unify;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;

public class CompoundNameMatchingConstraintSolver extends MultiConstraintSolver {

	protected CompoundNameMatchingConstraintSolver() {
		super(new Class[] {CompoundNameMatchingConstraint.class}, CompoundNameVariable.class,
				createConstraintSolvers(), new int[] {10});
		// TODO Auto-generated constructor stub
	}

	private static ConstraintSolver[] createConstraintSolvers() {
		ConstraintSolver[] ret = new ConstraintSolver[] {new NameMatchingConstraintSolver()};
		return ret;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1675831566052143377L;

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
