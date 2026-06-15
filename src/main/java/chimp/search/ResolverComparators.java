package chimp.search;

import java.util.Comparator;

public final class ResolverComparators {
    public static final ResolverOrderingCriterion UNIFICATION_FIRST = (left, right) ->
            Boolean.compare(
                    right.metadata().kind() == ResolverCandidateKind.TASK_UNIFICATION,
                    left.metadata().kind() == ResolverCandidateKind.TASK_UNIFICATION);
    public static final ResolverOrderingCriterion DEEPER_TASK_FIRST = (left, right) ->
            Integer.compare(right.metadata().depth(), left.metadata().depth());
    public static final ResolverOrderingCriterion SHALLOWER_TASK_FIRST = (left, right) ->
            Integer.compare(left.metadata().depth(), right.metadata().depth());
    public static final ResolverOrderingCriterion FEWER_SUBTASKS_FIRST = (left, right) ->
            Integer.compare(left.metadata().subtaskCount(), right.metadata().subtaskCount());
    public static final ResolverOrderingCriterion HIGHER_WEIGHT_FIRST = (left, right) -> {
        if (left.metadata().weight().isEmpty() || right.metadata().weight().isEmpty()) {
            return 0;
        }
        return Integer.compare(right.metadata().weight().getAsInt(), left.metadata().weight().getAsInt());
    };
    public static final ResolverOrderingCriterion EARLIER_TASK_FIRST = (left, right) ->
            Long.compare(left.metadata().taskEst(), right.metadata().taskEst());
    public static final ResolverOrderingCriterion NEWER_BINDINGS_FIRST = (left, right) ->
            Long.compare(
                    right.metadata().selectedPreconditionEstSum(),
                    left.metadata().selectedPreconditionEstSum());
    public static final ResolverOrderingCriterion OLDER_BINDINGS_FIRST = (left, right) ->
            Long.compare(
                    left.metadata().selectedPreconditionEstSum(),
                    right.metadata().selectedPreconditionEstSum());

    private ResolverComparators() {
    }

    public static Comparator<ResolverCandidate> lexicographic(Comparator<ResolverCandidate>... criteria) {
        return (left, right) -> {
            for (Comparator<ResolverCandidate> criterion : criteria) {
                int result = criterion.compare(left, right);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }
}
