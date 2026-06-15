package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class UnifyWeightNewestbindingsValOH extends CHIMPValOH {
    public UnifyWeightNewestbindingsValOH() {
        super(ResolverOrderingPolicies.UNIFY_WEIGHT_NEWEST);
    }
}
