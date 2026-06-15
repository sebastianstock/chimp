package htn;

import com.google.common.collect.Sets;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.SubDifferentDefinition;
import integers.IntegerConstraint;
import integers.IntegerVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import chimp.adapter.metacsp.MetaCspTemporalConstraintAdapter;
import chimp.adapter.metacsp.MetaCspIntegerConstraintAdapter;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import chimp.model.ResourceRequirement;
import chimp.adapter.metacsp.MetaCspResourceRequirementAdapter;
import chimp.adapter.metacsp.MetaCspResolverAdapter;
import chimp.model.FluentPrototypeDescription;
import chimp.model.FluentSignature;
import chimp.model.IntegerArgumentDescription;
import chimp.model.IntegerConstraintKind;
import chimp.model.ProblemMarking;
import chimp.model.ResolverArgumentReference;
import chimp.model.ResolverDescription;
import chimp.model.ResolverIntegerConstraintDescription;
import chimp.model.ResolverOrderingEdge;
import chimp.model.ResolverSemanticKind;
import chimp.model.ResolverTemporalConstraintDescription;
import chimp.model.SymbolicConstraintDescription;
import chimp.model.SymbolicRelationKind;
import org.metacsp.utility.logging.MetaCSPLogging;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class PlanReportroryItem {
	private static final MetaCspTemporalConstraintAdapter TEMPORAL_ADAPTER =
			new MetaCspTemporalConstraintAdapter();
	private static final MetaCspIntegerConstraintAdapter INTEGER_ADAPTER =
			new MetaCspIntegerConstraintAdapter();
	private static final MetaCspResourceRequirementAdapter RESOURCE_ADAPTER =
			new MetaCspResourceRequirementAdapter();
	private static final MetaCspResolverAdapter RESOLVER_ADAPTER =
			new MetaCspResolverAdapter();
	
	private static final boolean MULT_ACTION_NEG_EFFECT = false;
	
	static final AtomicInteger NEXT_ID = new AtomicInteger(0);
    private final int id = NEXT_ID.getAndIncrement();
	
	protected final String taskname;
	protected final int preferenceWeight;
	
	protected final HTNPrecondition[] preconditions;

	protected final String[] arguments;
	protected final IntArg[] integerArguments;
	protected final EffectTemplate[] effects;
	protected AdditionalConstraintTemplate[] additionalConstraints;
	protected IntegerConstraintTemplate[] integerConstraintTemplates;
	
	protected Map<String, Map<String, Integer>> variableOccurrencesMap;
	protected Map<String,String[]> variablesPossibleValuesMap = new HashMap<>();
	protected Map<String,String[]> variablesImpossibleValuesMap;
	protected SubDifferentDefinition[] subDifferentDefinitions;

	protected Map<String, Map<String, Integer>> integerVariableOccurrencesMap;

	protected List<String> symbolicConstants = new ArrayList<>();
	
	public static final String HEAD_KEYWORD_STRING = "task";
	
	Logger logger = MetaCSPLogging.getLogger(PlanReportroryItem.class);
	
	/**
	 * The bounds that will be used for the duration constraint of this task.
	 */
	protected TemporalBounds durationBounds;
	
	protected final List<ResourceRequirement> resourceUsageIndicators =
			new ArrayList<ResourceRequirement>();

	public PlanReportroryItem(String taskname, String[] arguments, IntArg[] intArgs, HTNPrecondition[] preconditions,
							  EffectTemplate[] effects, int preferenceWeight) {
		this.taskname = taskname;
		this.arguments = arguments;
		this.integerArguments = intArgs;
		this.preconditions = preconditions;
		this.effects = effects;
		this.preferenceWeight = preferenceWeight;
		createVariableOccurrencesMap();
		createIntegerVariableOccurrencesMap();
	}

	public PlanReportroryItem(String taskname, String[] arguments, HTNPrecondition[] preconditions, 
			EffectTemplate[] effects, int preferenceWeight) {
		this(taskname, arguments, new IntArg[0], preconditions, effects, preferenceWeight);
	}
	
	public void addResourceRequirement(ResourceRequirement rt) {
		resourceUsageIndicators.add(rt);
	}
	
	public void addResourceRequirements(List<ResourceRequirement> rtList) {
		resourceUsageIndicators.addAll(rtList);
	}
	
	public void setAdditionalConstraints(AdditionalConstraintTemplate[] additionalConstraints) {
		this.additionalConstraints = additionalConstraints;
	}

	public void setIntegerConstraintTemplates(IntegerConstraintTemplate[] integerConstraintTemplates) {
		this.integerConstraintTemplates = integerConstraintTemplates;
	}

	public String getName() {
		return this.taskname;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	private void createVariableOccurrencesMap() {
		variableOccurrencesMap = new HashMap<>();
		addVariableOccurrences(arguments, HEAD_KEYWORD_STRING);
		for (HTNPrecondition pre : preconditions) {
			addVariableOccurrences(pre.getArguments(), pre.getKey());
		}
		for (EffectTemplate et : effects) {
			addVariableOccurrences(et.getInputArgs(), et.getKey());
		}
	}

	protected void addVariableOccurrences(String[] argStrings, String key) {
		for (int i = 0; i < argStrings.length;  i++) {
			if (argStrings[i].startsWith(HybridDomain.VARIABLE_INDICATOR)) {
				Map<String, Integer> occ = variableOccurrencesMap.get(argStrings[i]);
				if (occ == null) {
					occ = new HashMap<String, Integer>();
					variableOccurrencesMap.put(argStrings[i], occ);
				}
				occ.put(key, Integer.valueOf(i));
			} else {
				// It is a constant -> create dummy variable name to insert it into variableOccurrencesMap and add it to possible values
				String dummyVarName = key + "_v" + i;
				Map<String, Integer> occ = new HashMap<>();
				occ.put(key, Integer.valueOf(i));
				variableOccurrencesMap.put(dummyVarName, occ);
				variablesPossibleValuesMap.put(dummyVarName, new String[] {argStrings[i]});
				symbolicConstants.add(argStrings[i]);
			}
		}
	}

	private void createIntegerVariableOccurrencesMap() {
		integerVariableOccurrencesMap = new HashMap<>();
		addIntegerVariableOccurrences(integerArguments, HEAD_KEYWORD_STRING);
		for (HTNPrecondition pre : preconditions) {
			addIntegerVariableOccurrences(pre.getIntegerArguments(), pre.getKey());
		}
		for (EffectTemplate et : effects) {
			addIntegerVariableOccurrences(et.getIntArgs(), et.getKey());
		}
	}

	protected void addIntegerVariableOccurrences(IntArg[] intArgs, String key) {
		if (intArgs == null) {
			return;
		}
		for (int i = 0; i < intArgs.length;  i++) {
			if (intArgs[i].isVariable()) {
				Map<String, Integer> occ = integerVariableOccurrencesMap.get(intArgs[i].varName);
				if (occ == null) {
					occ = new HashMap<String, Integer>();
					integerVariableOccurrencesMap.put(intArgs[i].varName, occ);
				}
				occ.put(key, Integer.valueOf(i));
			}
		}
	}

	public void setVariableOccurrencesMap(
			Map<String, Map<String, Integer>> variableOccurrencesMap) {
		this.variableOccurrencesMap = variableOccurrencesMap;
	}
	
	public void setVariablesPossibleValuesMap(Map<String,String[]> map) {
		this.variablesPossibleValuesMap = map;
	}

	public void addVariablesPossibleValues(Map<String, String[]> map) {
		this.variablesPossibleValuesMap.putAll(map);
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
	
	/**
	 * Describes feasible resolver candidates without materializing Meta-CSP resolver networks.
	 * @param taskFluent The task that has to be expanded.
	 * @param depth The current depth of the task fluent in the plan's hierarchy.
	 * @param openFluents All fluents that are open.
	 * @return Neutral descriptions of feasible resolver candidates.
	 */
	public List<ResolverDescription> describeResolvers(
			Fluent taskFluent, int depth, Fluent[] openFluents) {
		List<Set<PreconditionSelection>> possibleSelections = new ArrayList<>();
		if (preconditions != null) {
			for (HTNPrecondition precondition : preconditions) {
				Set<PreconditionSelection> matching = new HashSet<>();
				for (Fluent openFluent : openFluents) {
					if (precondition.getFluenttype().equals(
							openFluent.getCompoundSymbolicVariable().getPredicateName())
							&& testArguments(taskFluent, precondition, openFluent)
							&& (!precondition.isNegativeEffect()
									|| MULT_ACTION_NEG_EFFECT
									|| openFluent.getMarking() != HTNMetaConstraint.markings.CLOSED)) {
						matching.add(new PreconditionSelection(precondition, openFluent));
					}
				}
				if (matching.isEmpty()) {
					return List.of();
				}
				possibleSelections.add(matching);
			}
		}

		List<ResolverDescription> descriptions = new ArrayList<>();
		for (List<PreconditionSelection> combination : Sets.cartesianProduct(possibleSelections)) {
			Map<String, Fluent> selectedFluents = new HashMap<>();
			Map<String, Long> selectedIds = new LinkedHashMap<>();
			for (PreconditionSelection selection : combination) {
				selectedFluents.put(selection.precondition().getKey(), selection.fluent());
				selectedIds.put(selection.precondition().getKey(), (long) selection.fluent().getID());
			}
			if (isSymbolicallyFeasible(taskFluent, selectedFluents)) {
				descriptions.add(createResolverDescription(depth, selectedIds));
			}
		}
		return List.copyOf(descriptions);
	}

	public List<ConstraintNetwork> expand(
			Fluent taskFluent, int depth, FluentNetworkSolver groundSolver, Fluent[] openFluents) {
		List<ResolverDescription> descriptions = describeResolvers(taskFluent, depth, openFluents);
		return RESOLVER_ADAPTER.compile(this, descriptions, taskFluent, depth, groundSolver, openFluents);
	}

	public List<ConstraintNetwork> materializeResolverDescriptions(
			Fluent taskFluent, int depth, FluentNetworkSolver groundSolver, Fluent[] openFluents) {
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
									if (openFluent.getMarking() == HTNMetaConstraint.markings.CLOSED) {
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
				effKeyToVariableMap.put(et.getKey(), et.getPrototype(groundSolver));
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
						// subtract impossibleSymbols
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
				for (Constraint con : expandEffects(taskFluent, groundSolver)) {
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
					AllenIntervalConstraint durationCon = TEMPORAL_ADAPTER.toAllenIntervalConstraint(
							new TemporalConstraintTemplate(TemporalRelation.Duration, durationBounds));
					durationCon.setFrom(taskFluent.getAllenInterval());
					durationCon.setTo(taskFluent.getAllenInterval());
					cn.addConstraint(durationCon);
				}

				// Add Constraints for RESOURCES
				for (ResourceRequirement rt : this.resourceUsageIndicators) {
					FluentConstraint resourceCon = 
							new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE,
									RESOURCE_ADAPTER.toResourceUsageTemplate(rt));
					resourceCon.setFrom(taskFluent);
					resourceCon.setTo(taskFluent);
					cn.addConstraint(resourceCon);
				}


				if (groundSolver.hasIntegerConstraintSolver()) {
					// add integer constraints from templates
					if (integerConstraintTemplates != null) {
						for (IntegerConstraintTemplate ict : integerConstraintTemplates) {
							String[] variableKeys = ict.variableKeys();
							Variable[] icScope = new Variable[variableKeys.length];
							for (int i = 0; i < variableKeys.length; i++) {
								String key = variableKeys[i];
								Map<String, Integer> keyOccurrences = integerVariableOccurrencesMap.get(key);
								if (keyOccurrences == null) {
									throw new IllegalArgumentException(
											"Key " + key + " is used in IntegerConstraint but it is undefined!");
								}
								// prefer IntegerVariables from the task
								Integer positionInTask = keyOccurrences.get(HEAD_KEYWORD_STRING);
								if (positionInTask != null) {
									icScope[i] = taskFluent.getIntegerVariables()[positionInTask];
								} else { // otherwise use first entry
									for (Entry<String, Integer> occ : keyOccurrences.entrySet()) {
										Variable v = findVariable(occ.getKey(), taskFluent, preKeyToFluentMap, effKeyToVariableMap);
										if (v instanceof Fluent) {
											icScope[i] = ((Fluent)v).getIntegerVariables()[occ.getValue()];
										} else {
											icScope[i] = new VariablePrototype(groundSolver, v, occ.getValue());
										}
										break;
									}
								}
							}
							IntegerConstraint ic = INTEGER_ADAPTER.toIntegerConstraint(ict, icScope);
							cn.addConstraint(ic);
						}
					}

					// add ALLEQUAL integer constraint for integer variables with the same name
					for (Map<String, Integer> occurrences : integerVariableOccurrencesMap.values()) {
						if (occurrences.size() > 0) {
							List<Variable> vars = new ArrayList<>(occurrences.size());
							for (Entry<String, Integer> occ : occurrences.entrySet()) {
								if(occ.getKey().equals(HEAD_KEYWORD_STRING)) {
									vars.add(taskFluent.getIntegerVariables()[occ.getValue()]);
								} else {
									Variable v = findVariable(occ.getKey(), taskFluent, preKeyToFluentMap, effKeyToVariableMap);
									if (v instanceof Fluent) {
										vars.add(((Fluent)v).getIntegerVariables()[occ.getValue()]);
									} else {
										vars.add(new VariablePrototype(groundSolver, v, occ.getValue()));
									}
								}
							}
							cn.addConstraint(
									new IntegerConstraint(IntegerConstraint.Type.ALLEQUAL,
											vars.toArray(new Variable[vars.size()])));
						}
					}

					// add IntegerConstraints for constant values
					for (int i = 0; i < integerArguments.length; i++) {
						if(!integerArguments[i].isVariable()) {
							cn.addConstraint(new IntegerConstraint(IntegerConstraint.Type.ARITHM,
									new Variable[]{taskFluent.getIntegerVariables()[i]},
									"=", integerArguments[i].constValue));
						}
					}
					for (EffectTemplate et : effects) {
						for (int i = 0; i < et.getIntArgs().length; i++) {
							if (!et.getIntArgs()[i].isVariable()) {
								Variable v = et.getPrototype(groundSolver);
								VariablePrototype vp = new VariablePrototype(groundSolver, v, i);
								cn.addConstraint(new IntegerConstraint(IntegerConstraint.Type.ARITHM,
										new Variable[]{vp}, "=", et.getIntArgs()[i].constValue));
							}
						}
					}
					for (HTNPrecondition pre : preconditions) {
						if (pre.getIntegerArguments() != null) {
							IntArg[] intArgs = pre.getIntegerArguments();
							for (int i = 0; i < intArgs.length; i++) {
								if (!intArgs[i].isVariable()) {
									Fluent preFluent = preKeyToFluentMap.get(pre.getKey());
									cn.addConstraint(new IntegerConstraint(IntegerConstraint.Type.ARITHM,
											new Variable[]{preFluent.getIntegerVariables()[i]},
											"=", intArgs[i].constValue));
								}
							}
						}

					}

//					// add some integer constraints only for testing
//					IntegerConstraint ic0 = new IntegerConstraint(IntegerConstraint.Type.ARITHM,
//							new IntegerVariable[]{taskFluent.getIntegerVariables()[0]},
//							"=", 77);
//					cn.addConstraint(ic0);
//					IntegerConstraint ic1 = new IntegerConstraint(IntegerConstraint.Type.ARITHM,
//							new IntegerVariable[]{taskFluent.getIntegerVariables()[0], taskFluent.getIntegerVariables()[1]},
//							"+", ">=", 100);
//					cn.addConstraint(ic1);
//					// between first int variable of the task and third int variable of first effect
//					for (Entry<String, Variable> e: effKeyToVariableMap.entrySet()) {
//						VariablePrototype effectIntVar3 = new VariablePrototype(groundSolver, e.getValue(), 2);
//						IntegerConstraint icEffect = new IntegerConstraint(IntegerConstraint.Type.ARITHM,
//								new Variable[]{taskFluent.getIntegerVariables()[0], effectIntVar3},
//								"+", ">=", 1000);
//						cn.addConstraint(icEffect);
//						break;
//					}
				}

				ret.add(cn);
			} else {
				logger.finest("Ommitting non-feasible CN");
			}
		}
		return ret;		
	}

	private boolean isSymbolicallyFeasible(
			Fluent taskFluent, Map<String, Fluent> selectedPreconditions) {
		if (variableOccurrencesMap == null) {
			return true;
		}
		for (Entry<String, Map<String, Integer>> occurrences : variableOccurrencesMap.entrySet()) {
			Set<String> possibleValues = null;
			String[] positiveValues = variablesPossibleValuesMap.get(occurrences.getKey());
			if (positiveValues != null) {
				possibleValues = new HashSet<>(Arrays.asList(positiveValues));
			}
			for (Entry<String, Integer> occurrence : occurrences.getValue().entrySet()) {
				Fluent fluent = occurrence.getKey().equals(HEAD_KEYWORD_STRING)
						? taskFluent
						: selectedPreconditions.get(occurrence.getKey());
				if (fluent != null) {
					List<String> symbols = Arrays.asList(fluent.getCompoundSymbolicVariable()
							.getSymbolsAt(occurrence.getValue() + 1));
					if (possibleValues == null) {
						possibleValues = new HashSet<>(symbols);
					} else {
						possibleValues.retainAll(symbols);
					}
				}
			}
			if (possibleValues != null && variablesImpossibleValuesMap != null) {
				String[] impossibleValues = variablesImpossibleValuesMap.get(occurrences.getKey());
				if (impossibleValues != null) {
					possibleValues.removeAll(Arrays.asList(impossibleValues));
				}
			}
			if (possibleValues != null && possibleValues.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private ResolverDescription createResolverDescription(
			int depth, Map<String, Long> selectedPreconditions) {
		List<FluentPrototypeDescription> prototypes = new ArrayList<>();
		for (EffectTemplate effect : effects) {
			List<String> arguments = new ArrayList<>(Arrays.asList(effect.getInputArgs()));
			while (arguments.size() < effect.getMaxArgs()) {
				arguments.add(HybridDomain.EMPTYSTRING);
			}
			List<IntegerArgumentDescription> integerDescriptions = new ArrayList<>();
			for (IntArg argument : effect.getIntArgs()) {
				integerDescriptions.add(argument.isVariable()
						? IntegerArgumentDescription.variable(argument.varName)
						: IntegerArgumentDescription.constant(argument.constValue));
			}
			prototypes.add(new FluentPrototypeDescription(
					effect.getKey(),
					effect.getComponent(),
					new FluentSignature(effect.getName(), arguments),
					integerDescriptions,
					this instanceof HTNOperator ? ProblemMarking.OPEN : ProblemMarking.UNPLANNED));
		}

		List<ResolverSemanticKind> kinds = new ArrayList<>();
		for (HTNPrecondition precondition : preconditions) {
			kinds.add(ResolverSemanticKind.PRECONDITION);
			if (precondition.isNegativeEffect() && this instanceof HTNOperator) {
				kinds.add(ResolverSemanticKind.NEGATIVE_EFFECT);
			}
		}
		for (int i = 0; i < effects.length; i++) {
			kinds.add(this instanceof HTNOperator
					? ResolverSemanticKind.EFFECT
					: ResolverSemanticKind.DECOMPOSITION);
		}
		List<ResolverOrderingEdge> orderingEdges = new ArrayList<>();
		if (this instanceof HTNMethod method && method.getOrderings() != null) {
			for (OrderingConstraintTemplate ordering : method.getOrderings()) {
				orderingEdges.add(new ResolverOrderingEdge(
						ordering.getFrom().getKey(), ordering.getTo().getKey()));
				kinds.add(ResolverSemanticKind.ORDERING);
			}
		}
		List<SymbolicConstraintDescription> symbolicConstraints = createSymbolicDescriptions();
		int bindingCount = (int) symbolicConstraints.stream()
				.filter(constraint -> constraint.kind() == SymbolicRelationKind.EQUAL)
				.count();
		for (int i = 0; i < bindingCount; i++) {
			kinds.add(ResolverSemanticKind.SYMBOLIC_BINDING);
		}
		for (SymbolicConstraintDescription constraint : symbolicConstraints) {
			if (constraint.kind() == SymbolicRelationKind.DIFFERENT) {
				kinds.add(ResolverSemanticKind.SYMBOLIC_DIFFERENCE);
			} else if (constraint.kind() == SymbolicRelationKind.ALLOWED_VALUES
					|| constraint.kind() == SymbolicRelationKind.FORBIDDEN_VALUES) {
				kinds.add(ResolverSemanticKind.VALUE_RESTRICTION);
			}
		}
		List<ResolverTemporalConstraintDescription> temporalConstraints = new ArrayList<>();
		if (additionalConstraints != null) {
			for (AdditionalConstraintTemplate constraint : additionalConstraints) {
				addTemporalDescription(temporalConstraints, kinds, constraint);
			}
		}
		for (HTNPrecondition precondition : preconditions) {
			for (AdditionalConstraintTemplate constraint : precondition.getAdditionalConstraints()) {
				addTemporalDescription(temporalConstraints, kinds, constraint);
			}
		}
		for (EffectTemplate effect : effects) {
			for (AdditionalConstraintTemplate constraint : effect.getAdditionalConstraints()) {
				addTemporalDescription(temporalConstraints, kinds, constraint);
			}
		}
		if (durationBounds != null) {
			temporalConstraints.add(new ResolverTemporalConstraintDescription(
					HEAD_KEYWORD_STRING,
					HEAD_KEYWORD_STRING,
					new TemporalConstraintTemplate(TemporalRelation.Duration, durationBounds)));
			kinds.add(ResolverSemanticKind.TEMPORAL_RELATION);
		}
		if (!resourceUsageIndicators.isEmpty()) {
			for (int i = 0; i < resourceUsageIndicators.size(); i++) {
				kinds.add(ResolverSemanticKind.RESOURCE_USAGE);
			}
		}
		List<ResolverIntegerConstraintDescription> integerConstraints = createIntegerDescriptions();
		if (!integerConstraints.isEmpty()) {
			kinds.add(ResolverSemanticKind.INTEGER_RELATION);
		}
		kinds.add(ResolverSemanticKind.APPLICATION);
		return new ResolverDescription(
				id, taskname, depth, preferenceWeight, selectedPreconditions, prototypes,
				kinds, symbolicConstraints, orderingEdges, temporalConstraints, integerConstraints,
				resourceUsageIndicators, bindingCount);
	}

	private void addTemporalDescription(
			List<ResolverTemporalConstraintDescription> descriptions,
			List<ResolverSemanticKind> kinds,
			AdditionalConstraintTemplate constraint) {
		descriptions.add(new ResolverTemporalConstraintDescription(
				constraint.getFromKey(), constraint.getToKey(), constraint.getConstraint()));
		kinds.add(ResolverSemanticKind.TEMPORAL_RELATION);
	}

	private List<SymbolicConstraintDescription> createSymbolicDescriptions() {
		List<SymbolicConstraintDescription> descriptions = new ArrayList<>();
		for (Map<String, Integer> occurrence : variableOccurrencesMap.values()) {
			List<Entry<String, Integer>> entries = occurrence.entrySet().stream()
					.filter(entry -> !entry.getKey().equals(HEAD_KEYWORD_STRING))
					.toList();
			for (int i = 0; i < entries.size(); i++) {
				for (int j = i + 1; j < entries.size(); j++) {
					descriptions.add(SymbolicConstraintDescription.binary(
							SymbolicRelationKind.EQUAL,
							toReference(entries.get(i)),
							toReference(entries.get(j))));
				}
			}
		}
		if (subDifferentDefinitions != null) {
			for (SubDifferentDefinition different : subDifferentDefinitions) {
				Map<String, Integer> fromOccurrences = variableOccurrencesMap.get(different.getFromKey());
				Map<String, Integer> toOccurrences = variableOccurrencesMap.get(different.getToKey());
				for (Entry<String, Integer> from : fromOccurrences.entrySet()) {
					for (Entry<String, Integer> to : toOccurrences.entrySet()) {
						descriptions.add(SymbolicConstraintDescription.binary(
								SymbolicRelationKind.DIFFERENT,
								toReference(from),
								toReference(to)));
					}
				}
			}
		}
		for (Entry<String, Map<String, Integer>> variable : variableOccurrencesMap.entrySet()) {
			String[] allowed = variablesPossibleValuesMap.get(variable.getKey());
			String[] forbidden = variablesImpossibleValuesMap == null
					? null
					: variablesImpossibleValuesMap.get(variable.getKey());
			for (Entry<String, Integer> occurrence : variable.getValue().entrySet()) {
				if (allowed != null) {
					descriptions.add(SymbolicConstraintDescription.unary(
							SymbolicRelationKind.ALLOWED_VALUES,
							toReference(occurrence),
							Arrays.asList(allowed)));
				}
				if (forbidden != null) {
					descriptions.add(SymbolicConstraintDescription.unary(
							SymbolicRelationKind.FORBIDDEN_VALUES,
							toReference(occurrence),
							Arrays.asList(forbidden)));
				}
			}
		}
		return List.copyOf(descriptions);
	}

	private List<ResolverIntegerConstraintDescription> createIntegerDescriptions() {
		List<ResolverIntegerConstraintDescription> descriptions = new ArrayList<>();
		if (integerConstraintTemplates != null) {
			for (IntegerConstraintTemplate template : integerConstraintTemplates) {
				List<ResolverArgumentReference> references = new ArrayList<>();
				for (String variableKey : template.variableKeys()) {
					references.add(preferredIntegerReference(variableKey));
				}
				descriptions.add(new ResolverIntegerConstraintDescription(
						template.kind(), references, template.operator1(), template.operator2(),
						template.constant()));
			}
		}
		for (Map<String, Integer> occurrences : integerVariableOccurrencesMap.values()) {
			if (!occurrences.isEmpty()) {
				descriptions.add(new ResolverIntegerConstraintDescription(
						IntegerConstraintKind.ALL_EQUAL,
						occurrences.entrySet().stream().map(this::toReference).toList(),
						null, null, 0));
			}
		}
		for (int i = 0; i < integerArguments.length; i++) {
			if (!integerArguments[i].isVariable()) {
				descriptions.add(integerConstantDescription(HEAD_KEYWORD_STRING, i, integerArguments[i].constValue));
			}
		}
		for (EffectTemplate effect : effects) {
			for (int i = 0; i < effect.getIntArgs().length; i++) {
				if (!effect.getIntArgs()[i].isVariable()) {
					descriptions.add(integerConstantDescription(
							effect.getKey(), i, effect.getIntArgs()[i].constValue));
				}
			}
		}
		for (HTNPrecondition precondition : preconditions) {
			if (precondition.getIntegerArguments() != null) {
				for (int i = 0; i < precondition.getIntegerArguments().length; i++) {
					if (!precondition.getIntegerArguments()[i].isVariable()) {
						descriptions.add(integerConstantDescription(
								precondition.getKey(), i,
								precondition.getIntegerArguments()[i].constValue));
					}
				}
			}
		}
		return List.copyOf(descriptions);
	}

	private ResolverIntegerConstraintDescription integerConstantDescription(
			String key, int index, int value) {
		return new ResolverIntegerConstraintDescription(
				IntegerConstraintKind.ARITHMETIC,
				List.of(new ResolverArgumentReference(key, index)),
				"=", null, value);
	}

	private ResolverArgumentReference preferredIntegerReference(String variableKey) {
		Map<String, Integer> occurrences = integerVariableOccurrencesMap.get(variableKey);
		if (occurrences == null) {
			throw new IllegalArgumentException(
					"Key " + variableKey + " is used in IntegerConstraint but it is undefined!");
		}
		Integer headPosition = occurrences.get(HEAD_KEYWORD_STRING);
		if (headPosition != null) {
			return new ResolverArgumentReference(HEAD_KEYWORD_STRING, headPosition);
		}
		return toReference(occurrences.entrySet().iterator().next());
	}

	private ResolverArgumentReference toReference(Entry<String, Integer> occurrence) {
		return new ResolverArgumentReference(occurrence.getKey(), occurrence.getValue());
	}

	private record PreconditionSelection(HTNPrecondition precondition, Fluent fluent) {}
	
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
	 * @param preKeyToFluentMap Map of precondition keys to fluent variables.
	 * @param effKeyToVariableMap Map of effect keys to variables
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
				
				AllenIntervalConstraint newCon = TEMPORAL_ADAPTER.toAllenIntervalConstraint(act.getConstraint());
				newCon.setFrom(from);
				newCon.setTo(to);
				ret.add(newCon);
			} else if (act.headToHead()) {    // HEAD -> HEAD
				AllenIntervalConstraint newCon = TEMPORAL_ADAPTER.toAllenIntervalConstraint(act.getConstraint());
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

						Variable var = findCompoundSymbolicVariable(occ.getKey(), taskFluent, preKeyToFluentMap, effKeyToVariableMap);
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
								  Map<String, Fluent> preKeyToFluentMap,
								  Map<String, Variable> effKeyToVariableMap) {
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

		return var;
	}
	
	private Variable findCompoundSymbolicVariable(String id, Fluent taskFluent,
												  Map<String, Fluent> preKeyToFluentMap,
												  Map<String, Variable> effKeyToVariableMap) {
		Variable var = findVariable(id, taskFluent, preKeyToFluentMap, effKeyToVariableMap);

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
					
					Variable fromVar = findCompoundSymbolicVariable(fromEntry.getKey(), taskFluent, preKeyToFluentMap,
							effKeyToVariableMap);

					int fromIndex = fromEntry.getValue().intValue();
					for (Entry<String, Integer> toEntry :toOccs.entrySet()) {
						Variable toVar = findCompoundSymbolicVariable(toEntry.getKey(), taskFluent, preKeyToFluentMap,
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
	 * @return Constraints representing the decompositions or positive effects
	 */
	public abstract List<Constraint> expandEffects(Fluent taskfluent,
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
					connections.add(Integer.valueOf(j));
					connections.add(Integer.valueOf(i));
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
	public TemporalBounds getDurationBounds() {
		return durationBounds;
	}

	/**
	 * 
	 * @param durationBounds The bounds that will be used for the duration constraint of this task.
	 */
	public void setDurationBounds(TemporalBounds durationBounds) {
		this.durationBounds = durationBounds;
	}

	public int getId() {
		return id;
	}

	public int getPreferenceWeight() {
		return preferenceWeight;
	}

	public List<String> getSymbolicConstants() {
		return symbolicConstants;
	}

	public EffectTemplate[] getEffects() {
		return effects;
	}

	public String[] getStringArgumentNames() {
		return arguments;
	}
}
