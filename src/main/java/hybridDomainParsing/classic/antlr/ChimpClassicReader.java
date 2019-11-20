package hybridDomainParsing.classic.antlr;

import com.google.common.primitives.Ints;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.AdditionalConstraintTemplate;
import htn.EffectTemplate;
import htn.HTNMethod;
import htn.HTNOperator;
import htn.HTNPrecondition;
import htn.PlanReportroryItem;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.SubDifferentDefinition;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChimpClassicReader implements ChimpClassicVisitor {

    public class ParsedDomain implements ClassicHybridDomain {
        public final List<PlanReportroryItem> operators = new ArrayList<PlanReportroryItem>();
        public final List<PlanReportroryItem> methods = new ArrayList<PlanReportroryItem>();
        public final List<FluentScheduler> fluentSchedulers = new ArrayList<FluentScheduler>();
        public String name;
        public final List<FluentResourceUsageScheduler> resourceSchedulers =
                new ArrayList<FluentResourceUsageScheduler>();
        public final List<ResourceUsageTemplate> fluentResourceUsages =
                new ArrayList<ResourceUsageTemplate>();

        public int maxArgs; // Maximum number of arguments of a fluent.
        public final List<String> predicateSymbols = new ArrayList<>();

        @Override
        public List<PlanReportroryItem> getOperators() {
            return operators;
        }

        @Override
        public List<PlanReportroryItem> getMethods() {
            return methods;
        }

        @Override
        public List<FluentScheduler> getFluentSchedulers() {
            return fluentSchedulers;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<FluentResourceUsageScheduler> getResourceSchedulers() {
            return resourceSchedulers;
        }

        @Override
        public List<ResourceUsageTemplate> getFluentResourceUsages() {
            return fluentResourceUsages;
        }

        @Override
        public int getMaxArgs() {
            return maxArgs;
        }
    }

    public static final String HEAD_KEYWORD_STRING = "task";

    public static final String EFFECT_COMPONENT = "PlannedState";
    public static final String SUBTASK_COMPONENT = "Task";
    public static final int DEFAULT_PREFERENCE_WEIGHT = 1;

    private int maxArgs = -1;
    private final FluentNetworkSolver groundSolver;

    public ChimpClassicReader(FluentNetworkSolver groundSolver) {
        this.groundSolver = groundSolver;
    }

    @Override
    public ParsedDomain visitDomain(ChimpClassicParser.DomainContext ctx) {
        ParsedDomain ret = new ParsedDomain();
        ret.name = visitDomain_name_def(ctx.domain_name_def());
        ret.maxArgs = visitMaxargs_def(ctx.maxargs_def());
        maxArgs = ret.maxArgs;
        ret.predicateSymbols.addAll(visitPredicatesymbols_def(ctx.predicatesymbols_def()));

        for (ChimpClassicParser.Domain_itemContext d : ctx.domain_item()) {
            if (d instanceof ChimpClassicParser.Domain_item_operatorContext) {
                ret.operators.add(visitDomain_item_operator((ChimpClassicParser.Domain_item_operatorContext) d));
            } else if (d instanceof ChimpClassicParser.Domain_item_resourceContext) {
                ret.resourceSchedulers.add(
                        visitDomain_item_resource((ChimpClassicParser.Domain_item_resourceContext) d));
            } else if (d instanceof ChimpClassicParser.Domain_item_statevariableContext) {
                ret.fluentSchedulers.addAll(
                        visitDomain_item_statevariable((ChimpClassicParser.Domain_item_statevariableContext) d));
            } else if (d instanceof ChimpClassicParser.Domain_item_methodContext) {
                ret.methods.add(visitDomain_item_method((ChimpClassicParser.Domain_item_methodContext) d));
            }
        }
        return ret;
    }

    @Override
    public FluentResourceUsageScheduler visitDomain_item_resource(ChimpClassicParser.Domain_item_resourceContext ctx) {
        return visitResource_def(ctx.resource_def());
    }

    @Override
    public List<FluentScheduler> visitDomain_item_statevariable(ChimpClassicParser.Domain_item_statevariableContext ctx) {
        return visitStatevariable_def(ctx.statevariable_def());
    }

    @Override
    public PlanReportroryItem visitDomain_item_operator(ChimpClassicParser.Domain_item_operatorContext ctx) {
        return visitOperator_def(ctx.operator_def());
    }

    @Override
    public PlanReportroryItem visitDomain_item_method(ChimpClassicParser.Domain_item_methodContext ctx) {
        return visitMethod_def(ctx.method_def());
    }

    @Override
    public String visitDomain_name_def(ChimpClassicParser.Domain_name_defContext ctx) {
        return ctx.NAME().getText();
    }

    @Override
    public Integer visitMaxargs_def(ChimpClassicParser.Maxargs_defContext ctx) {
        return numberToInt(ctx.NUMBER());
    }

    @Override
    public List<String> visitPredicatesymbols_def(ChimpClassicParser.Predicatesymbols_defContext ctx) {
        List<String> ret = new ArrayList<>();
        for (ChimpClassicParser.Predicate_symbolContext symbolContext : ctx.predicate_symbol()) {
            ret.add(visitPredicate_symbol(symbolContext));
        }
        return ret;
    }

    @Override
    public String visitPredicate_symbol(ChimpClassicParser.Predicate_symbolContext ctx) {
        return ctx.getText();
    }

    @Override
    public FluentResourceUsageScheduler visitResource_def(ChimpClassicParser.Resource_defContext ctx) {
        return new FluentResourceUsageScheduler(null, null, ctx.NAME().getText(),
                numberToInt(ctx.NUMBER()));
    }

    @Override
    public List<FluentScheduler> visitStatevariable_def(ChimpClassicParser.Statevariable_defContext ctx) {
        String name = visitStatevariable_name(ctx.statevariable_name());
        int field = numberToInt(ctx.NUMBER());
        List<FluentScheduler> schedulers = new ArrayList<>();
        for (TerminalNode fieldValue : ctx.NAME()) {
            schedulers.add(new FluentScheduler(null, null, name, field, fieldValue.getText()));
        }
        return schedulers;
    }

    @Override
    public String visitStatevariable_name(ChimpClassicParser.Statevariable_nameContext ctx) {
        return ctx.NAME().getText();
    }

    @Override
    public PlanReportroryItem visitMethod_def(ChimpClassicParser.Method_defContext ctx) {
        Predicate head = visitHead(ctx.head());

        int preferenceWeight = DEFAULT_PREFERENCE_WEIGHT;
        if (ctx.NUMBER() != null) {
            preferenceWeight = numberToInt(ctx.NUMBER());
        }
        List<PredicateWithId> parsedPreconditions = new ArrayList<>();
        List<PredicateWithId> parsedSubtasks = new ArrayList<>();
        List<ValueRestriction> parsedValueRestrictions = new ArrayList<>();
        List<ValueRestriction> parsedNotvalueRestrictions = new ArrayList<>();
        List<SubDifferentDefinition> subDifferentDefinitions = new ArrayList<>();
        List<AdditionalConstraintTemplate> temporalConstraintTemplates = new ArrayList<>();
        List<ParsedOrdering> parsedOrderingConstraints = new ArrayList<>();
        for (ChimpClassicParser.Method_elementContext d : ctx.method_element()) {
            if (d instanceof ChimpClassicParser.Precondition_m_elementContext) {
                parsedPreconditions.add(visitPrecondition_m_element(
                        (ChimpClassicParser.Precondition_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Value_restriction_m_elementContext) {
                parsedValueRestrictions.add(visitValue_restriction_m_element(
                        (ChimpClassicParser.Value_restriction_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Notvalue_restriction_m_elementContext) {
                parsedNotvalueRestrictions.add(visitNotvalue_restriction_m_element(
                        (ChimpClassicParser.Notvalue_restriction_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Vardifferent_m_elementContext) {
                subDifferentDefinitions.add(visitVardifferent_m_element(
                        (ChimpClassicParser.Vardifferent_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Temporal_constraint_m_elementContext) {
                temporalConstraintTemplates.add(visitTemporal_constraint_m_element(
                        (ChimpClassicParser.Temporal_constraint_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Subtask_def_m_elementContext) {
                parsedSubtasks.add(visitSubtask_def_m_element(
                        (ChimpClassicParser.Subtask_def_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Ordering_def_m_elementContext) {
                parsedOrderingConstraints.add(
                        visitOrdering_def_m_element((ChimpClassicParser.Ordering_def_m_elementContext) d));
            }
        }

        HTNPrecondition[] htnPreconditions = createHTNPreconditionsAndNegativeEffects(head,
                parsedPreconditions, new ArrayList<String>());
        addTemporalConstraintsToPreconditions(temporalConstraintTemplates, htnPreconditions);

        EffectTemplate[] effectTemplates = createEffectTemplates(parsedSubtasks, SUBTASK_COMPONENT);
        addTemporalConstraintsToEffects(temporalConstraintTemplates, effectTemplates);

        Constraint[] orderingConstraints = createOrderingConstraints(parsedOrderingConstraints, effectTemplates);
        HTNMethod method = new HTNMethod(head.name, head.args.toArray(new String[head.args.size()]),
                htnPreconditions, effectTemplates, orderingConstraints, preferenceWeight);

        method.addVariablesPossibleValues(createVariablesValuesMap(parsedValueRestrictions));
        method.setVariablesImpossibleValuesMap(createVariablesValuesMap(parsedValueRestrictions));

        method.setSubDifferentDefinitions(
                subDifferentDefinitions.toArray(new SubDifferentDefinition[subDifferentDefinitions.size()]));

        // set additional constraints from head to head or between preconditions and effects
        method.setAdditionalConstraints(filterAdditionalConstraints(temporalConstraintTemplates));

        return method;
    }

    Constraint[] createOrderingConstraints(List<ParsedOrdering> parsedOrderings, EffectTemplate[] subtasks) {
        Map<String, VariablePrototype> keyToPrototypesMap = new HashMap<String, VariablePrototype>();
        for (EffectTemplate et : subtasks) {
            keyToPrototypesMap.put(et.getKey(), et.getPrototype());
        }

        List<Constraint> cons = new ArrayList<Constraint>();
        for (ParsedOrdering parsedOrdering : parsedOrderings) {

            if (!keyToPrototypesMap.containsKey(parsedOrdering.fromKey)) {
                throw new IllegalStateException("Error while parsing 'Ordering': (from) key " + parsedOrdering.fromKey + " is not a subtask of the method");
            }
            if (!keyToPrototypesMap.containsKey(parsedOrdering.toKey)) {
                throw new IllegalStateException("Error while parsing 'Ordering': (to) key " + parsedOrdering.toKey + " is not a subtask of the method");
            }

            FluentConstraint con = new FluentConstraint(FluentConstraint.Type.BEFORE);
            con.setFrom(keyToPrototypesMap.get(parsedOrdering.fromKey));
            con.setTo(keyToPrototypesMap.get(parsedOrdering.toKey));
            cons.add(con);
        }
        return cons.toArray(new Constraint[cons.size()]);
    }

    /**
     * Convert value restricitons to a mapping of varible names to their possible/impossible values.
     */
    private static Map<String,String[]> createVariablesValuesMap(List<ValueRestriction> restrictions) {
        Map<String,String[]> ret = new HashMap<String, String[]>();
        for (ValueRestriction vr : restrictions) {
            ret.put(vr.varName, vr.constants.toArray(new String[vr.constants.size()]));
        }
        return ret;
    }

    @Override
    public PlanReportroryItem visitOperator_def(ChimpClassicParser.Operator_defContext ctx) {
        Predicate head = visitHead(ctx.head());

        int preferenceWeight = DEFAULT_PREFERENCE_WEIGHT;
        if (ctx.NUMBER() != null) {
            preferenceWeight = numberToInt(ctx.NUMBER());
        }
        List<PredicateWithId> parsedPreconditions = new ArrayList<>();
        List<String> negativeEffectKeys = new ArrayList<>();
        List<PredicateWithId> parsedPositiveEffects = new ArrayList<>();
        List<ValueRestriction> parsedValueRestrictions = new ArrayList<>();
        List<ValueRestriction> parsedNotvalueRestrictions = new ArrayList<>();
        List<SubDifferentDefinition> subDifferentDefinitions = new ArrayList<>();
        List<AdditionalConstraintTemplate> temporalConstraintTemplates = new ArrayList<>();
        List<ResourceUsageTemplate> resourceUsageTemplates = new ArrayList<>();
        for (ChimpClassicParser.Op_elementContext d : ctx.op_element()) {
            if (d instanceof ChimpClassicParser.Precondition_op_elementContext) {
                parsedPreconditions.add(visitPrecondition_op_element(
                        (ChimpClassicParser.Precondition_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Negative_effect_def_op_elementContext) {
                negativeEffectKeys.add(visitNegative_effect_def_op_element(
                        (ChimpClassicParser.Negative_effect_def_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Positive_effect_def_op_elementContext) {
                parsedPositiveEffects.add(visitPositive_effect_def_op_element(
                        (ChimpClassicParser.Positive_effect_def_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Value_restriction_op_elementContext) {
                parsedValueRestrictions.add(visitValue_restriction_op_element(
                        (ChimpClassicParser.Value_restriction_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Notvalue_restriction_op_elementContext) {
                parsedNotvalueRestrictions.add(visitNotvalue_restriction_op_element(
                        (ChimpClassicParser.Notvalue_restriction_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Vardifferent_op_elementContext) {
                subDifferentDefinitions.add(visitVardifferent_op_element(
                        (ChimpClassicParser.Vardifferent_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Temporal_constraint_op_elementContext) {
                temporalConstraintTemplates.add(visitTemporal_constraint_op_element(
                        (ChimpClassicParser.Temporal_constraint_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Resource_usage_op_elementContext) {
                resourceUsageTemplates.add(visitResource_usage_op_element(
                        (ChimpClassicParser.Resource_usage_op_elementContext) d));
            }
        }

        HTNPrecondition[] htnPreconditions = createHTNPreconditionsAndNegativeEffects(head,
                parsedPreconditions, negativeEffectKeys);
        addTemporalConstraintsToPreconditions(temporalConstraintTemplates, htnPreconditions);

        EffectTemplate[] effectTemplates = createEffectTemplates(parsedPositiveEffects, EFFECT_COMPONENT);
        addTemporalConstraintsToEffects(temporalConstraintTemplates, effectTemplates);

        HTNOperator op = new HTNOperator(head.name, head.args.toArray(new String[head.args.size()]),
                htnPreconditions, effectTemplates, preferenceWeight);

        op.addVariablesPossibleValues(createVariablesValuesMap(parsedValueRestrictions));
        op.setVariablesImpossibleValuesMap(createVariablesValuesMap(parsedValueRestrictions));

        op.setSubDifferentDefinitions(
                subDifferentDefinitions.toArray(new SubDifferentDefinition[subDifferentDefinitions.size()]));

        // set additional constraints from head to head or between preconditions and effects
        op.setAdditionalConstraints(filterAdditionalConstraints(temporalConstraintTemplates));

        op.addResourceUsageTemplates(resourceUsageTemplates);

        return op;
    }

    /**
     * Filter temporal constraints that are from head to head or that do not involve head
     */
    private AdditionalConstraintTemplate[] filterAdditionalConstraints(
            List<AdditionalConstraintTemplate> constraintTemplates) {
        ArrayList<AdditionalConstraintTemplate> ret = new ArrayList<AdditionalConstraintTemplate>();
        for (AdditionalConstraintTemplate act : constraintTemplates) {
            if (act.headToHead() || act.withoutHead()) {
                ret.add(act);
            }
        }
        return ret.toArray(new AdditionalConstraintTemplate[ret.size()]);
    }

    private void addTemporalConstraintsToPreconditions(List<AdditionalConstraintTemplate> temporalConstraintTemplates,
                                                       HTNPrecondition[] preconditions) {
        for (AdditionalConstraintTemplate constraintTemplate : temporalConstraintTemplates) {
            for (HTNPrecondition pre : preconditions) {
                if (constraintTemplate.involvesHeadAndKey(pre.getKey())) {
                    pre.addAdditionalConstraint(constraintTemplate);
                    break;
                }
            }
        }
    }

    private void addTemporalConstraintsToEffects(List<AdditionalConstraintTemplate> temporalConstraintTemplates,
                                                 EffectTemplate[] effects) {
        for (AdditionalConstraintTemplate constraintTemplate : temporalConstraintTemplates) {
            for (EffectTemplate eff : effects) {
                if (constraintTemplate.involvesHeadAndKey(eff.getKey())) {
                    eff.addAdditionalConstraint(constraintTemplate);
                    break;
                }
            }
        }
    }

    private EffectTemplate[] createEffectTemplates(List<PredicateWithId> parsedPredicates, String component) {
        EffectTemplate[] effectTemplates = new EffectTemplate[parsedPredicates.size()];
        for (int i = 0; i < parsedPredicates.size(); i++) {
            PredicateWithId p = parsedPredicates.get(i);
            effectTemplates[i] = new EffectTemplate(p.id, p.predicate.name,
                    p.predicate.args.toArray(new String[p.predicate.args.size()]),
                    maxArgs,
                    groundSolver,
                    component);
        } return effectTemplates;
    }

    private HTNPrecondition[] createHTNPreconditionsAndNegativeEffects(Predicate head,
                                                                           List<PredicateWithId> parsedPreconditions,
                                                                           List<String> negativeEffectKeys) {
        HTNPrecondition[] htnPreconditions = new HTNPrecondition[parsedPreconditions.size()];
        for (int i = 0; i < parsedPreconditions.size(); i++) {
            PredicateWithId p = parsedPreconditions.get(i);
            HTNPrecondition htnPrecondition = createPrecondition(p, head);
            htnPrecondition.setNegativeEffect(negativeEffectKeys.contains(p.id));
            htnPreconditions[i] = htnPrecondition;
        }
        return htnPreconditions;
    }

    public class Predicate {
        String name;
        List<String> args;
    }

    public class PredicateWithId {
        String id;
        Predicate predicate;
    }

    @Override
    public Predicate visitHead(ChimpClassicParser.HeadContext ctx) {
        Predicate head = new Predicate();
        head.name = ctx.OP_NAME().getText();
        head.args = visitPredicate_args(ctx.predicate_args());
        return head;
    }

    @Override
    public PredicateWithId visitPrecondition_m_element(ChimpClassicParser.Precondition_m_elementContext ctx) {
        return visitPrecondition_def(ctx.precondition_def());
    }

    @Override
    public AdditionalConstraintTemplate visitTemporal_constraint_m_element(ChimpClassicParser.Temporal_constraint_m_elementContext ctx) {
        return visitTemporal_constraint_def(ctx.temporal_constraint_def());
    }

    @Override
    public ValueRestriction  visitValue_restriction_m_element(ChimpClassicParser.Value_restriction_m_elementContext ctx) {
        return visitValue_restriction_def(ctx.value_restriction_def());
    }

    @Override
    public  ValueRestriction visitNotvalue_restriction_m_element(ChimpClassicParser.Notvalue_restriction_m_elementContext ctx) {
        return visitNotvalue_restriction_def(ctx.notvalue_restriction_def());
    }

    @Override
    public Object visitTypevalue_restriction_m_element(ChimpClassicParser.Typevalue_restriction_m_elementContext ctx) {
        throw new IllegalStateException("Type restrictions are not implemented");
    }

    @Override
    public SubDifferentDefinition visitVardifferent_m_element(ChimpClassicParser.Vardifferent_m_elementContext ctx) {
        return visitVardifferent_def(ctx.vardifferent_def());
    }

    @Override
    public PredicateWithId visitSubtask_def_m_element(ChimpClassicParser.Subtask_def_m_elementContext ctx) {
        return visitSubtask_def(ctx.subtask_def());
    }

    @Override
    public ParsedOrdering visitOrdering_def_m_element(ChimpClassicParser.Ordering_def_m_elementContext ctx) {
        return visitOrdering_constraint_def(ctx.ordering_constraint_def());
    }

    @Override
    public PredicateWithId visitPrecondition_op_element(ChimpClassicParser.Precondition_op_elementContext ctx) {
        return visitPrecondition_def(ctx.precondition_def());
    }

    @Override
    public AdditionalConstraintTemplate visitTemporal_constraint_op_element(ChimpClassicParser.Temporal_constraint_op_elementContext ctx) {
        return visitTemporal_constraint_def(ctx.temporal_constraint_def());
    }

    @Override
    public PredicateWithId visitPositive_effect_def_op_element(ChimpClassicParser.Positive_effect_def_op_elementContext ctx) {
        return visitPositive_effect_def(ctx.positive_effect_def());
    }

    @Override
    public String visitNegative_effect_def_op_element(ChimpClassicParser.Negative_effect_def_op_elementContext ctx) {
        return visitNegative_effect_def(ctx.negative_effect_def());
    }

    @Override
    public ResourceUsageTemplate visitResource_usage_op_element(ChimpClassicParser.Resource_usage_op_elementContext ctx) {
        return visitResource_usage_def(ctx.resource_usage_def());
    }

    @Override
    public ValueRestriction visitValue_restriction_op_element(ChimpClassicParser.Value_restriction_op_elementContext ctx) {
        return visitValue_restriction_def(ctx.value_restriction_def());
    }

    @Override
    public ValueRestriction visitNotvalue_restriction_op_element(ChimpClassicParser.Notvalue_restriction_op_elementContext ctx) {
        return visitNotvalue_restriction_def(ctx.notvalue_restriction_def());
    }

    @Override
    public Object visitTypevalue_restriction_op_element(ChimpClassicParser.Typevalue_restriction_op_elementContext ctx) {
        throw new IllegalStateException("Type restrictions are not implemented");
    }

    @Override
    public SubDifferentDefinition visitVardifferent_op_element(ChimpClassicParser.Vardifferent_op_elementContext ctx) {
        return visitVardifferent_def(ctx.vardifferent_def());
    }

    @Override
    public PredicateWithId visitPrecondition_def(ChimpClassicParser.Precondition_defContext ctx) {
        PredicateWithId ret = new PredicateWithId();
        ret.id = visitId(ctx.id());
        ret.predicate = visitPredicate(ctx.predicate());
        return ret;
    }

    @Override
    public PredicateWithId visitSubtask_def(ChimpClassicParser.Subtask_defContext ctx) {
        PredicateWithId ret = new PredicateWithId();
        ret.id = visitId(ctx.id());
        ret.predicate = visitPredicate(ctx.predicate());
        return ret;
    }

    /**
     * Finds matching strings between two argument lists
     * @param src List of Strings that shall be matched
     * @param target List in which matching elements will be searched
     * @return An array of indexes with two entries for each string that occurs in both lists. In Alternating order the elements represent the index of the string in the source and target list.
     */
    private static int[] createArgConnections(List<String> src, List<String> target) {
        List<Integer> connectionsList = new ArrayList<Integer>();
        for (int i = 0; i < src.size(); i++) {
            String arg = src.get(i);
            if (arg.startsWith(HybridDomain.VARIABLE_INDICATOR)) {
                int targetPos = target.indexOf(arg);
                if (targetPos > 0) {
                    connectionsList.add(i);
                    connectionsList.add(targetPos);
                }
            }
        }
        return Ints.toArray(connectionsList);
    }

    // TODO move this to a SimpleFactory class
    public HTNPrecondition createPrecondition(PredicateWithId parsedPrecondition, Predicate head) {
        // find bindings to head
        int[] connections = createArgConnections(parsedPrecondition.predicate.args, head.args);

        HTNPrecondition ret = new HTNPrecondition(parsedPrecondition.predicate.name,
                parsedPrecondition.predicate.args.toArray(new String[parsedPrecondition.predicate.args.size()]),
                connections,
                maxArgs,
                HybridDomain.EMPTYSTRING,
                parsedPrecondition.id);

        // TODO move the following into caller method
//        for (AdditionalConstraintTemplate additionalCon : additionalAIConstraints) {
//            if (additionalCon.involvesHeadAndKey(preKey)) {
//                ret.addAdditionalConstraint(additionalCon);
//            }
//        }
        return ret;
    }

    @Override
    public PredicateWithId visitPositive_effect_def(ChimpClassicParser.Positive_effect_defContext ctx) {
        PredicateWithId ret = new PredicateWithId();
        ret.id = visitId(ctx.id());
        ret.predicate = visitPredicate(ctx.predicate());
        return ret;
    }

    @Override
    public String visitNegative_effect_def(ChimpClassicParser.Negative_effect_defContext ctx) {
        return visitId(ctx.id());
    }

    public class ParsedOrdering {
        String fromKey;
        String toKey;

        public ParsedOrdering(String fromKey, String toKey) {
            this.fromKey = fromKey;
            this.toKey = toKey;
        }
    }

    @Override
    public ParsedOrdering visitOrdering_constraint_def(ChimpClassicParser.Ordering_constraint_defContext ctx) {
        String fromKey = ctx.id(0).getText();
        String toKey = ctx.id(1).getText();
        return new ParsedOrdering(fromKey, toKey);
    }

    @Override
    public AdditionalConstraintTemplate visitTemporal_constraint_def(
            ChimpClassicParser.Temporal_constraint_defContext ctx) {
        if (ctx.unary_temporal_constraint_def() != null) {
            return visitUnary_temporal_constraint_def(ctx.unary_temporal_constraint_def());
        } else {
            return visitBinary_temporal_constraint_def(ctx.binary_temporal_constraint_def());
        }
    }

    @Override
    public AdditionalConstraintTemplate visitUnary_temporal_constraint_def(
            ChimpClassicParser.Unary_temporal_constraint_defContext ctx) {
        AllenIntervalConstraint.Type constraintType =
                visitUnary_temporal_constraint_type(ctx.unary_temporal_constraint_type());
        String from = visitId_or_task(ctx.id_or_task());
        return createAdditionalConstraintTemplate(constraintType, from, from, visitBounds_list(ctx.bounds_list()));
    }

    public AdditionalConstraintTemplate createAdditionalConstraintTemplate(AllenIntervalConstraint.Type constraintType,
                                              String from, String to, Bounds[] bounds) {
        AllenIntervalConstraint con;
        if (bounds.length > 0) {
            con = new AllenIntervalConstraint(constraintType, bounds);
        } else {
            con = new AllenIntervalConstraint(constraintType);
        }
        return new AdditionalConstraintTemplate(con, from, from);
    }

    @Override
    public AllenIntervalConstraint.Type visitUnary_temporal_constraint_type(
            ChimpClassicParser.Unary_temporal_constraint_typeContext ctx) {
        return AllenIntervalConstraint.Type.valueOf(ctx.getText());
    }

    @Override
    public AdditionalConstraintTemplate visitBinary_temporal_constraint_def(
            ChimpClassicParser.Binary_temporal_constraint_defContext ctx) {
        AllenIntervalConstraint.Type constraintType =
                visitBinary_temporal_constraint_type(ctx.binary_temporal_constraint_type());
        String from = visitId_or_task(ctx.id_or_task(0));
        String to = visitId_or_task(ctx.id_or_task(1));
        return createAdditionalConstraintTemplate(constraintType, from, to, visitBounds_list(ctx.bounds_list()));
    }

    @Override
    public AllenIntervalConstraint.Type visitBinary_temporal_constraint_type(ChimpClassicParser.Binary_temporal_constraint_typeContext ctx) {
        return AllenIntervalConstraint.Type.valueOf(ctx.getText());
    }

    @Override
    public Bounds[] visitBounds_list(ChimpClassicParser.Bounds_listContext ctx) {
        Bounds[] bounds = new Bounds[ctx.bounds().size()];
        for (int i = 0; i < ctx.bounds().size(); i++) {
            bounds[i] = visitBounds(ctx.bounds(i));
        }
        return bounds;
    }

    @Override
    public Bounds visitBounds(ChimpClassicParser.BoundsContext ctx) {
        Long lb = visitBound(ctx.bound(0));
        Long ub = visitBound(ctx.bound(1));
        return new Bounds(lb, ub);
    }

    @Override
    public Long visitBound(ChimpClassicParser.BoundContext ctx) {
        if (ctx.getText().equals("INF")) {
            return APSPSolver.INF;
        } else {
            return numberToLong(ctx.NUMBER());
        }
    }


    @Override
    public String visitId_or_task(ChimpClassicParser.Id_or_taskContext ctx) {
        return ctx.getText();
    }

    @Override
    public ResourceUsageTemplate visitResource_usage_def(ChimpClassicParser.Resource_usage_defContext ctx) {
        String fluentType = "";

        int[] resourceRequirementPositions = new int[ctx.param_item().size()];
        String[] resourceRequirements = new String[ctx.param_item().size()];
        String resourceName = ctx.NAME().getText();
        int usageLevel = numberToInt(ctx.NUMBER());

        for (int i = 0; i < ctx.param_item().size(); ++i) {
            UsageParam param = visitParam_item(ctx.param_item(i));
            resourceRequirementPositions[i] = param.position;
            resourceRequirements[i] = param.paramName;
        }
        return new ResourceUsageTemplate(resourceName, fluentType, resourceRequirementPositions,
                resourceRequirements, usageLevel);
    }

    public class UsageParam {
        public UsageParam(int position, String paramName) {
            this.position = position;
            this.paramName  = paramName;
        }

        public int position;
        public String paramName;
    }

    @Override
    public UsageParam visitParam_item(ChimpClassicParser.Param_itemContext ctx) {
        return new UsageParam(numberToInt(ctx.NUMBER()), ctx.NAME().getText());
    }


    public class ValueRestriction {

        public ValueRestriction(String varName, List<String> constants) {
            this.varName = varName;
            this.constants = constants;
        }

        public String varName;
        public List<String> constants;
    };

    @Override
    public ValueRestriction visitValue_restriction_def(ChimpClassicParser.Value_restriction_defContext ctx) {
        String varName = ctx.VAR_NAME().toString();
        List<String> constants = visitConstant_list(ctx.constant_list());
        return new ValueRestriction(varName, constants);
    }

    @Override
    public ValueRestriction visitNotvalue_restriction_def(ChimpClassicParser.Notvalue_restriction_defContext ctx) {
        String varName = ctx.VAR_NAME().toString();
        List<String> constants = visitConstant_list(ctx.constant_list());
        return new ValueRestriction(varName, constants);
    }

    @Override
    public Object visitTypevalue_restriction_def(ChimpClassicParser.Typevalue_restriction_defContext ctx) {
        throw new IllegalStateException("Type restrictions are not implemented");
    }

    @Override
    public SubDifferentDefinition visitVardifferent_def(ChimpClassicParser.Vardifferent_defContext ctx) {
        String fromVar = ctx.VAR_NAME(0).getText();
        String toVar = ctx.VAR_NAME(1).getText();
        return new SubDifferentDefinition(fromVar, toVar);
    }

    @Override
    public List<String> visitConstant_list(ChimpClassicParser.Constant_listContext ctx) {
        List<String> constants = new ArrayList<>();
        for (TerminalNode n : ctx.NAME()) {
            constants.add(n.getText());
        }
        return constants;
    }

    @Override
    public Predicate visitPredicate(ChimpClassicParser.PredicateContext ctx) {
        Predicate predicate = new Predicate();
        predicate.name = ctx.NAME().getText();
        predicate.args = visitPredicate_args(ctx.predicate_args());
        return predicate;
    }

    @Override
    public List<String> visitPredicate_args(ChimpClassicParser.Predicate_argsContext ctx) {
        List<String> predicateArgs = new ArrayList<>();
        for (ChimpClassicParser.Var_or_constContext vctx : ctx.var_or_const()) {
            predicateArgs.add(visitVar_or_const(vctx));
        }
        return predicateArgs;
    }

    @Override
    public String visitId(ChimpClassicParser.IdContext ctx) {
        return ctx.NAME().getText();
    }

    @Override
    public String visitVar_or_const(ChimpClassicParser.Var_or_constContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visit(ParseTree tree) {
        return null;
    }

    @Override
    public Object visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        return null;
    }

    private static int numberToInt(TerminalNode number) {
        return Integer.valueOf(number.getText());
    }

    private static Long numberToLong(TerminalNode number) {
        return Long.valueOf(number.getText());
    }

}
