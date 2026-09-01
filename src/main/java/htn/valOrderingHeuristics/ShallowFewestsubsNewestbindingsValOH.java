package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class ShallowFewestsubsNewestbindingsValOH extends CHIMPValOH {
    public ShallowFewestsubsNewestbindingsValOH() {
        super(ResolverOrderingPolicies.SHALLOW_FEWEST_SUBTASKS_NEWEST);
    }
}
