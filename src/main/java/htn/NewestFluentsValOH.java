package htn;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentConstraint.Type;

public class NewestFluentsValOH extends ValueOrderingH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		long cn0ESTSum = calcPreconditionESTSum(cn0);
		long cn1ESTSum = calcPreconditionESTSum(cn1);
		
		if (cn0ESTSum > cn1ESTSum) {
			return -1;
		} else if (cn0ESTSum < cn1ESTSum) {
			return 1;
		} else {
			return 0;
		}
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
