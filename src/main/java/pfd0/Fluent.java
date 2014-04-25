package pfd0;

import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

import unify.NameVariable;

public class Fluent extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3155335762297801220L;
	
	public Fluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
	
	/**
	 * @return The {@link BooleanVariable} representing the temporal value of this {@link SimpleBooleanValueVariable}.
	 */
	public NameVariable getNameVariable() {
		return (NameVariable)this.getInternalVariables()[0];
	}
	
	/**
	 * @return The {@link NameVariable} representing the symbolic value of this {@link SimpleBooleanValueVariable}.
	 */
	public SimpleBooleanValueVariable getSimpleBooleanValueVariable() {
		return (SimpleBooleanValueVariable)this.getInternalVariables()[1];
	}
	
	public void setName(String name) {
		this.getNameVariable().setName(name);
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
		ret += this.getComponent()+"("+this.getID()+")" + "::<" + this.getInternalVariables()[0].toString() + ">U<" + this.getInternalVariables()[1].toString() + ">";
		if (this.getMarking() != null) ret += "/" + this.getMarking();
		return ret;
	}

}
