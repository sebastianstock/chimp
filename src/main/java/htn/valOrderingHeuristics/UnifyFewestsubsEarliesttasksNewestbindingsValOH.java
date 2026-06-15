package htn.valOrderingHeuristics;

import chimp.search.ResolverOrderingPolicies;

public class UnifyFewestsubsEarliesttasksNewestbindingsValOH extends CHIMPValOH {
    public UnifyFewestsubsEarliesttasksNewestbindingsValOH() {
        super(ResolverOrderingPolicies.UNIFY_FEWEST_SUBTASKS_EARLIEST_NEWEST);
    }
}
