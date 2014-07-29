package pfd0;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0.PFD0MetaConstraint.markings;

public abstract class PlanReportroryItem {
	
	protected String taskname;
	
	protected PFD0Precondition[] preconditions;

	private String[] arguments;
	
// old
//	public PlanReportroryItem(String taskname, String[] preconditions) {
//		this.taskname = taskname;
//		this.preconditions = preconditions;
//	}
	
	public PlanReportroryItem(String taskname, String[] arguments, PFD0Precondition[] preconditions) {
		this.taskname = taskname;
		this.arguments = arguments;
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
		// Only check if taskname matches. Arguments are not checked here!
		if (taskname.equals(fluent.getCompoundNameVariable().getHeadName())) {
			return true;
		}
		return false;
	}
	
	
	public Map<String, Set<Fluent>> createFluentSetMap(Fluent[] fluents) {
		HashMap<String, Set<Fluent>> map = new HashMap<String, Set<Fluent>>();
		if (fluents != null) {
			for (int i = 0; i < fluents.length; i++) {
				String headName = fluents[i].getCompoundNameVariable().getHeadName();
				if(map.containsKey(headName)) {
					map.get(headName).add(fluents[i]);
				} else {
					HashSet<Fluent> newset = new HashSet<Fluent>();
					newset.add(fluents[i]);
					map.put(headName, newset);
				}
			}
		}
		return map;
	}
	
	/**
	 * Checks if all preconditions can potentially be fulfilled by the open fluents.
	 * @param openFluents Array of fluents with the marking OPEN
	 * @return True if the preconditions are fulfilled, otherwise false.
	 */
	public boolean checkPreconditions(Fluent[] openFluents) {
		if (preconditions == null)
			return true;
	
		if (openFluents != null) {
			Map<String, Set<Fluent>> fluentmap = createFluentSetMap(openFluents);	
			for (PFD0Precondition pre : preconditions) {
				boolean fulfilled = false;
				if (fluentmap.containsKey(pre.getFluenttype())) {
					for (Fluent f : fluentmap.get(pre.getFluenttype())) {
						if (f.getCompoundNameVariable().possibleMatch(pre.getFluenttype(), pre.getArguments())){
							fulfilled = true;
							break;
						}
					}
				}
				if (! fulfilled) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	/** Creates prototypes for the preconditions.
	 * For each precondition a precondition prototype will be created. This prototype will later be 
	 * connected via NameMatchingConstraints to the open fluents.
	 * 
	 * @param taskfluent The task that is expanded.
	 * @return New PRE constraints between new prototypes and taskfluent.
	 */
	protected Vector<FluentConstraint> addPreconditionPrototypes(Fluent taskfluent, 
			FluentNetworkSolver groundSolver) {
		
		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
		
		for (PFD0Precondition pre : this.preconditions) {
			String component = "Component"; // TODO use real component
			VariablePrototype newFluent = new VariablePrototype(groundSolver, component, 
					pre.getFluenttype(), pre.getArguments());
			newFluent.setMarking(markings.UNJUSTIFIED);
			newFluents.add(newFluent);
			FluentConstraint preconstr = new FluentConstraint(FluentConstraint.Type.PRE, 
					pre.getConnections());
			preconstr.setFrom(newFluent);
			preconstr.setTo(taskfluent);
			newConstraints.add(preconstr);
		}
		return newConstraints;
	}
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork after applying the operator/method.
	 */
	public abstract ConstraintNetwork expand(Fluent taskfluent, FluentNetworkSolver groundSolver);

}
