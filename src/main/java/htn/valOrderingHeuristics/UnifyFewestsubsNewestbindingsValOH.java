package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

/**
 * Compare based on
 * 1. prefer unification
 * 2. prefer fewer subtasks
 * 3. prefer newest precondition bindings
 * 
 * @author Sebastian Stock
 */
public class UnifyFewestsubsNewestbindingsValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int matchesChecking = checkMatches(cn0, cn1);
		if (matchesChecking != 0) {
			return matchesChecking;
		}
		
		int subtasksChecking = countSubtasks(cn0) - countSubtasks(cn1);
		if (subtasksChecking != 0) {
			return subtasksChecking;
		}
		
		return comparePreconditions(cn0, cn1);

	}

}
