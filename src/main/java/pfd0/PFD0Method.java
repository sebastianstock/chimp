package pfd0;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0.PFD0MetaConstraint.markings;

public class PFD0Method extends PlanReportroryItem {
	
	private String[] subtasks;

	public PFD0Method(String taskname, String[] subtasks) {
		super(taskname);
		this.subtasks = subtasks;
	}
	

	@Override
	public ConstraintNetwork expand(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);

		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();

		// create prototypes and decomposition constraints
		if (subtasks == null)
			return ret;
		for (String name : subtasks) {
			String component = "Component"; // TODO use real component
			VariablePrototype newFluent = new VariablePrototype(groundSolver, component, name);
			newFluent.setMarking(markings.UNPLANNED);
			newFluents.add(newFluent);
			// create dc constraint
			FluentConstraint dc = new FluentConstraint(FluentConstraint.Type.DC);
			dc.setFrom(taskfluent);
			dc.setTo(newFluent);
			newConstraints.add(dc);
		}
		

		// TODO add fluents for the preconditions
		// TODO add constraints for the preconditions

		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);

		return ret;
	}

}
