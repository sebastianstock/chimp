package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

/**
 * 1. Deepest Tasks
 * 2. Fewest Subtasks
 * 3. Newest Bindings
 * @author Sebastian Stock
 *
 */
public class DeepestFewestsubsNewestbindingsValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int checkDepth = getTaskDepth(cn1) - getTaskDepth(cn0); // prefer deeper task
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
