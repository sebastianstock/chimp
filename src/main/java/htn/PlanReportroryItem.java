package htn;

import com.google.common.collect.Sets;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.SubDifferentDefinition;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.Variable;
import chimp.kernel.VariablePrototype;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.fluent.ResourceRequirementAdapter;
import chimp.kernel.integer.IntegerConstraint;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.kernel.temporal.TemporalVariable;

import chimp.logging.ChimpLogging;
import chimp.model.FluentMarking;
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
import chimp.model.ResourceRequirement;
import chimp.model.SymbolicConstraintDescription;
import chimp.model.SymbolicRelationKind;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class PlanReportroryItem {
	private static final ResourceRequirementAdapter RESOURCE_ADAPTER =
			new ResourceRequirementAdapter();
	private static final chimp.search.ChimpResolverAdapter RESOLVER_ADAPTER =
			new chimp.search.ChimpResolverAdapter();
	
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
	
	Logger logger = ChimpLogging.getLogger(PlanReportroryItem.class);
	
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
									|| openFluent.getMarking() != FluentMarking.CLOSED.ordinal())) {
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

	/**
	 * Builds a single resolver {@link ConstraintNetwork} from one {@link ResolverDescription}.
	 * This is the description-driven compilation path — one network per description,
	 * no cartesian product (that's in {@link #describeResolvers}).
	 *
	 * @return the compiled network, or {@code null} if the description is infeasible
	 *         (e.g. sub-different check fails)
	 */
	public ConstraintNetwork buildResolverFromDescription(
			ResolverDescription description,
			Fluent taskFluent,
			int depth,
			FluentNetworkSolver groundSolver,
			Fluent[] openFluents) {

		// Reconstruct preKeyToFluentMap from selected preconditions
		Map<String, Fluent> preKeyToFluentMap = new HashMap<String, Fluent>();
		Map<String, Long> selectedIds = description.selectedPreconditions();
		if (preconditions != null) {
			for (HTNPrecondition pre : preconditions) {
				Long fluentId = selectedIds.get(pre.getKey());
				if (fluentId == null) continue;
				for (Fluent openFluent : openFluents) {
					if (openFluent.getID() == fluentId) {
						preKeyToFluentMap.put(pre.getKey(), openFluent);
						break;
					}
				}
			}
		}

		// Build effKeyToVariableMap from effects
		Map<String, Variable> effKeyToVariableMap = new HashMap<String, Variable>();
		for (EffectTemplate et : effects) {
			effKeyToVariableMap.put(et.getKey(), et.getPrototype(groundSolver));
		}

		ConstraintNetwork cn = new ConstraintNetwork();

		// Add PRE and CLOSES constraints
		if (preconditions != null) {
			for (HTNPrecondition pre : preconditions) {
				Fluent openFluent = preKeyToFluentMap.get(pre.getKey());
				if (openFluent == null) continue;

				FluentConstraint preCon = new FluentConstraint(FluentConstraint.Type.PRE,
						pre.getConnections());
				preCon.setFrom(openFluent);
				preCon.setTo(taskFluent);
				preCon.setNegativeEffect(pre.isNegativeEffect());
				preCon.setAdditionalConstraints(pre.getAdditionalConstraints());
				cn.addConstraint(preCon);

				// add closes for negative effects
				if (pre.isNegativeEffect() && this instanceof HTNOperator) {
					FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
					closes.setFrom(preCon.getTo());
					closes.setTo(preCon.getFrom());
					if (preCon.hasAdditionalConstraints()) {
						closes.setAdditionalConstraints(preCon.getAdditionalConstraints());
					}
					cn.addConstraint(closes);
				}
			}
		}

		// add binding constraints between preconditions or effects
		for (Constraint con : createPreconditionsEffectsBindings(preKeyToFluentMap, effKeyToVariableMap)) {
			cn.addConstraint(con);
		}

		// add positive effects/decomposition
		for (Constraint con : expandEffects(taskFluent, groundSolver)) {
			cn.addConstraint(con);
		}

		// add additional constraints between preconditions and effects
		for (TemporalConstraint aCon : setVarsInAdditionalConstraints(taskFluent, preKeyToFluentMap, effKeyToVariableMap)) {
			cn.addConstraint(aCon);
		}

		// add sub-different constraints
		try {
			for (Constraint con : createSubDifferents(taskFluent, preKeyToFluentMap, effKeyToVariableMap)) {
				cn.addConstraint(con);
			}
		} catch (IllegalStateException e) {
			return null;
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
			TemporalConstraint durationCon = toTemporalConstraint(
					new TemporalConstraintTemplate(TemporalRelation.Duration, durationBounds));
			durationCon.setFrom(taskFluent.getTemporalVariable());
			durationCon.setTo(taskFluent.getTemporalVariable());
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
									icScope[i] = VariablePrototype.forInteger(groundSolver, (VariablePrototype) v, occ.getValue());
								}
								break;
							}
						}
					}
					Constraint ic = toIntegerConstraint(ict, icScope);
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
								vars.add(VariablePrototype.forInteger(groundSolver, (VariablePrototype) v, occ.getValue()));
							}
						}
					}
					cn.addConstraint(
							createAllEqualConstraint(
									vars.toArray(new Variable[vars.size()])));
				}
			}

			// add IntegerConstraints for constant values
			for (int i = 0; i < integerArguments.length; i++) {
				if(!integerArguments[i].isVariable()) {
					cn.addConstraint(createConstantConstraint(
							taskFluent.getIntegerVariables()[i], integerArguments[i].constValue));
				}
			}
			for (EffectTemplate et : effects) {
				for (int i = 0; i < et.getIntArgs().length; i++) {
					if (!et.getIntArgs()[i].isVariable()) {
						VariablePrototype v = et.getPrototype(groundSolver);
						VariablePrototype vp = VariablePrototype.forInteger(groundSolver, v, i);
						cn.addConstraint(createConstantConstraint(
								vp, et.getIntArgs()[i].constValue));
					}
				}
			}
			for (HTNPrecondition pre : preconditions) {
				if (pre.getIntegerArguments() != null) {
					IntArg[] intArgs = pre.getIntegerArguments();
					for (int i = 0; i < intArgs.length; i++) {
						if (!intArgs[i].isVariable()) {
							Fluent preFluent = preKeyToFluentMap.get(pre.getKey());
							cn.addConstraint(createConstantConstraint(
										preFluent.getIntegerVariables()[i], intArgs[i].constValue));
						}
					}
				}
			}
		}

		return cn;
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
	private List<TemporalConstraint> setVarsInAdditionalConstraints(Fluent taskFluent,
			Map<String, Fluent> preKeyToFluentMap, Map<String, Variable> effKeyToVariableMap) {
		List<TemporalConstraint> ret = new ArrayList<TemporalConstraint>();
		
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
				
				TemporalConstraint newCon = toTemporalConstraint(act.getConstraint());
				newCon.setFrom(from);
				newCon.setTo(to);
				ret.add(newCon);
			} else if (act.headToHead()) {    // HEAD -> HEAD
				TemporalConstraint newCon = toTemporalConstraint(act.getConstraint());
				newCon.setFrom(taskFluent.getTemporalVariable());
				newCon.setTo(taskFluent.getTemporalVariable());
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
							Constraint rcon =
									createPositiveValueRestriction(
											var, indices, positiveRestrictions);
							ret.add(rcon);
						}
						
						if (impossibleValues != null) {
							// TODO merge multiple constraints into one.
							Constraint rcon =
									createNegativeValueRestriction(
											var, indices, negativeRestrictions);
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
						Constraint scon = createSubDifferent(fromVar, toVar, connections);
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
						// set from
						Variable from = preKeyToFluentMap.get(occKeys[i]); // try precondition keys
						if (from == null) {
							from = effKeyToVariableMap.get(occKeys[i]);          // else try effect keys
						}
						if (from == null) {
							logger.fine("No fluent found for key " + occKeys[i]);
							continue;
						}
						if (from instanceof Fluent) {
							from = ((Fluent) from).getCompoundSymbolicVariable();
						} // else: Variable Prototype (will be set in addResolverSub)
						
						// set to
						Variable to = preKeyToFluentMap.get(occKeys[j]); // try precondition keys
						if (to == null) {
							to = effKeyToVariableMap.get(occKeys[j]);        // else try effect keys
						}
						if (to == null) {
							logger.fine("No fluent found for key " + occKeys[j]);
							continue;
						}
						if (to instanceof Fluent) {
							to = ((Fluent) to).getCompoundSymbolicVariable();
						} // else: Variable Prototype (will be set in addResolverSub)
						
						Constraint bcon = createSubMatches(from, to, connections);
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

	// --- Inlined adapter methods (formerly in MetaCspTemporalConstraintAdapter
	//     and MetaCspSymbolicConstraintAdapter) ---

	private static TemporalConstraint toTemporalConstraint(
			TemporalConstraintTemplate template) {
		TemporalRelation relation = template.relation();
		if (template.bounds().isEmpty()) {
			return new TemporalConstraint(relation);
		}
		TemporalBounds[] bounds = template.bounds().toArray(TemporalBounds[]::new);
		return new TemporalConstraint(relation, bounds);
	}

	private static Constraint createPositiveValueRestriction(
			Variable var, int[] indices, String[][] restrictions) {
		CompoundSymbolicConstraint con = new CompoundSymbolicConstraint(
				CompoundSymbolicConstraint.Type.POSITIVEVALUERESTRICTION,
				indices, restrictions);
		con.setFrom(var);
		con.setTo(var);
		return con;
	}

	private static Constraint createNegativeValueRestriction(
			Variable var, int[] indices, String[][] restrictions) {
		CompoundSymbolicConstraint con = new CompoundSymbolicConstraint(
				CompoundSymbolicConstraint.Type.NEGATIVEVALUERESTRICTION,
				indices, restrictions);
		con.setFrom(var);
		con.setTo(var);
		return con;
	}

	private static Constraint createSubDifferent(
			Variable from, Variable to, int[] connections) {
		CompoundSymbolicConstraint con = new CompoundSymbolicConstraint(
				CompoundSymbolicConstraint.Type.SUBDIFFERENT, connections);
		con.setFrom(from);
		con.setTo(to);
		return con;
	}

	private static Constraint createSubMatches(
			Variable from, Variable to, int[] connections) {
		CompoundSymbolicConstraint con = new CompoundSymbolicConstraint(
				CompoundSymbolicConstraint.Type.SUBMATCHES, connections);
		con.setFrom(from);
		con.setTo(to);
		return con;
	}

	// --- Inlined adapter methods (formerly in MetaCspIntegerConstraintAdapter) ---

	private static IntegerConstraint toIntegerConstraint(
			IntegerConstraintTemplate template, Variable[] scope) {
		IntegerConstraint.Type type = switch (template.kind()) {
			case ARITHMETIC -> IntegerConstraint.Type.ARITHM;
			case ALL_DIFFERENT -> IntegerConstraint.Type.ALLDIFFERENT;
			case ALL_EQUAL -> IntegerConstraint.Type.ALLEQUAL;
		};
		return new IntegerConstraint(
				type, scope,
				template.operator1(), template.operator2(), template.constant());
	}

	private static IntegerConstraint createAllEqualConstraint(Variable[] scope) {
		return new IntegerConstraint(
				IntegerConstraint.Type.ALLEQUAL, scope);
	}

	private static IntegerConstraint createConstantConstraint(
			Variable variable, int constant) {
		return new IntegerConstraint(
				IntegerConstraint.Type.ARITHM,
				new Variable[]{variable}, "=", constant);
	}
}
