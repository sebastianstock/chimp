package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class DeepestNewestbindingsValOH extends CHIMPValOH {
    public DeepestNewestbindingsValOH() {
        super(ResolverOrderingPolicies.DEEPEST_NEWEST);
    }
}
