package htn;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentConstraint.Type;

/**
 * Compare based on
 * 1. prefer unification
 * 3. prefer fewer subtasks
 * 3. prefer planned task with earlier start time
 * 4. prefer newest precondition bindings
 * 
 * @author Sebastian Stock
 */
public class UnifyFewestsubsNewestbindingsValOH extends ValueOrderingH {

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

	
	private int countSubtasks(ConstraintNetwork cn) {
		int ret = 0;
		for (Constraint con : cn.getConstraints()) {
			if (con instanceof FluentConstraint && ((FluentConstraint) con).getType() == Type.DC) {
				ret++;
			}
		}
		return ret;
		
	}
	
	private int checkMatches(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		FluentConstraint m0 = searchMatches(cn0);
		FluentConstraint m1 = searchMatches(cn1);
		if (m0 == null) {
			if (m1 == null) {
				return 0; // both do not contain matches
			} else {
				return 1; // only cn1 contains matches
			}
		} else {
			if (m1 == null) {
				return -1; // only cn0 contains matches
			} else {
				return 0; // both contain matches TODO compare intervals
			}
		}
	}
	
	private FluentConstraint searchMatches(ConstraintNetwork cn) {
		for (Constraint con : cn.getConstraints()) {
			if (con instanceof FluentConstraint && ((FluentConstraint) con).getType() == Type.MATCHES) {
				return (FluentConstraint) con;
			}
		}
		return null;
	}
	
	private int comparePreconditions(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		long cn0ESTSum = calcPreconditionESTSum(cn0);
		long cn1ESTSum = calcPreconditionESTSum(cn1);

//		Fluent t0 = getPlannedTask(cn0);
//		Fluent t1 = getPlannedTask(cn1);
//		return t0.toString().hashCode() - t1.toString().hashCode();

		if (cn0ESTSum > cn1ESTSum) {
			return -1;
		} else if (cn0ESTSum < cn1ESTSum) {
			return 1;
		} else {
			return 0;
//			Fluent t0 = getPlannedTask(cn0);
//			Fluent t1 = getPlannedTask(cn1);
//			int ret =  cn0.toString().hashCode() - cn1.toString().hashCode();
//			if (ret != 0) {
//				return ret;
//			} else {
//				System.out.println("HASH IS ZERO");
//				return 2;
//			}
			
		}
	}
	
	private Fluent getPlannedTask(ConstraintNetwork cn) {
		for (Constraint con : cn.getConstraints()) {
			if (con instanceof FluentConstraint && ((FluentConstraint) con).getType() == Type.UNARYAPPLIED) {
				return (Fluent) ((FluentConstraint) con).getFrom();
			}
		}
		return null;
	}
	
	private long calcPreconditionESTSum(ConstraintNetwork cn) {
		long sum = 0;
		for(Constraint con : cn.getConstraints()) {

			if (con instanceof FluentConstraint) {
				FluentConstraint flc = (FluentConstraint) con;
				if (flc.getType() == Type.PRE) {
					sum += ((Fluent) flc.getFrom()).getAllenInterval().getEST();
				}

			}
		}
		return sum;
	}

}
