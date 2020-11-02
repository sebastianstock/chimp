package hybridDomainParsing.classic.antlr;

import com.google.common.primitives.Ints;
import htn.AdditionalConstraintTemplate;
import htn.EffectTemplate;
import htn.HTNMethod;
import htn.HTNOperator;
import htn.HTNPrecondition;
import htn.IntArg;
import htn.IntegerConstraintTemplate;
import htn.OrderingConstraintTemplate;
import htn.PlanReportroryItem;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.SubDifferentDefinition;
import integers.IntegerConstraint;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;

import java.io.IOException;
import java.util.*;

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
        public int maxIntArgs; // Maximum number of integer variables of a fluent.
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

        @Override
        public int getMaxIntArgs() {
            return maxIntArgs;
        }

        @Override
        public int getMinIntValue() {
            return 0; // TODO Parse this
        }

        @Override
        public int getMaxIntValue() {
            return 10000; // TODO Parse this
        }

        @Override
        public String[] getPredicateSymbols() {
            return predicateSymbols.toArray(new String[predicateSymbols.size()]);
        }

        @Override
        public Set<String> getOperatorNames() {
            Set<String> operatorNames = new HashSet<>();
            for (PlanReportroryItem operator : operators) {
                operatorNames.add(operator.getName());
            }
            return operatorNames;
        }

        /**
         * Set the components of all method's effects to Activity iff they are planned by an operator.
         */
        private void upateEffectComponents() {
            Set<String> operatorNames = getOperatorNames();
            for (PlanReportroryItem method : methods) {
                for (EffectTemplate effect : method.getEffects()) {
                    if (operatorNames.contains(effect.getName())) {
                        effect.setComponentToActivity();
                    }
                }
            }
        }
    }

    public static final String HEAD_KEYWORD_STRING = "task";

    public static final String EFFECT_COMPONENT = "PlannedState";
    public static final String SUBTASK_COMPONENT = "Task";
    public static final int DEFAULT_PREFERENCE_WEIGHT = 1;

    private int maxArgs = -1;
    private Map<String, String[]> typesInstancesMap;

    public static ParsedDomain parseDomainFromFile(String domainPath, Map<String, String[]> typesInstancesMap)
            throws DomainParsingException {
        ChimpClassicLexer lexer = null;
        try {
            lexer = new ChimpClassicLexer(CharStreams.fromFileName(domainPath));
        } catch (IOException e) {
            throw new DomainParsingException(e.getMessage());
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChimpClassicParser parser = new ChimpClassicParser(tokens);
        ChimpClassicReader visitor = new ChimpClassicReader(typesInstancesMap);
        return visitor.visitDomain(parser.domain());
    }

    public ChimpClassicReader(Map<String, String[]> typesInstancesMap) {
        this.typesInstancesMap = typesInstancesMap;
    }

    @Override
    public ParsedDomain visitDomain(ChimpClassicParser.DomainContext ctx) {
        ParsedDomain ret = new ParsedDomain();
        ret.name = visitDomain_name_def(ctx.domain_name_def());
        ret.maxArgs = visitMaxargs_def(ctx.maxargs_def());
        if (ctx.maxintargs_def() != null) {
            ret.maxIntArgs = visitMaxintargs_def(ctx.maxintargs_def());
        } else {
            ret.maxIntArgs = 0;
        }
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
            } else if (d instanceof ChimpClassicParser.Domain_item_fluentresourceusageContext) {
                ret.fluentResourceUsages.add(visitDomain_item_fluentresourceusage(
                        (ChimpClassicParser.Domain_item_fluentresourceusageContext) d));
            }
        }
        ret.upateEffectComponents();
        return ret;
    }

    @Override
    public FluentResourceUsageScheduler visitDomain_item_resource(ChimpClassicParser.Domain_item_resourceContext ctx) {
        return visitResource_def(ctx.resource_def());
    }

    @Override
    public ResourceUsageTemplate visitDomain_item_fluentresourceusage(
            ChimpClassicParser.Domain_item_fluentresourceusageContext ctx) {
        return visitFluentresourceusage_def(ctx.fluentresourceusage_def());
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
    public Integer visitMaxintargs_def(ChimpClassicParser.Maxintargs_defContext ctx) {
        if (ctx.NUMBER() != null) {
            return numberToInt(ctx.NUMBER());
        } else {
            return 0;
        }
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
        List<ResourceUsageTemplate> resourceUsageTemplates = new ArrayList<>();
        List<IntegerConstraintTemplate> integerConstraintTemplates = new ArrayList<>();
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
            } else if (d instanceof ChimpClassicParser.Typevalue_restriction_m_elementContext) {
                parsedValueRestrictions.add(visitTypevalue_restriction_m_element(
                        (ChimpClassicParser.Typevalue_restriction_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Nottypevalue_restriction_m_elementContext) {
                parsedNotvalueRestrictions.add(visitNottypevalue_restriction_m_element(
                        (ChimpClassicParser.Nottypevalue_restriction_m_elementContext) d));
            }else if (d instanceof ChimpClassicParser.Vardifferent_m_elementContext) {
                subDifferentDefinitions.add(visitVardifferent_m_element(
                        (ChimpClassicParser.Vardifferent_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Temporal_constraint_m_elementContext) {
                temporalConstraintTemplates.add(visitTemporal_constraint_m_element(
                        (ChimpClassicParser.Temporal_constraint_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Integer_constraint_m_elementContext) {
                integerConstraintTemplates.add(visitInteger_constraint_m_element(
                        (ChimpClassicParser.Integer_constraint_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Subtask_def_m_elementContext) {
                parsedSubtasks.add(visitSubtask_def_m_element(
                        (ChimpClassicParser.Subtask_def_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Ordering_def_m_elementContext) {
                parsedOrderingConstraints.add(
                        visitOrdering_def_m_element((ChimpClassicParser.Ordering_def_m_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Resource_usage_m_elementContext) {
                resourceUsageTemplates.add(visitResource_usage_m_element(
                    (ChimpClassicParser.Resource_usage_m_elementContext) d));
            }
        }

        HTNPrecondition[] htnPreconditions = createHTNPreconditionsAndNegativeEffects(head,
                parsedPreconditions, new ArrayList<String>());
        addTemporalConstraintsToPreconditions(temporalConstraintTemplates, htnPreconditions);

        EffectTemplate[] effectTemplates = createEffectTemplates(parsedSubtasks, SUBTASK_COMPONENT);
        addTemporalConstraintsToEffects(temporalConstraintTemplates, effectTemplates);

        OrderingConstraintTemplate[] orderingConstraints = createOrderingConstraints(parsedOrderingConstraints, effectTemplates);
        HTNMethod method = new HTNMethod(head.name, head.args.toArray(new String[head.args.size()]), head.integerArgs,
                htnPreconditions, effectTemplates, orderingConstraints, preferenceWeight);

        method.addVariablesPossibleValues(createVariablesValuesMap(parsedValueRestrictions));
        method.setVariablesImpossibleValuesMap(createVariablesValuesMap(parsedNotvalueRestrictions));

        method.setSubDifferentDefinitions(
                subDifferentDefinitions.toArray(new SubDifferentDefinition[subDifferentDefinitions.size()]));

        // set additional constraints from head to head or between preconditions and effects
        method.setAdditionalConstraints(filterAdditionalConstraints(temporalConstraintTemplates));

        method.setIntegerConstraintTemplates(
                integerConstraintTemplates.toArray(new IntegerConstraintTemplate[integerConstraintTemplates.size()]));

        method.addResourceUsageTemplates(resourceUsageTemplates);

        return method;
    }

    private OrderingConstraintTemplate[] createOrderingConstraints(List<ParsedOrdering> parsedOrderings,
                                                           EffectTemplate[] subtasks) {
        Map<String, EffectTemplate> keyToEffectTemplatesMap = new HashMap<String, EffectTemplate>();
        for (EffectTemplate et : subtasks) {
            keyToEffectTemplatesMap.put(et.getKey(), et);
        }

        List<OrderingConstraintTemplate> ret = new ArrayList<OrderingConstraintTemplate>();
        for (ParsedOrdering parsedOrdering : parsedOrderings) {

            if (!keyToEffectTemplatesMap.containsKey(parsedOrdering.fromKey)) {
                throw new IllegalStateException("Error while parsing 'Ordering': (from) key " +
                        parsedOrdering.fromKey + " is not a subtask of the method");
            }
            if (!keyToEffectTemplatesMap.containsKey(parsedOrdering.toKey)) {
                throw new IllegalStateException("Error while parsing 'Ordering': (to) key " +
                        parsedOrdering.toKey + " is not a subtask of the method");
            }

            ret.add(new OrderingConstraintTemplate(keyToEffectTemplatesMap.get(parsedOrdering.fromKey),
                            keyToEffectTemplatesMap.get(parsedOrdering.toKey)));
        }
        return ret.toArray(new OrderingConstraintTemplate[ret.size()]);
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
        List<IntegerConstraintTemplate> integerConstraintTemplates = new ArrayList<>();
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
            } else if (d instanceof ChimpClassicParser.Typevalue_restriction_op_elementContext) {
                parsedValueRestrictions.add(visitTypevalue_restriction_op_element(
                        (ChimpClassicParser.Typevalue_restriction_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Nottypevalue_restriction_op_elementContext) {
                parsedNotvalueRestrictions.add(visitNottypevalue_restriction_op_element(
                        (ChimpClassicParser.Nottypevalue_restriction_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Vardifferent_op_elementContext) {
                subDifferentDefinitions.add(visitVardifferent_op_element(
                        (ChimpClassicParser.Vardifferent_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Temporal_constraint_op_elementContext) {
                temporalConstraintTemplates.add(visitTemporal_constraint_op_element(
                        (ChimpClassicParser.Temporal_constraint_op_elementContext) d));
            } else if (d instanceof ChimpClassicParser.Integer_constraint_op_elementContext) {
                integerConstraintTemplates.add(visitInteger_constraint_op_element(
                        (ChimpClassicParser.Integer_constraint_op_elementContext) d));
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

        HTNOperator op = new HTNOperator(head.name, head.args.toArray(new String[head.args.size()]), head.integerArgs,
                htnPreconditions, effectTemplates, preferenceWeight);

        op.addVariablesPossibleValues(createVariablesValuesMap(parsedValueRestrictions));
        op.setVariablesImpossibleValuesMap(createVariablesValuesMap(parsedNotvalueRestrictions));

        op.setSubDifferentDefinitions(
                subDifferentDefinitions.toArray(new SubDifferentDefinition[subDifferentDefinitions.size()]));

        // set additional constraints from head to head or between preconditions and effects
        op.setAdditionalConstraints(filterAdditionalConstraints(temporalConstraintTemplates));

        op.setIntegerConstraintTemplates(
                integerConstraintTemplates.toArray(new IntegerConstraintTemplate[integerConstraintTemplates.size()]));

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
                    p.predicate.integerArgs,
                    maxArgs,
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
        IntArg[] integerArgs;
    }

    public class PredicateWithId {
        String id;
        Predicate predicate;
    }

    @Override
    public Predicate visitHead(ChimpClassicParser.HeadContext ctx) {
        Predicate head = new Predicate();
        head.name = visitPredicate_symbol(ctx.predicate_symbol());
        head.args = visitPredicate_args(ctx.predicate_args());
        if (ctx.int_args_def() != null) {
            head.integerArgs = visitInt_args_def(ctx.int_args_def());
        } else {
            head.integerArgs = new IntArg[0];
        }
        return head;
    }

    @Override
    public IntArg[] visitInt_args_def(ChimpClassicParser.Int_args_defContext ctx) {
        if (ctx == null) {
            return new IntArg[0];
        }
        if (ctx.int_args() != null) {
            return visitInt_args(ctx.int_args());
        } else {
            return new IntArg[0];
        }
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
    public ResourceUsageTemplate visitResource_usage_m_element(ChimpClassicParser.Resource_usage_m_elementContext ctx) {
        return visitResource_usage_def(ctx.resource_usage_def());
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
    public ValueRestriction visitTypevalue_restriction_m_element(ChimpClassicParser.Typevalue_restriction_m_elementContext ctx) {
        return visitTypevalue_restriction_def(ctx.typevalue_restriction_def());
    }

    @Override
    public ValueRestriction visitNottypevalue_restriction_m_element(ChimpClassicParser.Nottypevalue_restriction_m_elementContext ctx) {
        return visitNottypevalue_restriction_def(ctx.nottypevalue_restriction_def());
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
    public IntegerConstraintTemplate visitInteger_constraint_m_element(ChimpClassicParser.Integer_constraint_m_elementContext ctx) {
        return visitInteger_constraint_def(ctx.integer_constraint_def());
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
    public ValueRestriction visitTypevalue_restriction_op_element(ChimpClassicParser.Typevalue_restriction_op_elementContext ctx) {
        return visitTypevalue_restriction_def(ctx.typevalue_restriction_def());
    }

    @Override
    public ValueRestriction visitNottypevalue_restriction_op_element(ChimpClassicParser.Nottypevalue_restriction_op_elementContext ctx) {
        return visitNottypevalue_restriction_def(ctx.nottypevalue_restriction_def());
    }

    @Override
    public SubDifferentDefinition visitVardifferent_op_element(ChimpClassicParser.Vardifferent_op_elementContext ctx) {
        return visitVardifferent_def(ctx.vardifferent_def());
    }

    @Override
    public IntegerConstraintTemplate visitInteger_constraint_op_element(ChimpClassicParser.Integer_constraint_op_elementContext ctx) {
        return visitInteger_constraint_def(ctx.integer_constraint_def());
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
                parsedPrecondition.predicate.integerArgs,
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
    public IntegerConstraintTemplate visitInteger_constraint_def(ChimpClassicParser.Integer_constraint_defContext ctx) {
        if(ctx.integer_constraint1_def() != null) {
            return visitInteger_constraint1_def(ctx.integer_constraint1_def());
        } else {
            return visitInteger_constraint2_def((ctx.integer_constraint2_def()));
        }
    }

    @Override
    public IntegerConstraintTemplate visitInteger_constraint1_def(ChimpClassicParser.Integer_constraint1_defContext ctx) {
        String v0 = ctx.VAR_NAME().getText();
        IntArg intArg1 = visitVar_or_int(ctx.var_or_int());
        String op1 = visitInteger_operator1(ctx.integer_operator1());
        String op2 = null;
        String[] varKeys;
        int cste = 0;
        if (intArg1.isVariable()) {
            varKeys = new String[] {v0, intArg1.varName};
        } else {
            varKeys = new String[] {v0};
            cste = intArg1.constValue;
        }
        return new IntegerConstraintTemplate(IntegerConstraint.Type.ARITHM, varKeys, op1, op2, cste);
    }

    @Override
    public IntegerConstraintTemplate visitInteger_constraint2_def(ChimpClassicParser.Integer_constraint2_defContext ctx) {
        String v0 = ctx.VAR_NAME(0).getText();
        String v1 = ctx.VAR_NAME(1).getText();
        IntArg intArg2 = visitVar_or_int(ctx.var_or_int());
        String op1 = visitInteger_operator1(ctx.integer_operator1());
        String op2 = visitInteger_operator2(ctx.integer_operator2());
        String[] varKeys;
        int cste = 0;
        if (intArg2.isVariable()) {
            varKeys = new String[] {v0, v1, intArg2.varName};
        } else {
            varKeys = new String[] {v0, v1};
            cste = intArg2.constValue;
        }
        return new IntegerConstraintTemplate(IntegerConstraint.Type.ARITHM, varKeys, op1, op2, cste);
    }

    @Override
    public String visitInteger_operator1(ChimpClassicParser.Integer_operator1Context ctx) {
        return ctx.getText();
    }

    @Override
    public String visitInteger_operator2(ChimpClassicParser.Integer_operator2Context ctx) {
        return ctx.getText();
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
        return new AdditionalConstraintTemplate(con, from, to);
    }

    @Override
    public AllenIntervalConstraint.Type visitUnary_temporal_constraint_type(
            ChimpClassicParser.Unary_temporal_constraint_typeContext ctx) {
        return AllenIntervalConstraint.Type.valueOf(ctx.getText().split(" ")[1]);
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
        String resourceName;
        int usageLevel;

        if (ctx.NAME() != null) {
            resourceName = ctx.NAME().getText();
            usageLevel = numberToInt(ctx.NUMBER());
        } else {
            ParsedUsage usage = visitUsage_def(ctx.usage_def());
            resourceName = usage.resourceName;
            usageLevel = usage.usageLevel;
            for (int i = 0; i < ctx.param_item().size(); ++i) {
                UsageParam param = visitParam_item(ctx.param_item(i));
                resourceRequirementPositions[i] = param.position;
                resourceRequirements[i] = param.paramName;
            }
        }
        return new ResourceUsageTemplate(resourceName, fluentType, resourceRequirementPositions,
                resourceRequirements, usageLevel);
    }

    @Override
    public ResourceUsageTemplate visitFluentresourceusage_def(ChimpClassicParser.Fluentresourceusage_defContext ctx) {
        String fluentType = visitFluent_def(ctx.fluent_def());

        String resourceName = ctx.usage_def().NAME().getText();
        int usageLevel = numberToInt(ctx.usage_def().NUMBER());

        int[] resourceRequirementPositions = new int[ctx.param_item().size()];
        String[] resourceRequirements = new String[ctx.param_item().size()];

        for (int i = 0; i < ctx.param_item().size(); ++i) {
            UsageParam param = visitParam_item(ctx.param_item(i));
            resourceRequirementPositions[i] = param.position;
            resourceRequirements[i] = param.paramName;
        }
        return new ResourceUsageTemplate(resourceName, fluentType, resourceRequirementPositions,
                resourceRequirements, usageLevel);
    }

    @Override
    public String visitFluent_def(ChimpClassicParser.Fluent_defContext ctx) {
        return ctx.NAME().getText();
    }

    class ParsedUsage {
        int usageLevel;
        String resourceName;

        public ParsedUsage(String resourceName, int usageLevel) {
            this.resourceName = resourceName;
            this.usageLevel = usageLevel;
        }
    }

    @Override
    public ParsedUsage visitUsage_def(ChimpClassicParser.Usage_defContext ctx) {
        return new ParsedUsage(ctx.NAME().getText(), numberToInt(ctx.NUMBER()));
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
    public ValueRestriction visitTypevalue_restriction_def(ChimpClassicParser.Typevalue_restriction_defContext ctx) {
        String varName = ctx.VAR_NAME().toString();
        List<String> types = visitConstant_list(ctx.constant_list());
        List<String> values = new ArrayList<>();
        for (String type : types) {
            String[] instances = typesInstancesMap.get(type);
            if (instances != null) {
                for (String instance : instances) {
                    values.add(instance);
                }
            } else {
                System.err.println("Type " + type + " specified but not in typesInstancesMap");
//                throw new DomainParsingException("Type " + type + " specified but not in typesInstancesMap");
            }
        }
        return new ValueRestriction(varName, values);
    }

    @Override
    public ValueRestriction visitNottypevalue_restriction_def(ChimpClassicParser.Nottypevalue_restriction_defContext ctx) {
        String varName = ctx.VAR_NAME().toString();
        List<String> types = visitConstant_list(ctx.constant_list());
        List<String> values = new ArrayList<>();
        for (String type : types) {
            String[] instances = typesInstancesMap.get(type);
            if (instances != null) {
                for (String instance : instances) {
                    values.add(instance);
                }
            } else {
                System.err.println("Type " + type + " specified but not in typesInstancesMap");
//                throw new DomainParsingException("Type " + type + " specified but not in typesInstancesMap");
            }
        }
        return new ValueRestriction(varName, values);
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
        if (ctx.int_args_def() != null) {
            predicate.integerArgs = visitInt_args_def(ctx.int_args_def());
        } else {
            predicate.integerArgs = new IntArg[0];
        }

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
    public IntArg[] visitInt_args(ChimpClassicParser.Int_argsContext ctx) {
        IntArg[] intArgs = new IntArg[ctx.var_or_int().size()];
        for (int i = 0; i < ctx.var_or_int().size(); i++) {
            intArgs[i] = visitVar_or_int(ctx.var_or_int().get(i));
        }
        return intArgs;
    }

    @Override
    public IntArg visitVar_or_int(ChimpClassicParser.Var_or_intContext ctx) {
        if (ctx.NUMBER() != null) {
            return new IntArg(numberToInt(ctx.NUMBER()));
        } else {
            return new IntArg(ctx.VAR_NAME().getText());
        }
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
