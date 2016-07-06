package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

/**
 * Compare based on
 * 1. prefer unification
 * 3. prefer fewer subtasks
 * 3. prefer planned task with earlier start time
 * 4. prefer newest precondition bindings
 * 
 * @author Sebastian Stock
 */
public class UnifyFewestsubsEarliesttasksNewestbindingsValOH extends CHIMPValOH {

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
		
		long est0 = getPlannedTask(cn0).getAllenInterval().getEST();
		long est1 = getPlannedTask(cn1).getAllenInterval().getEST();
		if (est0 == est1) {
			return comparePreconditions(cn0, cn1);
		} else if (est0 < est1) {
			return -1;
		} else {
			return 1;
		}	
	}

}
