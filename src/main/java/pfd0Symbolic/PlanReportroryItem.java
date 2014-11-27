package pfd0Symbolic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

import com.google.common.collect.Sets;

public abstract class PlanReportroryItem {
	
	protected final String taskname;
	
	protected final PFD0Precondition[] preconditions;

	protected final String[] arguments;
	protected final EffectTemplate[] effects;
	
	protected Map<String, Map<String, Integer>> variableOccurrencesMap;
	protected Map<String,String[]> variablesPossibleValuesMap;
	
	public static final String HEAD_KEYWORD_STRING = "task";
	
	Logger logger = MetaCSPLogging.getLogger(PlanReportroryItem.class);
	
	/**
	 * The bounds that will be used for the duration constraint of this task.
	 */
	protected Bounds durationBounds;
	
	protected final List<ResourceUsageTemplate> resourceUsageIndicators = 
			new ArrayList<ResourceUsageTemplate>();
	
	public PlanReportroryItem(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			EffectTemplate[] effects) {
		this.taskname = taskname;
		this.arguments = arguments;
		this.preconditions = preconditions;
		this.effects = effects;
	}
	
	public void addResourceUsageTemplate(ResourceUsageTemplate rt) {
		resourceUsageIndicators.add(rt);
	}
	
	public void addResourceUsageTemplates(List<ResourceUsageTemplate> rtList) {
		resourceUsageIndicators.addAll(rtList);
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
	}
	
	public void setVariablesPossibleValuesMap(Map<String,String[]> map) {
		this.variablesPossibleValuesMap = map;
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
						FluentConstraint preCon = new FluentConstraint(FluentConstraint.Type.PRE, 
								pre.getConnections());
						preCon.setFrom(openFluent);
						preCon.setTo(taskFluent);
						preCon.setNegativeEffect(pre.isNegativeEffect());
						preCon.setAdditionalConstraints(pre.getAdditionalConstraints());
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
			
			Map<String, FluentConstraint> preconditionKeyToConstraint = 
					new HashMap<String, FluentConstraint>();  // TODO probably obsolete???
			
			Map<String, Variable> keyToFluentMap = new HashMap<String, Variable>();
//			keyToFluentMap.put(HEAD_STRING, taskFluent);
			for (EffectTemplate et : effects) {
				keyToFluentMap.put(et.getKey(), et.getPrototype());
			}
			
			// Add PRE and CLOSES constraints
			for (FluentConstraint con : comb) {
				cn.addConstraint(con);
				preconditionKeyToConstraint.put(constraintToPrecondition.get(con), con);
				keyToFluentMap.put(constraintToPrecondition.get(con), con.getFrom());
				
				// add closes for negative effects
				if (con.isNegativeEffect() && this instanceof PFD0Operator) {
					FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
					closes.setFrom(con.getTo());
					closes.setTo(con.getFrom());
					if (con.hasAdditionalConstraints()) {
						closes.setAdditionalConstraints(con.getAdditionalConstraints());
					}
					cn.addConstraint(closes);
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
						FluentConstraint preCon = preconditionKeyToConstraint.get(id);
						Fluent fl = null;
						if (preCon != null) {
							fl = (Fluent) preCon.getFrom();
						} else if (id.equals(HEAD_KEYWORD_STRING)){
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
			
			if (feasibleCN) {

				// add binding constraints between preconditions or effects
				for (Constraint con : createPreconditionsEffectsBindings(keyToFluentMap)) {
					cn.addConstraint(con);
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
								FluentConstraint preCon = preconditionKeyToConstraint.get(preconditionID);
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
				FluentConstraint applicationCon = 
						new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, this);
				applicationCon.setFrom(taskFluent);
				applicationCon.setTo(taskFluent);
				cn.addConstraint(applicationCon);

				// Add constraints for DURATION
				if (durationBounds != null) {
					AllenIntervalConstraint durationCon = 
							new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, durationBounds);
					durationCon.setFrom(taskFluent.getAllenInterval());
					durationCon.setTo(taskFluent.getAllenInterval());
					cn.addConstraint(durationCon);
				}

				// Add Constraints for RESOURCES
				for (ResourceUsageTemplate rt : this.resourceUsageIndicators) {
					FluentConstraint resourceCon = 
							new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
					resourceCon.setFrom(taskFluent);
					resourceCon.setTo(taskFluent);
					cn.addConstraint(resourceCon);
				}

				ret.add(cn);
			} else {
				logger.fine("Ommitting non-feasible CN");
			}
		}
		return ret;		
	}
	
	private void setVarsForAdditionalConstraints(Fluent taskFluent, Map<String, Variable> keyToFluentMap) {
		Vector<String> froms = new Vector<String>();
		Vector<String> tos = new Vector<String>();
		Vector<AllenIntervalConstraint> constraints = new Vector<AllenIntervalConstraint>();
		
		if (froms.size() != tos.size() || froms.size() != constraints.size()) {
			throw new IllegalStateException();
		}
		
		for (int i = 0; i < froms.size(); i++) {
			String fromKey = froms.get(i);
			String toKey = tos.get(i);
			if (!HEAD_KEYWORD_STRING.equals(fromKey) && !HEAD_KEYWORD_STRING.equals(toKey)) {
				Variable from = keyToFluentMap.get(fromKey);
				if (from == null) {
					throw new IllegalArgumentException("Error in Domain. No fluent for key " + fromKey);
				}
				Variable to = keyToFluentMap.get(fromKey);
				if (to == null) {
					throw new IllegalArgumentException("Error in Domain. No fluent for key " + toKey);
				}
				
				MultiBinaryConstraint con = (MultiBinaryConstraint) constraints.get(i);
				con.setFrom(from);
				con.setTo(to);
			} else if (HEAD_KEYWORD_STRING.equals(fromKey) && HEAD_KEYWORD_STRING.equals(toKey)) {    // HEAD -> HEAD
				MultiBinaryConstraint con = (MultiBinaryConstraint) constraints.get(i);
				con.setFrom(taskFluent);
				con.setTo(taskFluent);
			} else if (HEAD_KEYWORD_STRING.equals(fromKey) && !HEAD_KEYWORD_STRING.equals(toKey)) { // HEAD -> PRE
				// Add it to DC or PRe constraint
				
			}
			
		}
	}
	
	private List<Constraint> createPreconditionsEffectsBindings(Map<String, Variable> keyToFluentMap) {
		List<Constraint> ret = new ArrayList<Constraint>();
		if (variableOccurrencesMap != null) {
			for (Map<String, Integer> occurrence : variableOccurrencesMap.values()) {
				String[] occKeys = occurrence.keySet().toArray(new String[occurrence.keySet().size()]);
				for (int i = 0; i < occKeys.length; i++) {
					if (occKeys[i].equals(HEAD_KEYWORD_STRING)) {
						continue;
					}
					for (int j = i + 1; j < occKeys.length; j++) {
						if (occKeys[j].equals(HEAD_KEYWORD_STRING)) {
							continue;
						}
						// Create binding constraint
						int connections[] = new int[] {occurrence.get(occKeys[i]).intValue(),
								occurrence.get(occKeys[j]).intValue()};
						CompoundSymbolicValueConstraint bcon = new CompoundSymbolicValueConstraint(
								CompoundSymbolicValueConstraint.Type.SUBMATCHES, 
								connections);

						// set from
						Variable from = keyToFluentMap.get(occKeys[i]);
						if (from != null) {
							if (from instanceof Fluent) {
								bcon.setFrom(((Fluent) from).getCompoundSymbolicVariable());
							} else {  // Variable Prototype (will be set in addResolverSub)
								bcon.setFrom(from);
							}
						} else {
							logger.fine("No fluent found for key " + occKeys[i]);
							continue;
						}

						// set to
						Variable to = keyToFluentMap.get(occKeys[j]);
						if (to != null) {
							if (to instanceof Fluent) {
								bcon.setTo(((Fluent) to).getCompoundSymbolicVariable());
							} else {  // Variable Prototype (will be set in addResolverSub)
								bcon.setTo(to);
							}
						} else {
							logger.fine("No fluent found for key " + occKeys[j]);
							continue;
						}

						ret.add(bcon);
					}
				}
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
