package pfd0Symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import com.google.common.collect.Sets;

public abstract class PlanReportroryItem {
	
	protected String taskname;
	
	protected PFD0Precondition[] preconditions;

	protected String[] arguments;
	
	/**
	 * The bounds that will be used for the duration constraint of this task.
	 */
	protected Bounds durationBounds;
	
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
	 * Creates the dummy preconditions for a task + Adds Duration constraint
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork witht the added dummy preconditions.
	 */
	public List<ConstraintNetwork> expandPreconditions(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		List<ConstraintNetwork> ret = new ArrayList<ConstraintNetwork>();
		
		Fluent[] openFluents = groundSolver.getOpenFluents();
		List<Set<FluentConstraint>> constraints = new ArrayList<Set<FluentConstraint>>();
		
		if(this.preconditions != null) {
			for (PFD0Precondition pre : this.preconditions) {
				String headName = pre.getFluenttype();
				Set<FluentConstraint> possiblePreconditions = new HashSet<FluentConstraint>();
				for (Fluent openFluent : openFluents)  {
					String oname = openFluent.getCompoundSymbolicVariable().getPredicateName();
					if(headName.equals(oname)) {
						// potential match. Add matches constraint.
						// TODO could be optimized by testing the full name.
						FluentConstraint preconstr = new FluentConstraint(FluentConstraint.Type.PRE, 
								pre.getConnections());
						preconstr.setFrom(openFluent);
						preconstr.setTo(taskfluent);
						preconstr.setNegativeEffect(pre.isNegativeEffect());
						possiblePreconditions.add(preconstr);
					}
				}
				if (possiblePreconditions.size() > 0) {
					constraints.add(possiblePreconditions);
				} else {
					return ret;  // Precondition can not be fulfilled!
				}
			}
		}
		
		// Create constraint networks as cartesian product of precondtion constraints
		Set<List<FluentConstraint>> combinations = Sets.cartesianProduct(constraints);
		for (List<FluentConstraint> comb : combinations) {
			ConstraintNetwork cn = new ConstraintNetwork(null);
			for (FluentConstraint con : comb) {
				cn.addConstraint(con);
				// add closes for negative effects
				if (con.isNegativeEffect() && this instanceof PFD0Operator) {
					FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
					closes.setFrom(con.getTo());
					closes.setTo(con.getFrom());
					cn.addConstraint(closes);
				}
			}
			
			// add positive effects/decomposition
			for (Constraint con : expandEffects(taskfluent, groundSolver)) {
				cn.addConstraint(con);
			}
			
			// Add a UNARYAPPLIED to remember which method/operator has been used.
			// UNARYAPPLIED is not needed anymore.
			FluentConstraint applicationconstr = new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, 
					this);
			applicationconstr.setFrom(taskfluent);
			applicationconstr.setTo(taskfluent);
			cn.addConstraint(applicationconstr);
			// add constraints for duration
			if (durationBounds != null) {
				AllenIntervalConstraint duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, durationBounds);
				duration.setFrom(taskfluent.getAllenInterval());
				duration.setTo(taskfluent.getAllenInterval());
				cn.addConstraint(duration);
			}
			ret.add(cn);
		}
		return ret;		
	}
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork after applying the operator/method.
	 */
	@Deprecated
	public abstract ConstraintNetwork expandTail(Fluent taskfluent, FluentNetworkSolver groundSolver);
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return Constraints representing the decompositions or positive effects
	 */
	public abstract List<Constraint> expandEffects(Fluent taskfluent, 
			FluentNetworkSolver groundSolver);
	
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
	
	/**
	 * 
	 * @return The bounds that will be used for the duration constraint of this task.
	 */
	public Bounds getDurationBounds() {
		return durationBounds;
	}

	/**
	 * 
	 * @param durationBounds The bounds that will be used for the duration constraint of this task.
	 */
	public void setDurationBounds(Bounds durationBounds) {
		this.durationBounds = durationBounds;
	}

}
