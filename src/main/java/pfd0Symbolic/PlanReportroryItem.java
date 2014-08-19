package pfd0Symbolic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;

public abstract class PlanReportroryItem {
	
	protected String taskname;
	
	protected PFD0Precondition[] preconditions;

	protected String[] arguments;
	
	
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
		// test all possible predicate names
		for (String possibleName : fluent.getCompoundSymbolicVariable().getPossiblePredicateNames()) {
			if (taskname.equals(possibleName)) {
				return true;
			}
		}
		return false;
	}
	
	public Map<String, Set<Fluent>> createFluentSetMap(Fluent[] fluents) {
		HashMap<String, Set<Fluent>> map = new HashMap<String, Set<Fluent>>();
		if (fluents != null) {
			for (int i = 0; i < fluents.length; i++) {
				for (String headName : fluents[i].getCompoundSymbolicVariable().getPossiblePredicateNames()) {
					if(map.containsKey(headName)) {
						map.get(headName).add(fluents[i]);
					} else {
						HashSet<Fluent> newset = new HashSet<Fluent>();
						newset.add(fluents[i]);
						map.put(headName, newset);
					}
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
						if (f.getCompoundSymbolicVariable().possibleMatch(pre.getFluenttype(), pre.getArguments())){
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
		
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
		if(this.preconditions != null) {
			for (PFD0Precondition pre : this.preconditions) {
				newConstraints.add(pre.createPreconditionConstraint(taskfluent, groundSolver));
			}
		}
		return newConstraints;
	}
	
	/**
	 * Creates the dummy preconditions for a task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork witht the added dummy preconditions.
	 */
	public ConstraintNetwork expandPreconditions(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		
		ConstraintNetwork ret = new ConstraintNetwork(null);
		if(this.preconditions != null) {
			for (PFD0Precondition pre : this.preconditions) {
				ret.addConstraint(pre.createPreconditionConstraint(taskfluent, groundSolver));
			}
		}
		return ret;		
	}
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork after applying the operator/method.
	 */
	public abstract ConstraintNetwork expandTail(Fluent taskfluent, FluentNetworkSolver groundSolver);
	
	/**
	 * Creates the connections array for the opens constraint.
	 * 
	 * Looks at the arguments of effects and operator to see if they should have an equal constraint.
	 * @param args Arguments of the operator/method.
	 * @return Array representing the connections.
	 */
	protected int[] createConnections(String[] args) {
		boolean[] matches = new boolean[args.length];
		int matchesCnt = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("?") && args[i].equals(this.arguments[i])) {
				matches[i] = true;
				matchesCnt++;
			}
		}
		if (matchesCnt > 0) {
			int[] connections = new int[matchesCnt * 2];
			int j = 0;
			for (int i = 0; i < matches.length; i++) {
				if (matches[i])  {
					connections[j] = i;
					connections[j+1] = i;
					j += 2;
				}
			}
			return connections;
		}
		else 
			return null;
	}

}
