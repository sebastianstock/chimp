package chimp.adapter.metacsp;

import chimp.model.ResolverDescription;
import chimp.search.ExpansionCandidateDescription;
import chimp.search.PlanningFlaw;
import chimp.search.ResolverCandidate;
import chimp.search.ResolverCandidateKind;
import chimp.search.ResolverCandidateMetadata;
import chimp.search.TaskPlanningFlaw;
import chimp.search.TaskUnificationCandidateDescription;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import htn.PlanReportroryItem;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class MetaCspSearchAdapter {

    public void annotateFlaw(ConstraintNetwork network, TaskPlanningFlaw flaw) {
        network.setSpecilizedAnnotation(flaw);
    }

    public PlanningFlaw toPlanningFlaw(ConstraintNetwork network) {
        if (network.getSpecilizedAnnotation() instanceof PlanningFlaw flaw) {
            return flaw;
        }
        List<Long> taskIds = new ArrayList<>();
        for (Variable variable : network.getVariables()) {
            taskIds.add((long) variable.getID());
        }
        return new TaskPlanningFlaw(taskIds);
    }

    public void annotateExpansion(
            ConstraintNetwork network,
            Fluent task,
            ResolverDescription description) {
        ResolverCandidateMetadata metadata = metadata(
                network,
                task,
                ResolverCandidateKind.EXPANSION,
                OptionalInt.of(description.repositoryItemId()),
                OptionalLong.empty(),
                OptionalInt.of(description.preferenceWeight()),
                description.bindingCount());
        network.setSpecilizedAnnotation(new ResolverCandidate(
                metadata,
                new ExpansionCandidateDescription(description)));
    }

    public void annotateTaskUnification(ConstraintNetwork network, Fluent task, Fluent matchedTask) {
        ResolverCandidateMetadata metadata = metadata(
                network,
                task,
                ResolverCandidateKind.TASK_UNIFICATION,
                OptionalInt.empty(),
                OptionalLong.of(matchedTask.getID()),
                OptionalInt.empty(),
                0);
        network.setSpecilizedAnnotation(new ResolverCandidate(
                metadata,
                new TaskUnificationCandidateDescription(matchedTask.getID())));
    }

    public ResolverCandidate toResolverCandidate(ConstraintNetwork network) {
        if (network.getSpecilizedAnnotation() instanceof ResolverCandidate candidate) {
            return candidate;
        }
        FluentConstraint applied = find(network, FluentConstraint.Type.UNARYAPPLIED);
        if (applied == null) {
            throw new IllegalArgumentException("Resolver network has no UNARYAPPLIED constraint");
        }
        Fluent task = (Fluent) applied.getFrom();
        FluentConstraint matches = find(network, FluentConstraint.Type.MATCHES);
        if (matches != null) {
            Fluent matchedTask = (Fluent) matches.getTo();
            annotateTaskUnification(network, task, matchedTask);
            return (ResolverCandidate) network.getSpecilizedAnnotation();
        }

        PlanReportroryItem item = applied.getPlannedWith();
        if (item == null) {
            throw new IllegalArgumentException("Expansion resolver has no repository item");
        }
        LinkedHashMap<String, Long> selectedPreconditions = new LinkedHashMap<>();
        int selectedIndex = 0;
        for (Constraint constraint : network.getConstraints()) {
            if (constraint instanceof FluentConstraint fluentConstraint
                    && fluentConstraint.getType() == FluentConstraint.Type.PRE) {
                selectedPreconditions.put(
                        "precondition-" + selectedIndex++,
                        (long) fluentConstraint.getFrom().getID());
            }
        }
        ResolverDescription description = new ResolverDescription(
                item.getId(), item.getName(), depth(applied), item.getPreferenceWeight(),
                selectedPreconditions, List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), 0);
        annotateExpansion(network, task, description);
        return (ResolverCandidate) network.getSpecilizedAnnotation();
    }

    private ResolverCandidateMetadata metadata(
            ConstraintNetwork network,
            Fluent task,
            ResolverCandidateKind kind,
            OptionalInt repositoryItemId,
            OptionalLong matchedTaskId,
            OptionalInt weight,
            int bindingCount) {
        FluentConstraint applied = find(network, FluentConstraint.Type.UNARYAPPLIED);
        int subtaskCount = 0;
        long preconditionEstSum = 0;
        for (Constraint constraint : network.getConstraints()) {
            if (constraint instanceof FluentConstraint fluentConstraint) {
                if (fluentConstraint.getType() == FluentConstraint.Type.DC) {
                    subtaskCount++;
                } else if (fluentConstraint.getType() == FluentConstraint.Type.PRE) {
                    preconditionEstSum += ((Fluent) fluentConstraint.getFrom()).getAllenInterval().getEST();
                }
            }
        }
        return new ResolverCandidateMetadata(
                kind,
                task.getID(),
                repositoryItemId,
                matchedTaskId,
                applied == null ? 0 : depth(applied),
                weight,
                subtaskCount,
                task.getAllenInterval().getEST(),
                preconditionEstSum,
                bindingCount);
    }

    private FluentConstraint find(ConstraintNetwork network, FluentConstraint.Type type) {
        for (Constraint constraint : network.getConstraints()) {
            if (constraint instanceof FluentConstraint fluentConstraint
                    && fluentConstraint.getType() == type) {
                return fluentConstraint;
            }
        }
        return null;
    }

    private int depth(FluentConstraint applied) {
        try {
            return applied.getDepth();
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }
}
