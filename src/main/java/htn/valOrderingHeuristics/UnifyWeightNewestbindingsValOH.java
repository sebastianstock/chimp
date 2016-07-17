package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

import htn.PlanReportroryItem;

/**
 * 1. Deepest Tasks
 * 2. Newest Bindings
 * @author Sebastian Stock
 *
 */
public class UnifyWeightNewestbindingsValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int matchesChecking = checkMatches(cn0, cn1);
		if (matchesChecking != 0) {
			return matchesChecking;
		}
		
//		int checkDepth = getTaskDepth(cn1) - getTaskDepth(cn0); // prefer deeper task
//		if (checkDepth != 0) {
//			return checkDepth;
//		}
		
		PlanReportroryItem pi0 = getUnaryApplied(cn0).getPlannedWith();
		PlanReportroryItem pi1 = getUnaryApplied(cn1).getPlannedWith();
		if (pi0 != null && pi1 != null) {
			int w0 = pi0.getPreferenceWeight();
			int w1 = pi1.getPreferenceWeight();
//			if (w0 != 0 || w1 != 0) 
//				System.out.println("Diff " + "PI0: " + pi0 + " PI1: " + pi1);
			int weightComp = w1 - w0;
			if (weightComp != 0) {
				return weightComp;
			}
		}
		
		return comparePreconditions(cn0, cn1);
	}

}
