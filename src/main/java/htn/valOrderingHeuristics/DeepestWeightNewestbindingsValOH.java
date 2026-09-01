package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class DeepestWeightNewestbindingsValOH extends CHIMPValOH {
    public DeepestWeightNewestbindingsValOH() {
        super(ResolverOrderingPolicies.DEEPEST_WEIGHT_NEWEST);
    }
}
