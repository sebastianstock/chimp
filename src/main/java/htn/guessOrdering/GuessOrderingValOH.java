package htn.guessOrdering;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;

public class GuessOrderingValOH extends ValueOrderingH {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		return cn1.getConstraints().length - cn0.getConstraints().length;
	}

}
