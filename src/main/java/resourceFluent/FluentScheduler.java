package resourceFluent;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;

import fluentSolver.Fluent;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

public class FluentScheduler extends Schedulable {
	
	private final String predicateName;
	private final int field;
	private final String fieldValues;

	public FluentScheduler(VariableOrderingH varOH, ValueOrderingH valOH, String predicateName, 
			int field, String fieldValues) {
		super(varOH, valOH);
		this.predicateName = predicateName;
		this.field = field;
		this.fieldValues = fieldValues;
		this.setPeakCollectionStrategy(PEAKCOLLECTION.BINARY);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2767348254309353956L;
	
	
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		updateUsage();
		return super.getMetaVariables();
	}

	public void updateUsage() {
		// TODO test if this is fast enough
		if (activities == null) activities = new Vector<Activity>();
		else activities.clear();
		ConstraintSolver s = this.getGroundSolver();
		for (Variable var : s.getVariables()) {
			CompoundSymbolicVariable comp = ((Fluent) var).getCompoundSymbolicVariable();
			if (comp.getPredicateName().equals(predicateName)) {
				String[] symbols = comp.getSymbolsAt(field);
				if (symbols.length == 1 && symbols[0].equals(fieldValues)) {
					activities.add(((Fluent) var));
				}
			}
		}
	}
	

	@Override
	public boolean isConflicting(Activity[] peak) {
		if (peak.length < 2) return false;
		Fluent f1 = (Fluent)peak[0].getVariable();
		Fluent f2 = (Fluent)peak[1].getVariable();
		CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(CompoundSymbolicValueConstraint.Type.MATCHES);
		con.setFrom(f1.getCompoundSymbolicVariable());
		con.setTo(f2.getCompoundSymbolicVariable());
//		System.out.println("testing if constraint works:\n" + con);
		if (f1.getCompoundSymbolicVariable().getConstraintSolver().addConstraint(con)) {
			f1.getCompoundSymbolicVariable().getConstraintSolver().removeConstraint(con);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return this.metaCS.getConstraintSolvers()[0];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FluentScheduler ");
		sb.append(predicateName);
		sb.append(" : ");
		sb.append(field);
		sb.append(" : ");
		sb.append(fieldValues);
		return sb.toString();
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
