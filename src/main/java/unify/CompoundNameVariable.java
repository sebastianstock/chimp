package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class CompoundNameVariable extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1194147485131064251L;
	
	public CompoundNameVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers,
			Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
	
	public void setName(String head, String... arguments) {
		Variable[] vars = this.getInternalVariables();
		((NameVariable) vars[0]).setName(head);
		if (arguments.length > vars.length - 1)
			throw new IllegalArgumentException(
					"Too many arguments. Only " + (this.getInternalVariables().length - 1) + " permitted.");
		int i = 1;
		for (String s : arguments) {
			((NameVariable) vars[i]).setName(s);
			i++;
		}
		for (int j = i; j < vars.length; j++) {
			((NameVariable) vars[j]).setName(null);
		}
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
		Variable[] internalvars = this.getInternalVariables();
		StringBuilder ret = new StringBuilder(internalvars[0].toString());
		ret.append("(");
		for (int i = 1; i < internalvars.length; i++) {
			ret.append(internalvars[i].toString());
			ret.append(" ");
		}
		ret.append(")");
		return ret.toString();
	}

}
