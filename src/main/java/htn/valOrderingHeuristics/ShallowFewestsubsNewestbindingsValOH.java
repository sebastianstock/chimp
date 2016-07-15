package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

/**
 * 1. Less deep tasks
 * 2. Fewest Subtasks
 * 3. Newest Bindings
 * @author Sebastian Stock
 *
 */
public class ShallowFewestsubsNewestbindingsValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int checkDepth = getTaskDepth(cn0) - getTaskDepth(cn1); // prefer less deep task
		if (checkDepth != 0) {
			return checkDepth;
		}
		
		int subtasksChecking = countSubtasks(cn0) - countSubtasks(cn1);
		if (subtasksChecking != 0) {
			return subtasksChecking;
		}
		
		return comparePreconditions(cn0, cn1);
	}

}
