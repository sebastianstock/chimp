package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;


public class PFD0Method extends PlanReportroryItem {
	
	private VariablePrototype[] subtaskprototypes;
	private FluentConstraint[] constraints;

	
	public PFD0Method(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			VariablePrototype[] subtaskprototypes, FluentConstraint[] constraints) {
		super(taskname, arguments, preconditions);
		this.subtaskprototypes = subtaskprototypes;
		this.constraints = constraints;
	}
	
	
	@Override
	public ConstraintNetwork expandTail(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);

		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();

		// create prototypes and decomposition constraints
		if (subtaskprototypes == null)
			return ret;
		for (VariablePrototype sub : subtaskprototypes) {
			sub.setMarking(markings.UNPLANNED);
			newFluents.add(sub);
			// create dc constraint
			FluentConstraint dc = new FluentConstraint(FluentConstraint.Type.DC);
			dc.setFrom(taskfluent);
			dc.setTo(sub);
			newConstraints.add(dc);
		}
		if (constraints != null) {
			for (FluentConstraint c : constraints) {
				newConstraints.add(c);
			}
		}
		

		// TODO add fluents for the preconditions
		// TODO add constraints for the preconditions

		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);

		return ret;
	}

}
