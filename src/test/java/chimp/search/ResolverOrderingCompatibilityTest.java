package chimp.search;

import htn.valOrderingHeuristics.CHIMPValOH;
import htn.valOrderingHeuristics.DeepestFewestsubsNewestbindingsValOH;
import htn.valOrderingHeuristics.DeepestNewestbindingsValOH;
import htn.valOrderingHeuristics.DeepestWeightNewestbindingsValOH;
import htn.valOrderingHeuristics.DeepestWeightOldestbindingsValOH;
import htn.valOrderingHeuristics.ShallowFewestsubsNewestbindingsValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightOldestbindingsValOH;
import htn.valOrderingHeuristics.UnifyEarlisttasksValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsNewestbindingsValOH;
import htn.valOrderingHeuristics.UnifyWeightNewestbindingsValOH;
import org.junit.Test;

import chimp.kernel.ConstraintNetwork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.Assert.assertEquals;

public class ResolverOrderingCompatibilityTest {

    @Test
    public void legacyHeuristicsMatchTheirNetworkPoliciesPairwise() {
        List<ResolverCandidate> candidates = List.of(
                expansion(1, 2, 3, 7, 20, 10),
                expansion(2, 4, 1, 2, 10, 30),
                expansionWithoutWeight(3, 4, 1, 10, 30),
                expansion(4, 2, 3, 7, 20, 10),
                unification(5, 0, 20));
        List<Case> cases = List.of(
                new Case(new DeepestFewestsubsNewestbindingsValOH(), ResolverOrderingPolicies.DEEPEST_FEWEST_SUBTASKS_NEWEST),
                new Case(new DeepestNewestbindingsValOH(), ResolverOrderingPolicies.DEEPEST_NEWEST),
                new Case(new DeepestWeightNewestbindingsValOH(), ResolverOrderingPolicies.DEEPEST_WEIGHT_NEWEST),
                new Case(new DeepestWeightOldestbindingsValOH(), ResolverOrderingPolicies.DEEPEST_WEIGHT_OLDEST),
                new Case(new ShallowFewestsubsNewestbindingsValOH(), ResolverOrderingPolicies.SHALLOW_FEWEST_SUBTASKS_NEWEST),
                new Case(new UnifyDeepestWeightNewestbindingsValOH(), ResolverOrderingPolicies.UNIFY_DEEPEST_WEIGHT_NEWEST),
                new Case(new UnifyDeepestWeightOldestbindingsValOH(), ResolverOrderingPolicies.UNIFY_DEEPEST_WEIGHT_OLDEST),
                new Case(new UnifyEarlisttasksValOH(), ResolverOrderingPolicies.UNIFY_EARLIEST_TASKS),
                new Case(new UnifyFewestsubsEarliesttasksNewestbindingsValOH(), ResolverOrderingPolicies.UNIFY_FEWEST_SUBTASKS_EARLIEST_NEWEST),
                new Case(new UnifyFewestsubsNewestbindingsValOH(), ResolverOrderingPolicies.UNIFY_FEWEST_SUBTASKS_NEWEST),
                new Case(new UnifyWeightNewestbindingsValOH(), ResolverOrderingPolicies.UNIFY_WEIGHT_NEWEST));

        for (Case testCase : cases) {
            for (ResolverCandidate left : candidates) {
                for (ResolverCandidate right : candidates) {
                    ConstraintNetwork leftNetwork = annotated(left);
                    ConstraintNetwork rightNetwork = annotated(right);
                    assertEquals(
                            Integer.signum(testCase.expected.compare(left, right)),
                            Integer.signum(testCase.legacy.compare(leftNetwork, rightNetwork)));
                }
            }
        }
    }

    @Test
    public void missingRepositoryWeightIsAWeightTie() {
        ResolverCandidate weighted = expansion(1, 1, 0, 100, 0, 1);
        ResolverCandidate unweighted = expansionWithoutWeight(2, 1, 0, 0, 2);

        assertEquals(0, ResolverComparators.HIGHER_WEIGHT_FIRST.compare(weighted, unweighted));
        assertEquals(0, ResolverComparators.HIGHER_WEIGHT_FIRST.compare(unweighted, weighted));
    }

    @Test
    public void representativeCandidatesKeepUnificationThenDeclarationOrderOnTies() {
        ResolverCandidate operator = expansion(1, 1, 0, 4, 10, 0);
        ResolverCandidate method = expansion(2, 1, 2, 4, 10, 0);
        ResolverCandidate unification = unification(3, 0, 10);
        List<ResolverCandidate> candidates = new ArrayList<>(List.of(operator, method, unification));

        candidates.sort(ResolverOrderingPolicies.UNIFY_FEWEST_SUBTASKS_NEWEST);

        assertEquals(List.of(unification, operator, method), candidates);
    }

    private ConstraintNetwork annotated(ResolverCandidate candidate) {
        ConstraintNetwork network = new ConstraintNetwork();
        network.setSpecilizedAnnotation(candidate);
        return network;
    }

    private ResolverCandidate expansion(
            long taskId, int depth, int subtasks, int weight, long taskEst, long preconditionEstSum) {
        return candidate(taskId, depth, subtasks, OptionalInt.of(weight), taskEst, preconditionEstSum);
    }

    private ResolverCandidate expansionWithoutWeight(
            long taskId, int depth, int subtasks, long taskEst, long preconditionEstSum) {
        return candidate(taskId, depth, subtasks, OptionalInt.empty(), taskEst, preconditionEstSum);
    }

    private ResolverCandidate candidate(
            long taskId, int depth, int subtasks, OptionalInt weight, long taskEst, long preconditionEstSum) {
        ResolverCandidateMetadata metadata = new ResolverCandidateMetadata(
                ResolverCandidateKind.EXPANSION, taskId, OptionalInt.of((int) taskId),
                OptionalLong.empty(), depth, weight, subtasks, taskEst, preconditionEstSum, 0);
        chimp.model.ResolverDescription description = new chimp.model.ResolverDescription(
                (int) taskId, "item-" + taskId, depth, weight.orElse(0),
                java.util.Map.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), 0);
        return new ResolverCandidate(metadata, new ExpansionCandidateDescription(description));
    }

    private ResolverCandidate unification(long taskId, int depth, long taskEst) {
        ResolverCandidateMetadata metadata = new ResolverCandidateMetadata(
                ResolverCandidateKind.TASK_UNIFICATION, taskId, OptionalInt.empty(),
                OptionalLong.of(taskId + 100), depth, OptionalInt.empty(), 0, taskEst, 0, 0);
        return new ResolverCandidate(metadata, new TaskUnificationCandidateDescription(taskId + 100));
    }

    private record Case(CHIMPValOH legacy, Comparator<ResolverCandidate> expected) {
    }
}
