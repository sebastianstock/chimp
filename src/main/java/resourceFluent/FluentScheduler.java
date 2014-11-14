package resourceFluent;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.meta.symbolsAndTime.Schedulable.PEAKCOLLECTION;
import org.metacsp.multi.activity.Activity;

import cern.colt.Arrays;
import pfd0Symbolic.Fluent;
import pfd0Symbolic.FluentNetworkSolver;
import unify.CompoundSymbolicValueConstraint;

public class FluentScheduler extends Schedulable {

	public FluentScheduler(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		this.setPeakCollectionStrategy(PEAKCOLLECTION.BINARY);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2767348254309353956L;

	@Override
	public boolean isConflicting(Activity[] peak) {
		if (peak.length < 2) return false;
		Fluent f1 = (Fluent)peak[0].getVariable();
		Fluent f2 = (Fluent)peak[1].getVariable();
		CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(CompoundSymbolicValueConstraint.Type.MATCHES);
		con.setFrom(f1.getCompoundSymbolicVariable());
		con.setTo(f2.getCompoundSymbolicVariable());
		System.out.println("testing if constraint works:\n" + con);
		if (f1.getCompoundSymbolicVariable().getConstraintSolver().addConstraint(con)) {
			f1.getCompoundSymbolicVariable().getConstraintSolver().removeConstraint(con);
			return false;
		}
		return true;
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

}
