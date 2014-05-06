package pfd0;

import java.util.ArrayList;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;

import pfd0.PFD0MetaConstraint.markings;
import unify.NameMatchingConstraintSolver;

public class FluentNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5831971530237352714L;
	
	public FluentNetworkSolver(long origin, long horizon) {
		super(new Class[] {FluentConstraint.class}, Fluent.class, createConstraintSolvers(origin, horizon, -1), new int[] {1, 1});
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
	
	/**
	 * @return All Fluents with the marking OPEN.
	 */
	public Fluent[] getOpenFluents() {
		ArrayList<Fluent> ret = new ArrayList<Fluent>();
		for (Variable var: getVariables()) {
			if (var.getMarking() == markings.OPEN) {
				ret.add((Fluent) var);
			}
		}
		return ret.toArray(new Fluent[ret.size()]);
	}
	
	public Constraint[] getConstraintsTo(Variable to) {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (con.getScope().length == 2) {
				if (con.getScope()[1].equals(to)) ret.add(con);
			}
		}
		return ret.toArray(new Constraint[ret.size()]);
	}


}
