package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class DeepestFewestsubsNewestbindingsValOH extends CHIMPValOH {
    public DeepestFewestsubsNewestbindingsValOH() {
        super(ResolverOrderingPolicies.DEEPEST_FEWEST_SUBTASKS_NEWEST);
    }
}
