package pfd0;

import org.metacsp.framework.ConstraintNetwork;

public class PFD0Operator extends PlanReportroryItem {

	public PFD0Operator(String taskname) {
		super(taskname);
	}

	
	@Override
	public ConstraintNetwork expand(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		return ret;
	}

}
