package chimp.adapter.metacsp;

import chimp.search.ResolverCandidate;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;

import java.util.Comparator;
import java.util.Objects;

public class MetaCspResolverOrderingAdapter extends ValueOrderingH {
    private static final MetaCspSearchAdapter SEARCH_ADAPTER = new MetaCspSearchAdapter();

    private final Comparator<ResolverCandidate> comparator;

    public MetaCspResolverOrderingAdapter(Comparator<ResolverCandidate> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    @Override
    public int compare(ConstraintNetwork left, ConstraintNetwork right) {
        return comparator.compare(
                SEARCH_ADAPTER.toResolverCandidate(left),
                SEARCH_ADAPTER.toResolverCandidate(right));
    }
}
