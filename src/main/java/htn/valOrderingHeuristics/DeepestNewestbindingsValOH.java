package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

/**
 * 1. Deepest Tasks
 * 2. Newest Bindings
 * @author Sebastian Stock
 *
 */
public class DeepestNewestbindingsValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int checkDepth = getTaskDepth(cn1) - getTaskDepth(cn0); // prefer deeper task
		if (checkDepth != 0) {
			return checkDepth;
		}
		
		return comparePreconditions(cn0, cn1);
	}

}
