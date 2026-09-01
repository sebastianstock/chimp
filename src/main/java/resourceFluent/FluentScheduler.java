package resourceFluent;

import java.util.Comparator;
import java.util.Vector;

import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.search.ChimpFlaw;
import chimp.search.ChimpSchedulable;
import htn.GroundSolverHolder;

public class FluentScheduler extends ChimpSchedulable implements GroundSolverHolder {

	private final String predicateName;
	private final int field;
	private final String fieldValues;
	private FluentNetworkSolver groundSolver;

	public FluentScheduler(Comparator<ChimpFlaw> varOH, Comparator<ConstraintNetwork> valOH, String predicateName,
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
	public ChimpFlaw[] getMetaVariables() {
		updateUsage();
		return super.getMetaVariables();
	}

	public void updateUsage() {
		if (activities == null) activities = new Vector<Fluent>();
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
	public boolean isConflicting(Fluent[] peak) {
		if (peak.length < 2) return false;
		Fluent f1 = peak[0];
		Fluent f2 = peak[1];
		CompoundSymbolicConstraint con = new CompoundSymbolicConstraint(CompoundSymbolicConstraint.Type.MATCHES);
		con.setFrom(f1.getCompoundSymbolicVariable());
		con.setTo(f2.getCompoundSymbolicVariable());
		ConstraintSolver solver = f1.getCompoundSymbolicVariable().getConstraintSolver();
		if (solver.addConstraint(con)) {
			solver.removeConstraint(con);
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
