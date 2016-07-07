package htn;

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
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import com.google.common.collect.Sets;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.TaskApplicationMetaConstraint.markings;
import hybridDomainParsing.SubDifferentDefinition;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

public abstract class PlanReportroryItem {
	
	private static final boolean MULT_ACTION_NEG_EFFECT = false;
	
	private static int EXPAND_COUNT = 0;
	private static long time_sum = 0;
	
	protected final String taskname;
	
	protected final HTNPrecondition[] preconditions;

	protected final String[] arguments;
	protected final EffectTemplate[] effects;
	protected AdditionalConstraintTemplate[] additionalConstraints;
	
	protected Map<String, Map<String, Integer>> variableOccurrencesMap;
	protected Map<String,String[]> variablesPossibleValuesMap;
	protected Map<String,String[]> variablesImpossibleValuesMap;
	protected SubDifferentDefinition[] subDifferentDefinitions;
	
	public static final String HEAD_KEYWORD_STRING = "task";
	
	Logger logger = MetaCSPLogging.getLogger(PlanReportroryItem.class);
	
	/**
	 * The bounds that will be used for the duration constraint of this task.
	 */
	protected Bounds durationBounds;
	
	protected final List<ResourceUsageTemplate> resourceUsageIndicators = 
			new ArrayList<ResourceUsageTemplate>();
	
	public PlanReportroryItem(String taskname, String[] arguments, HTNPrecondition[] preconditions, 
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
	
	public void setAdditionalConstraints(AdditionalConstraintTemplate[] additionalConstraints) {
		this.additionalConstraints = additionalConstraints;
	}

	public String getName() {
		return this.taskname;
	}
	
	@Override
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
	
	public void setVariablesImpossibleValuesMap(Map<String,String[]> map) {
		this.variablesImpossibleValuesMap = map;
	}

	public void setSubDifferentDefinitions(SubDifferentDefinition[] subDifferentDefinitions) {
		this.subDifferentDefinitions = subDifferentDefinitions;
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
			for (HTNPrecondition pre : preconditions) {
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
			for (HTNPrecondition pre : this.preconditions) {
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
	public List<ConstraintNetwork> expandOneShot(Fluent taskFluent, int depth, FluentNetworkSolver groundSolver, Fluent[] openFluents) {
		long startTime = System.nanoTime();
		long t;
		EXPAND_COUNT++;
//		System.out.println("ExpandCount: " + EXPAND_COUNT);
		List<ConstraintNetwork> ret = new ArrayList<ConstraintNetwork>();
		
		List<Set<FluentConstraint>> fluentConstraints = new ArrayList<Set<FluentConstraint>>();
		
		Map<FluentConstraint, String> constraintToPrecondition = 
				new HashMap<FluentConstraint, String>();
		
		// Create set of potential precondition constraints.
		if(this.preconditions != null) {
			for (HTNPrecondition pre : this.preconditions) {
				String preName = pre.getFluenttype();
				Set<FluentConstraint> possiblePreconditions = new HashSet<FluentConstraint>();
				for (Fluent openFluent : openFluents)  {
					String oName = openFluent.getCompoundSymbolicVariable().getPredicateName();
					if(preName.equals(oName)) {
						if (testArguments(taskFluent, pre, openFluent)) {
							/// For negative effects the fluent should not be closed
							if (!MULT_ACTION_NEG_EFFECT) {
								if (pre.isNegativeEffect()) {
									if (openFluent.getMarking() == markings.CLOSED) {
//										System.out.println("Fluent is already closed");
										continue;
									}
								}
							}
							
//							System.out.println("true: " + taskFluent.getCompoundSymbolicVariable() + " " + openFluent.getCompoundSymbolicVariable() + " " + Arrays.toString(pre.getConnections()));
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
//						else {
////							System.out.println("false" + taskFluent.getCompoundSymbolicVariable() + " " + openFluent.getCompoundSymbolicVariable() + " " + Arrays.toString(pre.getConnections()));
//						}
					}
				}
				if (possiblePreconditions.size() > 0) {
					fluentConstraints.add(possiblePreconditions);
				} else {
					return ret;  // Precondition cannot be fulfilled!
				}
			}
		}
		
		long td_add = 0;
		
		// Create constraint networks as cartesian product of precondition constraints
		Set<List<FluentConstraint>> combinations = Sets.cartesianProduct(fluentConstraints);

//		System.out.println("combinations.size: " + combinations.size());
//		System.out.println("Computing combinations took: " + ((System.nanoTime() - startTime) / 1000000) + " ms");
		
		// Create Constraint networks
		for (List<FluentConstraint> comb : combinations) {
			
			Map<String, Fluent> preKeyToFluentMap = new HashMap<String, Fluent>();
			Map<String, Variable> effKeyToVariableMap = new HashMap<String, Variable>();
//			keyToFluentMap.put(HEAD_STRING, taskFluent);
			for (EffectTemplate et : effects) {
				effKeyToVariableMap.put(et.getKey(), et.getPrototype());
			}
			
			for (FluentConstraint con : comb) {
				preKeyToFluentMap.put(constraintToPrecondition.get(con), (Fluent) con.getFrom());
			}
			
			// Analyze if values can possibly match
			// by calculating the intersection of possible values of involved head and preconditions.
			boolean feasibleCN = true;
			if (variableOccurrencesMap != null) {
				for (Entry<String, Map<String, Integer>> occs : variableOccurrencesMap.entrySet()) {
					Set<String> possibleValues = null;
					
					if (variablesPossibleValuesMap != null && variablesPossibleValuesMap.size() > 0) {
						String[] positiveValues = variablesPossibleValuesMap.get(occs.getKey());
						if (positiveValues != null) {
							possibleValues = new HashSet<String>(Arrays.asList(positiveValues));
						}
					}
					
					for (Entry<String, Integer> occ : occs.getValue().entrySet()) {
						String id = occ.getKey();
						Fluent fl = null;
						if (id.equals(HEAD_KEYWORD_STRING)) {
							fl = taskFluent;
						} else {
							fl = preKeyToFluentMap.get(id);
						}

						if (fl != null) { // head or pre-Key but not an effect key:
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
						// subtract impossiblesymbols
						if (variablesImpossibleValuesMap != null && variablesImpossibleValuesMap.size() > 0) {
							String[] negativeValues = variablesImpossibleValuesMap.get(occs.getKey());
							if (negativeValues != null) {
								possibleValues.removeAll(Arrays.asList(negativeValues));
							}
						}
						
						if (possibleValues.isEmpty()) {
							feasibleCN = false;
							break;
						}
					}
				}
			}
			
			if (feasibleCN) {
				
				// Add PRE and CLOSES constraints
				long td1 = System.nanoTime();
				ConstraintNetwork cn = new ConstraintNetwork(null);
				for (FluentConstraint con : comb) {
					cn.addConstraint(con);
					
					// add closes for negative effects
					if (con.isNegativeEffect() && this instanceof HTNOperator) {
						FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
						closes.setFrom(con.getTo());
						closes.setTo(con.getFrom());
						if (con.hasAdditionalConstraints()) {
							closes.setAdditionalConstraints(con.getAdditionalConstraints());
						}
						cn.addConstraint(closes);
					}
				}
				td_add += (System.nanoTime() - td1);

				// add binding constraints between preconditions or effects
				for (Constraint con : createPreconditionsEffectsBindings(preKeyToFluentMap, effKeyToVariableMap)) {
					cn.addConstraint(con);
				}

				// add positive effects/decomposition
				for (Constraint con : expandEffectsOneShot(taskFluent, groundSolver)) {
					cn.addConstraint(con);
				}
				
				// add additional constraints between preconditions and effects
				for (AllenIntervalConstraint aCon : setVarsInAdditionalConstraints(taskFluent, preKeyToFluentMap, effKeyToVariableMap)) {
					cn.addConstraint(aCon);
				}
				
				try {
					for (Constraint con : createSubDifferents(taskFluent, preKeyToFluentMap, effKeyToVariableMap)) {
						cn.addConstraint(con);
					}
				} catch (IllegalStateException e) {
					continue;
				}
				
				// add VALUERESTRICTION constraints for task, preconditions and effects
				for (Constraint con : createValueRestrictions(taskFluent, preKeyToFluentMap, effKeyToVariableMap)) {
					cn.addConstraint(con);
				}

				// Add a UNARYAPPLIED to remember which method/operator has been used.
				FluentConstraint applicationCon = 
						new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, this);
				applicationCon.setFrom(taskFluent);
				applicationCon.setTo(taskFluent);
				applicationCon.setDepth(depth);
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
				logger.finest("Ommitting non-feasible CN");
			}
		}
//		System.out.println("td_add = " + td_add / 1000000 + " ms");
//		long diff = System.nanoTime() - startTime;
//		System.out.println("Full Took "+((diff) / 1000000) + " ms"); 
//		time_sum += diff;
//		System.out.println("Full Took in sum "+((time_sum) / 1000000) + " ms"); 
		return ret;		
	}
	
	private boolean testArguments(Fluent taskFluent, HTNPrecondition pre,
			Fluent openFluent) {
		int[] connections = pre.getConnections();
		if (connections.length > 0) {
			CompoundSymbolicVariable task_symb = taskFluent.getCompoundSymbolicVariable();
			return openFluent.getCompoundSymbolicVariable().possibleArgumentsMatch(task_symb, connections);
		}
		
		return true;
	}

	/**
	 * Goes trough templates of additional constraints, creates cloned versions of the constraints and sets the variables.
	 * @param taskFluent The fluent of the current task.
	 * @param keyToFluentMap Map of keys to fluent variables.
	 */
	private List<AllenIntervalConstraint> setVarsInAdditionalConstraints(Fluent taskFluent, 
			Map<String, Fluent> preKeyToFluentMap, Map<String, Variable> effKeyToVariableMap) {
		List<AllenIntervalConstraint> ret = new ArrayList<AllenIntervalConstraint>();
		
		for (AdditionalConstraintTemplate act : additionalConstraints) {
			if (act.withoutHead()) {
				String fromKey = act.getFromKey();
				Variable from = null;
				if (preKeyToFluentMap.containsKey(fromKey)) {
					from = preKeyToFluentMap.get(fromKey).getAllenInterval();
				} else if (effKeyToVariableMap.containsKey(fromKey)) {
					from = effKeyToVariableMap.get(fromKey);
				} else {
					throw new IllegalArgumentException("Error in Domain. No fluent for key " 
							+ fromKey + " in " + taskname );
				}
				
				String toKey = act.getToKey();
				Variable to = null;
				if (preKeyToFluentMap.containsKey(toKey)) {
					to = preKeyToFluentMap.get(toKey).getAllenInterval();
				} else if (effKeyToVariableMap.containsKey(toKey)) {
					to = effKeyToVariableMap.get(toKey);
				} else {
					throw new IllegalArgumentException("Error in Domain. No fluent for key " 
							+ toKey + " in " + taskname );
				}
				
				AllenIntervalConstraint newCon = (AllenIntervalConstraint) act.getConstraint().clone();
				newCon.setFrom(from);
				newCon.setTo(to);
				ret.add(newCon);
			} else if (act.headToHead()) {    // HEAD -> HEAD
				AllenIntervalConstraint newCon = (AllenIntervalConstraint) act.getConstraint().clone();
				newCon.setFrom(taskFluent.getAllenInterval());
				newCon.setTo(taskFluent.getAllenInterval());
				ret.add(newCon);
			}
		}
		return ret;
	}
	
	private List<Constraint> createValueRestrictions(Fluent taskFluent, 
			Map<String, Fluent> preKeyToFluentMap, Map<String, Variable> effKeyToVariableMap) {
		List<Constraint> ret = new ArrayList<Constraint>();
		// add VALUERESTRICTION constraints for task, preconditions and effects
		if (variableOccurrencesMap != null && 
				(variablesPossibleValuesMap.size() > 0 || variablesImpossibleValuesMap.size() > 0) ) {
			// go through all variables
			for (Entry<String, Map<String, Integer>> occs : variableOccurrencesMap.entrySet()) {
				String[] possibleValues = variablesPossibleValuesMap.get(occs.getKey()); 
				String[] impossibleValues = variablesImpossibleValuesMap.get(occs.getKey());
				if (possibleValues != null || impossibleValues != null) {
					String[][] positiveRestrictions = new String[][] {possibleValues};
					String[][] negativeRestrictions = new String[][] {impossibleValues};

					// go though all occurrences of that variable
					for (Entry<String, Integer> occ : occs.getValue().entrySet()) {

						Variable var = findVariable(occ.getKey(), taskFluent, preKeyToFluentMap, effKeyToVariableMap);
						int[] indices = new int[] {occ.getValue().intValue()};
						
						if (possibleValues != null) {
							// TODO merge multiple constraints into one.
							CompoundSymbolicValueConstraint rcon = 
									new CompoundSymbolicValueConstraint(
											CompoundSymbolicValueConstraint.Type.POSITIVEVALUERESTRICTION, 
											indices, 
											positiveRestrictions);
							rcon.setFrom(var);
							rcon.setTo(var);
							ret.add(rcon);
						}
						
						if (impossibleValues != null) {
							// TODO merge multiple constraints into one.
							CompoundSymbolicValueConstraint rcon = 
									new CompoundSymbolicValueConstraint(
											CompoundSymbolicValueConstraint.Type.NEGATIVEVALUERESTRICTION, 
											indices, 
											negativeRestrictions);
							rcon.setFrom(var);
							rcon.setTo(var);
							ret.add(rcon);
						}
					}
				}
			}
		}
		return ret;
	}
	
	private Variable findVariable(String id, Fluent taskFluent, 
			Map<String, Fluent> preKeyToFluentMap, Map<String, Variable> effKeyToVariableMap) {
		Variable var = null;
		if (id.equals(HEAD_KEYWORD_STRING)) {
			var = taskFluent;
		} else if (preKeyToFluentMap.containsKey(id)){
			var = preKeyToFluentMap.get(id);
		} else {
			var = effKeyToVariableMap.get(id);
		}

		if (var == null) {
			throw new IllegalArgumentException("Error in Domain. No fluent for key " 
					+ id + " in " + taskname );
		}

		// if it is a fluent we set it directly to the compound variable
		// if it is a prototype we do that in addResolverSub
		if (var instanceof Fluent) {
			var = ((Fluent) var).getCompoundSymbolicVariable();
		}
		return var;
	}
	
	private List<Constraint> createSubDifferents(Fluent taskFluent, 
			Map<String, Fluent> preKeyToFluentMap, Map<String, Variable> effKeyToVariableMap) {
		List<Constraint> ret = new ArrayList<Constraint>();
		
		if (variableOccurrencesMap != null && subDifferentDefinitions != null) {
			for (SubDifferentDefinition diff : subDifferentDefinitions) {
				String fromKey = diff.getFromKey();
				String toKey = diff.getToKey();
				
				Map<String, Integer> fromOccs = variableOccurrencesMap.get(fromKey);
				Map<String, Integer> toOccs = variableOccurrencesMap.get(toKey);
				for (Entry<String, Integer> fromEntry : fromOccs.entrySet()) {
					
					Variable fromVar = findVariable(fromEntry.getKey(), taskFluent, preKeyToFluentMap, 
							effKeyToVariableMap);

					int fromIndex = fromEntry.getValue().intValue();
					for (Entry<String, Integer> toEntry :toOccs.entrySet()) {
						Variable toVar = findVariable(toEntry.getKey(), taskFluent, preKeyToFluentMap, 
							effKeyToVariableMap);
						// Create SUBMATCHES constraint
						int[] connections = new int[] {fromIndex, toEntry.getValue().intValue()};
						if (fromVar instanceof CompoundSymbolicVariable && toVar instanceof CompoundSymbolicVariable) {
 							if (! ((CompoundSymbolicVariable) fromVar).possibleArgumentDifferent((CompoundSymbolicVariable) toVar, connections[0], connections[1])) {
								throw new IllegalStateException();
							}
						}
						CompoundSymbolicValueConstraint scon = new CompoundSymbolicValueConstraint(
								CompoundSymbolicValueConstraint.Type.SUBDIFFERENT, 
								connections);
						scon.setFrom(fromVar);
						scon.setTo(toVar);
						ret.add(scon);
					}
				}
			}

		}
		return ret;
	}
	
	
	
	private List<Constraint> createPreconditionsEffectsBindings(Map<String, Fluent> preKeyToFluentMap, 
			Map<String, Variable> effKeyToVariableMap) {
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
						Variable from = preKeyToFluentMap.get(occKeys[i]); // try precondition keys
						if (from == null) {
							from = effKeyToVariableMap.get(occKeys[i]);          // else try effect keys
						}
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
						Variable to = preKeyToFluentMap.get(occKeys[j]); // try precondition keys
						if (to == null) {
							to = effKeyToVariableMap.get(occKeys[j]);        // else try effect keys
						}
						
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
