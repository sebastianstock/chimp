package chimp.search;

import java.util.Comparator;

import static chimp.search.ResolverComparators.*;

public final class ResolverOrderingPolicies {
    public static final Comparator<ResolverCandidate> DEEPEST_FEWEST_SUBTASKS_NEWEST =
            lexicographic(DEEPER_TASK_FIRST, FEWER_SUBTASKS_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> DEEPEST_NEWEST =
            lexicographic(DEEPER_TASK_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> DEEPEST_WEIGHT_NEWEST =
            lexicographic(DEEPER_TASK_FIRST, HIGHER_WEIGHT_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> DEEPEST_WEIGHT_OLDEST =
            lexicographic(DEEPER_TASK_FIRST, HIGHER_WEIGHT_FIRST, OLDER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> SHALLOW_FEWEST_SUBTASKS_NEWEST =
            lexicographic(SHALLOWER_TASK_FIRST, FEWER_SUBTASKS_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_DEEPEST_WEIGHT_NEWEST =
            lexicographic(UNIFICATION_FIRST, DEEPER_TASK_FIRST, HIGHER_WEIGHT_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_DEEPEST_WEIGHT_OLDEST =
            lexicographic(UNIFICATION_FIRST, DEEPER_TASK_FIRST, HIGHER_WEIGHT_FIRST, OLDER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_EARLIEST_TASKS =
            lexicographic(UNIFICATION_FIRST, EARLIER_TASK_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_FEWEST_SUBTASKS_EARLIEST_NEWEST =
            lexicographic(UNIFICATION_FIRST, FEWER_SUBTASKS_FIRST, EARLIER_TASK_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_FEWEST_SUBTASKS_NEWEST =
            lexicographic(UNIFICATION_FIRST, FEWER_SUBTASKS_FIRST, NEWER_BINDINGS_FIRST);
    public static final Comparator<ResolverCandidate> UNIFY_WEIGHT_NEWEST =
            lexicographic(UNIFICATION_FIRST, HIGHER_WEIGHT_FIRST, NEWER_BINDINGS_FIRST);

    private ResolverOrderingPolicies() {
    }
}
