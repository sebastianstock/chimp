package resourceFluent;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import chimp.search.ChimpSchedulable;
import org.metacsp.multi.activity.Activity;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.GroundSolverHolder;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

public class FluentScheduler extends ChimpSchedulable implements GroundSolverHolder {

	private final String predicateName;
	private final int field;
	private final String fieldValues;
	private FluentNetworkSolver groundSolver;

	public FluentScheduler(VariableOrderingH varOH, ValueOrderingH valOH, String predicateName,
			int field, String fieldValues) {
		super(varOH, valOH);
		this.predicateName = predicateName;
		this.field = field;
		this.fieldValues = fieldValues;
		this.setPeakCollectionStrategy(PeakCollection.BINARY);
	}

	public void setGroundSolver(FluentNetworkSolver groundSolver) {
		this.groundSolver = groundSolver;
	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		updateUsage();
		return super.getMetaVariables();
	}

	public void updateUsage() {
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
		if (f1.getCompoundSymbolicVariable().getConstraintSolver().addConstraint(con)) {
			f1.getCompoundSymbolicVariable().getConstraintSolver().removeConstraint(con);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return groundSolver;
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
}
