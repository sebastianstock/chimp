package pfd0Symbolic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

import com.google.common.collect.Sets;

public abstract class PlanReportroryItem {
	
	protected final String taskname;
	
	protected final PFD0Precondition[] preconditions;

	protected final String[] arguments;
	protected Map<String, VariablePrototype> effectsMap;
	
	protected Map<String, Map<String, Integer>> variableOccurrencesMap;
	protected Map<String,String[]> variablesPossibleValuesMap;
	
	Logger logger = MetaCSPLogging.getLogger(PlanReportroryItem.class);
	
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

	
	public void setVariableOccurrencesMap(
			Map<String, Map<String, Integer>> variableOccurrencesMap) {
		this.variableOccurrencesMap = variableOccurrencesMap;
		System.out.println("VARIABLEOCCURRENCESMAP:\n" + variableOccurrencesMap);
	}
	
	public void setVariablesPossibleValuesMap(Map<String,String[]> map) {
		this.variablesPossibleValuesMap = map;
		System.out.println("VARIABLESPOSSIBLEVALUESMAP:\n" + variablesPossibleValuesMap);
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

	
	@Deprecated // only used when we have three meta-constraints by TaskSelectionMetaConstraint
	/**
	 * Creates the dummy preconditions for a task + Adds Duration constraint
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
		// Add a UNARYAPPLIED to remember which method/operator has been used.
		FluentConstraint applicationconstr = new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, 
				this);
		applicationconstr.setFrom(taskfluent);
		applicationconstr.setTo(taskfluent);
		ret.addConstraint(applicationconstr);
		
		if (durationBounds != null) {
			AllenIntervalConstraint duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, durationBounds);
			duration.setFrom(taskfluent.getAllenInterval());
			duration.setTo(taskfluent.getAllenInterval());
			ret.addConstraint(duration);
		}
		return ret;		
	}
	
	/**
	 * Expands preconditions and effects of a task + Adds Duration constraint
	 * @param taskFluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return The resulting ConstraintNetwork.
	 */
	public List<ConstraintNetwork> expandOneShot(Fluent taskFluent, FluentNetworkSolver groundSolver, Fluent[] openFluents) {
		List<ConstraintNetwork> ret = new ArrayList<ConstraintNetwork>();
		
		List<Set<FluentConstraint>> fluentConstraints = new ArrayList<Set<FluentConstraint>>();
		
		Map<FluentConstraint, String> constraintToPrecondition = 
				new HashMap<FluentConstraint, String>();
		
		// Create set of potential precondition constraints.
		if(this.preconditions != null) {
			for (PFD0Precondition pre : this.preconditions) {
				String preName = pre.getFluenttype();
				Set<FluentConstraint> possiblePreconditions = new HashSet<FluentConstraint>();
				for (Fluent openFluent : openFluents)  {
					String oName = openFluent.getCompoundSymbolicVariable().getPredicateName();
					if(preName.equals(oName)) {
						// potential match. Add PRE constraint.
						// TODO could be optimized by testing the full name.
						FluentConstraint preCon = new FluentConstraint(FluentConstraint.Type.PRE, 
								pre.getConnections());
						preCon.setFrom(openFluent);
						preCon.setTo(taskFluent);
						preCon.setNegativeEffect(pre.isNegativeEffect());
						possiblePreconditions.add(preCon);
						constraintToPrecondition.put(preCon, pre.getKey());
					}
				}
				if (possiblePreconditions.size() > 0) {
					fluentConstraints.add(possiblePreconditions);
				} else {
					return ret;  // Precondition cannot be fulfilled!
				}
			}
		}
		
		// Create constraint networks as cartesian product of precondition constraints
		Set<List<FluentConstraint>> combinations = Sets.cartesianProduct(fluentConstraints);
		
		// Create Constraint networks
		for (List<FluentConstraint> comb : combinations) {
			ConstraintNetwork cn = new ConstraintNetwork(null);
			
			Map<String, FluentConstraint> preconditionToConstraint = 
					new HashMap<String, FluentConstraint>();
			
			// Add PRE and CLOSES constraints
			for (FluentConstraint con : comb) {
				cn.addConstraint(con);
				preconditionToConstraint.put(constraintToPrecondition.get(con), con);
				
				// add closes for negative effects
				if (con.isNegativeEffect() && this instanceof PFD0Operator) {
					FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
					closes.setFrom(con.getTo());
					closes.setTo(con.getFrom());
					cn.addConstraint(closes);
				}
			}
			
			// add binding constraints between preconditions or effects
			if (variableOccurrencesMap != null) {
				for (Map<String, Integer> occurrence : variableOccurrencesMap.values()) {
					String[] keys = occurrence.keySet().toArray(new String[occurrence.keySet().size()]);
					for (int i = 0; i < keys.length; i++) {
						if (keys[i].equals("head")) {
							continue;
						}
						for (int j = i + 1; j < keys.length; j++) {
							if (keys[j].equals("head")) {
								continue;
							}
							// Create binding constraint
							int connections[] = new int[] {occurrence.get(keys[i]).intValue(),
									occurrence.get(keys[j]).intValue()};
							CompoundSymbolicValueConstraint bcon = new CompoundSymbolicValueConstraint(
									CompoundSymbolicValueConstraint.Type.SUBMATCHES, 
									connections);
							
							if (preconditionToConstraint.containsKey(keys[i])) {
								Fluent from = (Fluent) preconditionToConstraint.get(keys[i]).getFrom();
								bcon.setFrom(from.getCompoundSymbolicVariable());
							} else if (effectsMap != null && effectsMap.containsKey(keys[i])){
								bcon.setFrom(effectsMap.get(keys[i]));
							} else {
								logger.fine("No effect found for key " + keys[i]);
								continue;
							}
							
							if (preconditionToConstraint.containsKey(keys[j])) {
								Fluent to = (Fluent) preconditionToConstraint.get(keys[j]).getFrom();
								bcon.setTo(to.getCompoundSymbolicVariable());
							} else if (effectsMap != null && effectsMap.containsKey(keys[j])){
								bcon.setTo(effectsMap.get(keys[j]));
							} else {
								logger.fine("No effect found for key " + keys[j]);
								continue;
							}
							System.out.println("FROM: " +keys[i]);
							System.out.println("TO: " +keys[j]);
							cn.addConstraint(bcon);
						}
					}
				}
			}
			
			// Analyze if values can possibly match
			// by calculating the intersection of possible values of involved head and preconditions.
			boolean feasibleCN = true;
			if (variableOccurrencesMap != null) {
				for (Entry<String, Map<String, Integer>> occs : variableOccurrencesMap.entrySet()) {
					Set<String> possibleValues = null;
					
					if (variablesPossibleValuesMap != null) {
						String[] restrictedValues = variablesPossibleValuesMap.get(occs.getKey());
						if (restrictedValues != null) {
							possibleValues = new HashSet<String>(Arrays.asList(restrictedValues));
						}
					}
					
					for (Entry<String, Integer> occ : occs.getValue().entrySet()) {
						String id = occ.getKey();
						FluentConstraint preCon = preconditionToConstraint.get(id);
						Fluent fl = null;
						if (preCon != null) {
							fl = (Fluent) preCon.getFrom();
						} else if (id.equals("head")){
							fl = taskFluent;
						}
						if (fl != null) {
							List<String> varSymbolsList = Arrays.asList(
									fl.getCompoundSymbolicVariable().getSymbolsAt(occ.getValue().intValue() + 1));
							if (possibleValues == null) {
								possibleValues = new HashSet<String>(varSymbolsList);
							} else {   // remove objects that are no possible symbols of fl
								possibleValues.retainAll(varSymbolsList);
							}
						}
					}
					if (possibleValues != null) {
						if (possibleValues.isEmpty()) {
							feasibleCN = false;
						}
					}
				}
			}
			
			
			// add positive effects/decomposition
			for (Constraint con : expandEffectsOneShot(taskFluent, groundSolver)) {
				cn.addConstraint(con);
			}
			
			// add VALUERESTRICTION constraints for preconditions
			if (variableOccurrencesMap != null && variablesPossibleValuesMap != null) {
				for (Entry<String, Map<String, Integer>> occs : variableOccurrencesMap.entrySet()) {
					String[] possibleValues = variablesPossibleValuesMap.get(occs.getKey());
					if (possibleValues != null) {
						for (Entry<String, Integer> e : occs.getValue().entrySet()) {
							String preconditionID = e.getKey();
							FluentConstraint preCon = preconditionToConstraint.get(preconditionID);
							if (preCon != null) {
								int[] indices = new int[] {e.getValue().intValue()};
								String[][] restrictions = new String[][] {possibleValues};
								// TODO merge multiple constraints into one.
								CompoundSymbolicValueConstraint rcon = 
										new CompoundSymbolicValueConstraint(
												CompoundSymbolicValueConstraint.Type.VALUERESTRICTION, 
												indices, 
												restrictions);
								CompoundSymbolicVariable var = 
										((Fluent) preCon.getFrom()).getCompoundSymbolicVariable();
								rcon.setFrom(var);
								rcon.setTo(var);
								cn.addConstraint(rcon);
								logger.fine("ADDED VALUERESTRICTION");
							}
						}
					}
				}
			}
			
			// Add a UNARYAPPLIED to remember which method/operator has been used.
			// UNARYAPPLIED is not needed anymore.
			FluentConstraint applicationconstr = new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, 
					this);
			applicationconstr.setFrom(taskFluent);
			applicationconstr.setTo(taskFluent);
			cn.addConstraint(applicationconstr);
			// add constraints for duration
			if (durationBounds != null) {
				AllenIntervalConstraint duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, durationBounds);
				duration.setFrom(taskFluent.getAllenInterval());
				duration.setTo(taskFluent.getAllenInterval());
				cn.addConstraint(duration);
			}
			if (feasibleCN) {  // TODO can be aborted earlier if false!
				ret.add(cn);
			} else {
				logger.fine("Ommitting non-feasible CN");
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
	@Deprecated
	public abstract ConstraintNetwork expandOnlyTail(Fluent taskfluent, FluentNetworkSolver groundSolver);
	
	/**
	 * Applies the method or operator to one task.
	 * @param taskfluent The task that has to be expanded.
	 * @param groundSolver The groundSolver.
	 * @return Constraints representing the decompositions or positive effects
	 */
	public abstract List<Constraint> expandEffectsOneShot(Fluent taskfluent, 
			FluentNetworkSolver groundSolver);
	
	/**
	 * Creates the connections array for the opens constraint.
	 * 
	 * Looks at the arguments of effects and operator to see if they should have an equal constraint.
	 * @param prototypeargs Arguments of the effect.
	 * @return Array representing the connections.
	 */
	protected int[] createConnections(String[] prototypeargs) {
		List<Integer> connections = new ArrayList<Integer>();
		for (int i = 0; i < prototypeargs.length; i++) {
			for (int j = 0; j < arguments.length; j++) {
				if (prototypeargs[i].startsWith("?") && prototypeargs[i].equals(this.arguments[j])) {
					connections.add(new Integer(j));
					connections.add(new Integer(i));
				}
			}
		}
		int[] ret = new int[connections.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = connections.get(i).intValue();
		}
		return ret;
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
