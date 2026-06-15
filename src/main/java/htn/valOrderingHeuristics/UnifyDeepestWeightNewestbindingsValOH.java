package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class UnifyDeepestWeightNewestbindingsValOH extends CHIMPValOH {
    public UnifyDeepestWeightNewestbindingsValOH() {
        super(ResolverOrderingPolicies.UNIFY_DEEPEST_WEIGHT_NEWEST);
    }
}
