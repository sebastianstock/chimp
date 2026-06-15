package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class UnifyEarlisttasksValOH extends CHIMPValOH {
    public UnifyEarlisttasksValOH() {
        super(ResolverOrderingPolicies.UNIFY_EARLIEST_TASKS);
    }
}
