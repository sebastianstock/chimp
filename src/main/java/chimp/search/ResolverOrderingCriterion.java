package chimp.search;

import java.util.Comparator;

@FunctionalInterface
public interface ResolverOrderingCriterion extends Comparator<ResolverCandidate> {
}
