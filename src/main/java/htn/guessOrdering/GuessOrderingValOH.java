package htn.guessOrdering;

import chimp.kernel.ConstraintNetwork;

import java.util.Comparator;

public class GuessOrderingValOH implements Comparator<ConstraintNetwork> {

	@Override
	public int compare(ConstraintNetwork cn0, ConstraintNetwork cn1) {
		return cn1.getConstraints().length - cn0.getConstraints().length;
	}

}
