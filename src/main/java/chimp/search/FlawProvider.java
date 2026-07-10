package chimp.search;

import java.util.Comparator;

import org.metacsp.framework.ConstraintNetwork;

/**
 * Provides flaws (open sub-problems) and resolver candidates for the
 * {@link ChimpSearchEngine} backtracking search. Each meta-constraint in
 * the planner implements this port. This is the CHIMP-owned replacement
 * for the {@code MetaConstraint} search protocol ({@code getMetaVariable},
 * {@code getMetaValues}, {@code markResolvedSub}, {@code valOH}).
 *
 * <p>For this milestone the flaw and resolver representations remain
 * Meta-CSP {@link ConstraintNetwork}s. The port decouples the search
 * <em>engine</em> from the {@code MetaConstraintSolver} inheritance
 * without yet changing the storage layer.
 */
public interface FlawProvider {

    /**
     * Returns the current flaw as a {@link ConstraintNetwork}, or
     * {@code null} if this provider has no open flaw. If multiple flaws
     * exist, the provider applies its own variable-ordering and returns
     * the highest-priority one.
     */
    ConstraintNetwork currentFlaw();

    /**
     * Returns all resolver candidates for the given flaw, or
     * {@code null}/{@code empty} if no resolver exists. The search engine
     * applies {@link #resolverOrdering()} to sort the result before
     * trying them.
     */
    ConstraintNetwork[] resolvers(ConstraintNetwork flaw);

    /**
     * Notifies this provider that {@code resolver} was successfully
     * applied to {@code flaw}. Currently a no-op in all implementations
     * but preserved for protocol completeness.
     */
    void markResolved(ConstraintNetwork flaw, ConstraintNetwork resolver);

    /**
     * Returns a comparator for ordering resolvers, or {@code null} if
     * no value-ordering heuristic is active.
     */
    Comparator<ConstraintNetwork> resolverOrdering();
}
