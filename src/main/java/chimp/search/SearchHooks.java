package chimp.search;

import org.metacsp.framework.ConstraintNetwork;

/**
 * Callbacks invoked by {@link ChimpSearchEngine} during resolver
 * application and retraction. This is the CHIMP-owned replacement for
 * the {@code MetaConstraintSolver} abstract hooks ({@code addResolverSub},
 * {@code retractResolverSub}, {@code preBacktrack},
 * {@code postBacktrack}).
 *
 * <p>The flaw representation is {@link ChimpFlaw} (CHIMP-owned). The
 * resolver representation remains Meta-CSP {@link ConstraintNetwork}
 * for this milestone.
 *
 * <p>The implementor (ultimately {@code HTNPlanner}) performs
 * prototype-to-fluent materialization in {@link #addResolverSub} and
 * marking/variable cleanup in {@link #retractResolverSub}.
 */
public interface SearchHooks {

    /**
     * Called before branching over a flaw. Currently a no-op in CHIMP
     * but preserved for protocol completeness.
     */
    void preBacktrack();

    /**
     * Called after backtracking over a flaw. Currently a no-op in CHIMP
     * but preserved for protocol completeness.
     *
     * @param flaw the flaw that was branched upon
     */
    void postBacktrack(ChimpFlaw flaw);

    /**
     * Called before a resolver is applied to the ground solvers. The
     * implementor materializes variable prototypes into real variables
     * and repairs constraint scopes.
     *
     * @return {@code true} if the resolver is consistent with the ground
     *         state; {@code false} to reject it (the ground state is left
     *         unchanged)
     */
    boolean addResolverSub(ChimpFlaw flaw, ConstraintNetwork resolver);

    /**
     * Called after a resolver is retracted from the ground solvers. The
     * implementor restores markings and removes materialized variables.
     */
    void retractResolverSub(ChimpFlaw flaw, ConstraintNetwork resolver);
}
