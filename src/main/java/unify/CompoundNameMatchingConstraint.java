package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;

public class CompoundNameMatchingConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6660479562784201973L;
	
	public static enum Type {MATCHES};
	
	private Type type;
	
	public CompoundNameMatchingConstraint(Type type) {
		this.type = type;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof CompoundNameVariable) || !(t instanceof CompoundNameVariable)) {
			return null;
		}
		
		Variable[] finternals = ((CompoundNameVariable) f).getInternalVariables();
		Variable[] tinternals = ((CompoundNameVariable) t).getInternalVariables();
//		Vector<NameMatchingConstraint> cons = new Vector<NameMatchingConstraint>();
		NameMatchingConstraint[] ret = new NameMatchingConstraint[finternals.length];
		if (this.type.equals(Type.MATCHES)) {
			for(int i = 0; i < finternals.length; i++) {
				NameMatchingConstraint con = new NameMatchingConstraint();
				if(finternals[i] != null && tinternals[i] != null) {
					con.setFrom(finternals[i]);
					con.setTo(tinternals[i]);
					ret[i] = con;
				}
			}
			return ret;
		}
		
		return null;
	}

	@Override
	public Object clone() {
		return new CompoundNameMatchingConstraint(this.type);
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
	
	public Type getType() {
		return type;
	}

}
