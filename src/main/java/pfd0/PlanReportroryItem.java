package pfd0;

import java.util.HashMap;
import java.util.Map;

import org.metacsp.framework.ConstraintNetwork;

public abstract class PlanReportroryItem {
	
	protected String taskname;
	
	protected String[] preconditions;
	
	public PlanReportroryItem(String taskname, String[] preconditions) {
		this.taskname = taskname;
		this.preconditions = preconditions;
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
	
	
	public Map<String, Fluent> createFluentMap(Fluent[] fluents) {
		HashMap<String, Fluent> map = new HashMap<String, Fluent>();
		if (fluents != null) {
			for (int i = 0; i < fluents.length; i++) {
				map.put(fluents[i].getNameVariable().getName(), fluents[i]);
			}
		}
		return map;
	}
	
	/**
	 * Checks if all preconditions are fulfilled by the open fluents.
	 * @param openFluents Array of fluents with the marking OPEN
	 * @return True if the preconditions are fulfilled, otherwise false.
	 */
	public boolean checkPreconditions(Fluent[] openFluents) {
		if (preconditions == null)
			return true;
	
		if (openFluents != null) {
			Map<String, Fluent> fluentmap = createFluentMap(openFluents);	
			for (String pre : preconditions) {
				if (! fluentmap.containsKey(pre))
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork after applying the operator/method.
	 */
	public abstract ConstraintNetwork expand(Fluent taskfluent, FluentNetworkSolver groundSolver);

}
