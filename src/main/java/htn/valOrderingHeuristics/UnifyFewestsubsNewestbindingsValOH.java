package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class UnifyFewestsubsNewestbindingsValOH extends CHIMPValOH {
    public UnifyFewestsubsNewestbindingsValOH() {
        super(ResolverOrderingPolicies.UNIFY_FEWEST_SUBTASKS_NEWEST);
    }
}
