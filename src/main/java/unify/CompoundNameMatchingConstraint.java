package unify;

import java.util.Vector;

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
		Vector<NameMatchingConstraint> constraints = 
				new Vector<NameMatchingConstraint>(finternals.length);
		if (this.type.equals(Type.MATCHES)) {
			for(int i = 0; i < finternals.length; i++) {
				NameMatchingConstraint con = new NameMatchingConstraint();
				if(((NameVariable) finternals[i]).getName().length() > 0 
						&& ((NameVariable) tinternals[i]).getName().length() > 0) {
					con.setFrom(finternals[i]);
					con.setTo(tinternals[i]);
					constraints.add(con);
				}
			}
			return constraints.toArray(new Constraint[constraints.size()]);
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
