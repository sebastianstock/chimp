package chimp.adapter.metacsp;

import chimp.search.PlanningFlaw;
import chimp.search.ResolverCandidate;
import chimp.search.ResolverCandidateKind;
import chimp.search.TaskPlanningFlaw;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import org.junit.Test;
import org.metacsp.framework.ConstraintNetwork;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetaCspSearchAdapterTest {
    private final MetaCspSearchAdapter adapter = new MetaCspSearchAdapter();

    @Test
    public void flawAnnotationPreservesEligibleTaskOrder() {
        ConstraintNetwork network = new ConstraintNetwork(null);
        adapter.annotateFlaw(network, new TaskPlanningFlaw(List.of(9L, 3L, 12L)));

        PlanningFlaw flaw = adapter.toPlanningFlaw(network);

        assertEquals(List.of(9L, 3L, 12L), flaw.eligibleTaskIds());
    }

    @Test
    public void taskUnificationRoundTripsThroughAnnotation() {
        FluentNetworkSolver solver = solver();
        Fluent task = (Fluent) solver.createVariable(Fluent.TASK_TYPE_STR);
        task.setName("move", new String[] {"r1"});
        Fluent matched = (Fluent) solver.createVariable(Fluent.TASK_TYPE_STR);
        matched.setName("move", new String[] {"r1"});
        ConstraintNetwork network = new ConstraintNetwork(null);
        FluentConstraint matches = new FluentConstraint(FluentConstraint.Type.MATCHES);
        matches.setFrom(task);
        matches.setTo(matched);
        network.addConstraint(matches);
        FluentConstraint applied = new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED);
        applied.setFrom(task);
        applied.setTo(task);
        network.addConstraint(applied);

        adapter.annotateTaskUnification(network, task, matched);
        ResolverCandidate candidate = adapter.toResolverCandidate(network);

        assertEquals(ResolverCandidateKind.TASK_UNIFICATION, candidate.metadata().kind());
        assertEquals(task.getID(), candidate.metadata().taskId());
        assertEquals(matched.getID(), candidate.metadata().matchedTaskId().getAsLong());
        assertTrue(candidate.metadata().repositoryItemId().isEmpty());
    }

    private FluentNetworkSolver solver() {
        return new FluentNetworkSolver(
                0, 1000,
                new String[][] {{"move"}, {"r1"}},
                new int[] {1, 1});
    }
}
