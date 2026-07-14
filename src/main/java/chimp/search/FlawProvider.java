package chimp.search;

import java.util.Comparator;

/**
 * Provides flaws (open sub-problems) and resolver candidates for the
 * {@link ChimpSearchEngine} backtracking search. Each meta-constraint in
 * the planner implements this port.
 *
 * <p>The flaw representation is {@link ChimpFlaw} (CHIMP-owned, carries
 * task fluent IDs). The resolver representation is {@link ChimpResolver}
 * (CHIMP-owned, delegates to Meta-CSP internally for this milestone).
 */
public interface FlawProvider {

    /**
     * Returns the current flaw, or {@code null} if this provider has no
     * open flaw. If multiple flaws exist, the provider applies its own
     * variable-ordering and returns the highest-priority one.
     */
    ChimpFlaw currentFlaw();

    /**
     * Returns all resolver candidates for the given flaw, or
     * {@code null}/{@code empty} if no resolver exists. The search engine
     * applies {@link #resolverOrdering()} to sort the result before
     * trying them.
     */
    ChimpResolver[] resolvers(ChimpFlaw flaw);

    /**
     * Notifies this provider that the given resolver was successfully
     * applied to the flaw. Currently a no-op in all implementations
     * but preserved for protocol completeness.
     */
    void markResolved(ChimpFlaw flaw, ChimpResolver resolver);

    /**
     * Returns a comparator for ordering resolvers, or {@code null} if
     * no value-ordering heuristic is active.
     */
    Comparator<ChimpResolver> resolverOrdering();
}
