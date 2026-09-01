package htn.valOrderingHeuristics;

import chimp.kernel.ConstraintNetwork;
import chimp.search.ChimpSearchAdapter;
import chimp.search.ResolverCandidate;

import java.util.Comparator;

/**
 * CHIMP-native resolver ordering heuristic.
 *
 * <p>Implements {@link Comparator}<{@link ConstraintNetwork}> so it can be used
 * directly as the value-ordering heuristic in {@link planner.CHIMP.CHIMPBuilder}.
 */
public abstract class CHIMPValOH implements Comparator<ConstraintNetwork> {

    private static final ChimpSearchAdapter SEARCH_ADAPTER = new ChimpSearchAdapter();
    private final Comparator<ResolverCandidate> comparator;

    protected CHIMPValOH(Comparator<ResolverCandidate> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(ConstraintNetwork left, ConstraintNetwork right) {
        return comparator.compare(
                SEARCH_ADAPTER.toResolverCandidate(left),
                SEARCH_ADAPTER.toResolverCandidate(right));
    }
}
