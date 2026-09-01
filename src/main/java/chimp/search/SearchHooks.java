package chimp.search;

/**
 * Callbacks invoked by {@link ChimpSearchEngine} during resolver
 * application and retraction.
 *
 * <p>The flaw representation is {@link ChimpFlaw} (CHIMP-owned). The
 * resolver representation is {@link ChimpResolver} (CHIMP-owned, delegates
 * to Meta-CSP internally for this milestone).
 *
 * <p>The implementor (ultimately {@code HTNPlanner}) performs
 * prototype-to-fluent materialization in {@link #addResolverSub} and
 * marking/variable cleanup in {@link #retractResolverSub}.
 */
public interface SearchHooks {

    /** Called before branching over a flaw. Currently a no-op. */
    void preBacktrack();

    /** Called after backtracking over a flaw. Currently a no-op. */
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
    boolean addResolverSub(ChimpFlaw flaw, ChimpResolver resolver);

    /**
     * Called after a resolver is retracted from the ground solvers. The
     * implementor restores markings and removes materialized variables.
     */
    void retractResolverSub(ChimpFlaw flaw, ChimpResolver resolver);
}
