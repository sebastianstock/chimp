package unify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private static Pattern namepattern = Pattern.compile("^(.+)\\((.*)\\)$");
	
	private int argsize;
	
	public CompoundNameVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers,
			Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		this.argsize = 0;
	}
	
	public void setName(String head, String... arguments) {
		Variable[] vars = this.getInternalVariables();
		((NameVariable) vars[0]).setName(head);
		int argcounter = 1;
		if (arguments != null) {
			if (arguments.length > vars.length - 1)
				throw new IllegalArgumentException(
						"Too many arguments. Only " + (this.getInternalVariables().length - 1) + " permitted.");
			for (String s : arguments) {
				((NameVariable) vars[argcounter]).setName(s);
				argcounter++;
			}
			this.argsize = arguments.length;
		} else {
			argcounter = 1; // all argument variables will be set to null
		}
		for (int j = argcounter; j < vars.length; j++) {
			((NameVariable) vars[j]).setName(null);
		}
	}
	
	/**
	 * Set the name with one String.
	 * Name should have the format HEAD(ARG1 ARG2 ARG3 ...).
	 * Example: 'On(mug1 table1)'
	 * @param name Full name of the variable.
	 */
	public void setFullName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name must not be null");
		}

		Matcher m = namepattern.matcher(name);
		if(m.find()) {
			String head = m.group(1);
			String[] args = null;
			if(m.group(2).length() > 0) {
				args = m.group(2).split("\\s+");
			}
			this.setName(head, args);
		} else {
			throw new IllegalArgumentException(
					"Name does not match pattern of a predicate, e.g., 'On(mug1 table1)'.");
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
	
	/**
	 * @return Full name with head and arguments, e.g., 'On(mug1 table1)'.
	 */
	public String getName() {
		Variable[] internalvars = this.getInternalVariables();
		StringBuilder ret = new StringBuilder(internalvars[0].toString());
		ret.append("(");
		if (internalvars.length > 1 && internalvars[1].toString().length() > 0) {
			ret.append(internalvars[1].toString());
		}
		for (int i = 2; i < internalvars.length; i++) {
			if(internalvars[i].toString().length() > 0) {
				ret.append(" ");
				ret.append(internalvars[i].toString());
			} else {
				break;
			}
			
		}
		ret.append(")");
		return ret.toString();
	}
	
	/**
	 * @return Only the name of the head.
	 */
	public String getHeadName() {
		return ((NameVariable) getInternalVariables()[0]).getName();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * Checks if it can be possibly be matched to another fluent.
	 * This is the case when the type and all constant parameters are the same.
	 * @param fluenttype
	 * @param arguments
	 * @return
	 */
	public boolean possibleMatch(String fluenttype, String[] arguments) {
		if (fluenttype == null || (! fluenttype.equals(getHeadName()))) {
			return false;
		}
		if (arguments == null) {
			if (getArgumentsSize() == 0) {
				return true;
			} else {
				return false;
			}
		}
		
		if (arguments.length != getArgumentsSize()) {
			return false;
		}
		
		for (int i = 0; i < arguments.length; i++) {
			String param = ((NameVariable) getInternalVariables()[i+1]).getName();
			if (! arguments[i].equals(param)) {
				if ((arguments[i].charAt(0) != '?') && (param.charAt(0) != '?')) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int getArgumentsSize() {
		return this.argsize;
	}

}
