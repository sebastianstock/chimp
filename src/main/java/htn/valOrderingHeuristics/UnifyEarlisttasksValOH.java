package htn.valOrderingHeuristics;

import org.metacsp.framework.ConstraintNetwork;

public class UnifyEarlisttasksValOH extends CHIMPValOH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		int matchesChecking = checkMatches(cn0, cn1);
		if (matchesChecking != 0) {
			return matchesChecking;
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
