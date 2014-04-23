package unify;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;

public class NameMatchingConstraint extends BinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -223776924287340596L;
	
	public NameMatchingConstraint() {
		
	}

	@Override
	public Object clone() {
		return new NameMatchingConstraint();
	}

	@Override
	public String getEdgeLabel() {
		return " -- NM -->";
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof NameMatchingConstraint)) return false;
		return true;
	}
	
	public String toString() {
		return "[" + this.getFrom() + " -- NM --> " + this.getTo();
	}
	
	public boolean getResult() {
		NameDomain domFrom = ((NameDomain) ((NameVariable) this.getFrom()).getDomain());
		NameDomain domTo = ((NameDomain) ((NameVariable) this.getTo()).getDomain());
		return domFrom.getName().equals(domTo.getName());
	}

}
