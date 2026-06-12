package chimp.adapter.metacsp;

import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;
import chimp.model.ProblemMarking;
import chimp.model.ProblemOrderingEdge;
import chimp.model.ProblemTemporalConstraint;
import chimp.model.ResourceRequirement;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import hybridDomainParsing.ClassicHybridDomain;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MetaCspProblemCompiler {
    private final MetaCspTemporalConstraintAdapter temporalAdapter =
            new MetaCspTemporalConstraintAdapter();
    private final MetaCspResourceRequirementAdapter resourceAdapter =
            new MetaCspResourceRequirementAdapter();

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
            fluent.setMarking(toMetaCspMarking(definition.marking()));
            variables.put(definition.id(), fluent);
        }

        Set<String> operatorNames = domain.getOperatorNames();
        for (InitialTaskDefinition definition : problem.initialTasks()) {
            String component = operatorNames.contains(definition.signature().predicate())
                    ? Fluent.ACTIVITY_TYPE_STR
                    : Fluent.TASK_TYPE_STR;
            Fluent task = (Fluent) fluentSolver.createVariable(component);
            setName(task, definition.signature().predicate(), definition.signature().arguments());
            task.setMarking(toMetaCspMarking(definition.marking()));
            variables.put(definition.id(), task);
        }

        List<Constraint> temporalConstraints = new ArrayList<>();
        for (ProblemTemporalConstraint definition : problem.temporalConstraints()) {
            AllenIntervalConstraint constraint =
                    temporalAdapter.toAllenIntervalConstraint(definition.template());
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

    private Object toMetaCspMarking(ProblemMarking marking) {
        return switch (marking) {
            case OPEN -> HTNMetaConstraint.markings.OPEN;
            case UNPLANNED -> HTNMetaConstraint.markings.UNPLANNED;
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
            CompoundSymbolicVariable symbolic =
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
}
