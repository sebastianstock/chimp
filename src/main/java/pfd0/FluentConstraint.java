package pfd0;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;

import unify.NameMatchingConstraint;
import unify.NameVariable;

public class FluentConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 137380711080409334L;
	
	public static enum Type {MATCHES, DC, PRE};
	
	private Type type;
	
	public FluentConstraint(Type type) {
		this.type = type;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof Fluent) || !(t instanceof Fluent)) return null;
		
		if (this.type.equals(Type.MATCHES)) {
			SimpleBooleanValueConstraint scon = new SimpleBooleanValueConstraint(SimpleBooleanValueConstraint.Type.EQUALS);
			SimpleBooleanValueVariable bf = ((Fluent) f).getSimpleBooleanValueVariable();
			scon.setFrom(bf);
			SimpleBooleanValueVariable bt = ((Fluent) t).getSimpleBooleanValueVariable();
			scon.setTo(bt);

			NameMatchingConstraint ncon = new NameMatchingConstraint();
			NameVariable nf = ((Fluent) f).getNameVariable();
			ncon.setFrom(nf);
			NameVariable nt = ((Fluent) t).getNameVariable();
			ncon.setTo(nt);

			return new Constraint[]{scon, ncon};
		} else if (this.type.equals(Type.DC)) {
			// TODO nothing to add here?
		} else if (this.type.equals(Type.PRE)) {
			// TODO nothing to add here?
		}
		
		return null;
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
