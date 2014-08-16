package pfd0Symbolic;

import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

import simpleBooleanValueCons.SimpleBooleanValueVariable;
import symbolicUnifyTyped.TypedCompoundSymbolicVariable;

public class Fluent extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3155335762297801220L;
	
	public Fluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] 
			internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
	
	/**
	 * @return The {@link CompoundNameVariable} representing the temporal value 
	 * of this {@link SimpleBooleanValueVariable}.
	 */
	public TypedCompoundSymbolicVariable getCompoundNameVariable() {
		return (CompoundNameVariable)this.getInternalVariables()[0];
	}
	
	/**
	 * @return The {@link BooleanVariable} representing the symbolic value 
	 * of this {@link SimpleBooleanValueVariable}.
	 */
	public SimpleBooleanValueVariable getSimpleBooleanValueVariable() {
		return (SimpleBooleanValueVariable)this.getInternalVariables()[1];
	}
	
	public void setName(String name) {
		this.getCompoundNameVariable().setFullName(name);
	}
	
	public void setName(String type, String[] arguments) {
		this.getCompoundNameVariable().setName(type, arguments);
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
		StringBuilder ret = new StringBuilder();
		ret.append(this.getComponent());
		ret.append("(");
		ret.append(this.getID());
		ret.append(")::<");
		ret.append(this.getInternalVariables()[0].toString());
		ret.append(">U<");
		ret.append(this.getInternalVariables()[1].toString());
		ret.append(">");
		if (this.getMarking() != null) {
			ret.append("/");
			ret.append(this.getMarking());
		}
		return ret.toString();
	}

}
