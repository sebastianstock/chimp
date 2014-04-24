package pfd0;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;

import unify.NameMatchingConstraint;

public class FluentConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 137380711080409334L;
	
	public static enum Type {MATCHES};
	
	private Type type;
	
	public FluentConstraint(Type type) {
		this.type = type;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof Fluent) || !(t instanceof Fluent)) return null;
		
		SimpleBooleanValueConstraint scon = new SimpleBooleanValueConstraint(SimpleBooleanValueConstraint.Type.EQUALS);
		scon.setFrom(f);
		scon.setTo(t);
		
		NameMatchingConstraint ncon = new NameMatchingConstraint();
		ncon.setFrom(f);
		ncon.setTo(t);
		
		return new Constraint[]{scon, ncon};
	}

	@Override
	public Object clone() {
		return new FluentConstraint(this.type);
	}

	@Override
	public String getEdgeLabel() {
		return this.type.toString();
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
