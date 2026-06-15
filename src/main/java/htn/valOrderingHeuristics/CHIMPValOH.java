package htn.valOrderingHeuristics;

import chimp.adapter.metacsp.MetaCspResolverOrderingAdapter;
import chimp.search.ResolverCandidate;

import java.util.Comparator;

/** Meta-CSP compatibility adapter for CHIMP-owned resolver ordering policies. */
public abstract class CHIMPValOH extends MetaCspResolverOrderingAdapter {
    protected CHIMPValOH(Comparator<ResolverCandidate> comparator) {
        super(comparator);
    }
}
