package chimp.kernel.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.model.FluentMarking;
import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;
import chimp.model.ProblemMarking;
import chimp.model.ProblemOrderingEdge;
import chimp.model.ProblemTemporalConstraint;
import chimp.model.ResourceRequirement;
import htn.HTNMetaConstraint;
import hybridDomainParsing.ClassicHybridDomain;
import resourceFluent.ResourceUsageTemplate;

/**
 * CHIMP-native problem compiler — populates a {@link FluentNetworkSolver}
 * from a {@link ProblemDefinition}.
 */
public final class ProblemCompiler {

    private final ResourceRequirementAdapter resourceAdapter = new ResourceRequirementAdapter();

    public void compile(
            ProblemDefinition problem,
            FluentNetworkSolver fluentSolver,
            ClassicHybridDomain domain) {
        Map<String, Variable> variables = new LinkedHashMap<>();

        for (InitialFluentDefinition definition : problem.initialFluents()) {
            Fluent fluent = (Fluent) fluentSolver.createVariable();
            setName(fluent, definition.signature().predicate(), definition.signature().arguments());
            for (int i = 0; i < definition.integerConstants().size(); i++) {
                fluent.getIntegerVariables()[i]
                        .setConstantValue(definition.integerConstants().get(i));
            }
            fluent.setMarking(toMarking(definition.marking()));
            variables.put(definition.id(), fluent);
        }

        Set<String> operatorNames = domain.getOperatorNames();
        for (InitialTaskDefinition definition : problem.initialTasks()) {
            String component = operatorNames.contains(definition.signature().predicate())
                    ? Fluent.ACTIVITY_TYPE_STR
                    : Fluent.TASK_TYPE_STR;
            Fluent task = (Fluent) fluentSolver.createVariable(component);
            setName(task, definition.signature().predicate(), definition.signature().arguments());
            task.setMarking(toMarking(definition.marking()));
            variables.put(definition.id(), task);
        }

        List<Constraint> temporalConstraints = new ArrayList<>();
        for (ProblemTemporalConstraint definition : problem.temporalConstraints()) {
            TemporalConstraintAdapter adapter = new TemporalConstraintAdapter();
            TemporalConstraint constraint = adapter.toTemporalConstraint(definition.template());
            constraint.setFrom(requireVariable(variables, definition.fromId()));
            constraint.setTo(requireVariable(variables, definition.toId()));
            temporalConstraints.add(constraint);
        }
        fluentSolver.addConstraints(temporalConstraints.toArray(Constraint[]::new));

        List<Constraint> orderingConstraints = new ArrayList<>();
        for (ProblemOrderingEdge edge : problem.orderingEdges()) {
            FluentConstraint constraint = new FluentConstraint(FluentConstraint.Type.BEFORE);
            constraint.setFrom(requireVariable(variables, edge.fromId()));
            constraint.setTo(requireVariable(variables, edge.toId()));
            orderingConstraints.add(constraint);
        }
        fluentSolver.addConstraints(orderingConstraints.toArray(Constraint[]::new));

        fluentSolver.addConstraints(createResourceUsageConstraints(
                domain.getFluentResourceUsages(), variables.values()));
    }

    private void setName(Fluent fluent, String predicate, List<String> arguments) {
        fluent.setName(predicate, arguments.toArray(String[]::new));
    }

    private int toMarking(ProblemMarking marking) {
        return switch (marking) {
            case OPEN -> FluentMarking.OPEN.ordinal();
            case UNPLANNED -> FluentMarking.UNPLANNED.ordinal();
        };
    }

    private Variable requireVariable(Map<String, Variable> variables, String id) {
        Variable variable = variables.get(id);
        if (variable == null) {
            throw new IllegalArgumentException("Problem constraint references unknown ID: " + id);
        }
        return variable;
    }

    private Constraint[] createResourceUsageConstraints(
            List<ResourceRequirement> requirements,
            Collection<Variable> variables) {
        List<Constraint> constraints = new ArrayList<>();
        Map<String, List<ResourceRequirement>> requirementsByPredicate =
                HTNMetaConstraint.createResourceUsagesMap(requirements);
        for (Variable variable : variables) {
            chimp.kernel.symbolic.CompoundSymbolicVariable symbolic =
                    ((Fluent) variable).getCompoundSymbolicVariable();
            List<ResourceRequirement> matching =
                    requirementsByPredicate.get(symbolic.getPredicateName());
            if (matching != null) {
                for (ResourceRequirement requirement : matching) {
                    ResourceUsageTemplate template =
                            resourceAdapter.toResourceUsageTemplate(requirement);
                    FluentConstraint constraint = new FluentConstraint(
                            FluentConstraint.Type.RESOURCEUSAGE, template);
                    constraint.setFrom(variable);
                    constraint.setTo(variable);
                    constraints.add(constraint);
                }
            }
        }
        return constraints.toArray(Constraint[]::new);
    }

    /** Small helper that converts problem templates to CHIMP-native temporal constraints. */
    private static final class TemporalConstraintAdapter {
        TemporalConstraint toTemporalConstraint(chimp.model.TemporalConstraintTemplate template) {
            if (template.bounds().isEmpty()) {
                return new TemporalConstraint(template.relation());
            }
            return new TemporalConstraint(template.relation(),
                    template.bounds().toArray(chimp.model.TemporalBounds[]::new));
        }
    }
}
