package pfd0Activity;

import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.allenInterval.AllenInterval;

public class Fluent extends MultiVariable {

	private static final long serialVersionUID = -3155335762297801220L;
	
	public Fluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
	
	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link Activity}.
	 */
	public BooleanVariable getBooleanVariable() {
		return (BooleanVariable)this.getInternalVariables()[0];
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		String ret="";
		ret += this.getComponent()+"("+this.getID()+")" + "::<" + this.getInternalVariables()[0].toString() + ">";
		if (this.getMarking() != null) ret += "/" + this.getMarking();
		return ret;
	}

}
