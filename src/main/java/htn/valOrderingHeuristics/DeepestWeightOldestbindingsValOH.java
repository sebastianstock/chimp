package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class DeepestWeightOldestbindingsValOH extends CHIMPValOH {
    public DeepestWeightOldestbindingsValOH() {
        super(ResolverOrderingPolicies.DEEPEST_WEIGHT_OLDEST);
    }
}
