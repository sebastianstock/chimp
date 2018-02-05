package unify;

import java.util.Arrays;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;

public class NameMatchingConstraint extends BinaryConstraint {

	private static final long serialVersionUID = 7531778878497168468L;

	public enum Type {EQUALS, DIFFERENT, UNARYEQUALS, UNARYDIFFERENT}

    private Type type;
	private int[] unaryValues;
	
	public NameMatchingConstraint(Type type) {
		this.type = type;
	}
	
	public void setUnaryValues(int[] unaryValues) {
		this.unaryValues = unaryValues;
		Arrays.sort(this.unaryValues);
	}

	@Override
	public Object clone() {
		NameMatchingConstraint c =  new NameMatchingConstraint(this.type);
		c.setUnaryValues(this.unaryValues);
		return c;
	}

	@Override
	public String getEdgeLabel() {
		if (this.type.equals(Type.UNARYDIFFERENT) || this.type.equals(Type.UNARYEQUALS))
			return "" + this.type + " " + Arrays.toString(unaryValues);
		return "" + this.type;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof NameMatchingConstraint)) return false;
		if (this.getScope().length != c.getScope().length) return false;
		for (int i = 0; i < this.getScope().length; i++) {
			if (!this.getScope()[i].equals(c.getScope()[i])) return false;
		}
		if (type != ((NameMatchingConstraint) c).getType()) return false;
		if (type.equals(Type.UNARYEQUALS) || type.equals(Type.UNARYDIFFERENT)) {
			for (int s : unaryValues) {
				boolean found = false;
				for (int cs : ((NameMatchingConstraint) c).getUnaryValues()) {
					if (s == cs) {
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append(this.getFrom());
		sb.append(this.getEdgeLabel());
		sb.append(this.getTo());
		sb.append("]");
		return sb.toString();
	}
	
	public Type getType() {
		return type;
	}
	
	public int[] getUnaryValues() {
		return unaryValues;
	}

}
