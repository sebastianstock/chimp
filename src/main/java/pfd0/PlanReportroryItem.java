package pfd0;

import org.metacsp.framework.ConstraintNetwork;

public abstract class PlanReportroryItem {
	
	protected String taskname;
	
	public PlanReportroryItem(String taskname) {
		this.taskname = taskname;
	}
	
	public String getName() {
		return this.taskname;
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * Checks if a PlanreportroryItem is applicable to a given task. Currently, this only checks if the taskname is the same.
	 * 
	 * @param fluent The task.
	 * @return True if it is applicable, i.e., the names match, false otherwise.
	 */
	public boolean checkApplicability(Fluent fluent) {
		if (taskname.equals(fluent.getNameVariable().getName()))
			return true;
		return false;
	}
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork after applying the operator/method.
	 */
	public abstract ConstraintNetwork expand(Fluent taskfluent, FluentNetworkSolver groundSolver);

}
