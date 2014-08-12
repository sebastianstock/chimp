package symbolicUnifyTyped;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.symbols.SymbolicVariable;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.NameVariable;

public class TypedCompoundSymbolicVariable extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3187043494226482779L;

	private static Pattern namepattern = Pattern.compile("^(.+)\\((.*)\\)$");
	
	private static int internalVarsCount;
	
	
	public TypedCompoundSymbolicVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers,
			Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		internalVarsCount = internalVars.length;
		
	}
	
	/**
	 * Sets the Domain of the internal variable at position.
	 * @param position Index of internal variable to set.
	 * @param symbols Domain to which the internal variable will be set.
	 */
	public void setDomainAtPosition(int position, String... symbols) {
		((SymbolicVariable)this.getInternalVariables()[position]).setDomain(symbols);
	}
	
	/**
	 * Sets domain of all internal variables.
	 * @param symbols Array of symbols for the domain of each internal variable.
	 */
	public void setDomain(String[][] symbols) {
		for(int i = 0; i < symbols.length; i++) {
			this.setDomainAtPosition(i, symbols[i]);
		}
	}
	
	
	/**
	 * Sets the ground name of the variable.
	 * @param arguments Each internal variable is set to the value of the corresponding string in the array. These should be ground.
	 * @throws IllegalArgumentException If size of argument array does not match number of internal variables.
	 */
	public void setName(String[] arguments) {
		Variable[] vars = this.getInternalVariables();
		if (arguments.length != internalVarsCount) {
			throw new IllegalArgumentException("Number of arguments does not match number of internal variables");
		}
		for (int i = 0; i < internalVarsCount; i++) {
			((SymbolicVariable) vars[i]).setDomain(arguments[i]);
		}
	}
	
	
	// TODO: UPDATE
//	/**
//	 * Set the name with one String.
//	 * Name should have the format HEAD(ARG1 ARG2 ARG3 ...).
//	 * Example: 'On(mug1 table1)'
//	 * @param name Full name of the variable.
//	 */
//	public void setFullName(String name) { // TODO: UPDATE
//		if (name == null) {
//			throw new IllegalArgumentException("Name must not be null");
//		}
//
//		Matcher m = namepattern.matcher(name);
//		if(m.find()) {
//			String head = m.group(1);
//			String[] args = null;
//			if(m.group(2).length() > 0) {
//				args = m.group(2).split("\\s+");
//			}
//			this.setName(head, args);
//		} else {
//			throw new IllegalArgumentException(
//					"Name does not match pattern of a predicate, e.g., 'On(mug1 table1)'.");
//		}
//	}

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
	
	// TODO: UPDATE
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
	
	// TODO: UPDATE
//	/**
//	 * @return Only the name of the head.
//	 */
//	public String getHeadName() {  
//		return ((NameVariable) getInternalVariables()[0]).getName();
//	}

	// TODO: UPDATE
	@Override
	public String toString() {
		return getName();
	}
	
	
	// TODO: UPDATE
//	/**
//	 * Checks if it can be possibly be matched to another fluent.
//	 * This is the case when the type and all constant parameters are the same.
//	 * @param fluenttype
//	 * @param arguments
//	 * @return
//	 */
//	public boolean possibleMatch(String fluenttype, String[] arguments) {   // TODO: UPDATE
//		if (fluenttype == null || (! fluenttype.equals(getHeadName()))) {
//			return false;
//		}
//		if (arguments == null) {
//			if (getArgumentsSize() == 0) {
//				return true;
//			} else {
//				return false;
//			}
//		}
//		
//		if (arguments.length != getArgumentsSize()) {
//			return false;
//		}
//		
//		for (int i = 0; i < arguments.length; i++) {
//			String param = ((NameVariable) getInternalVariables()[i+1]).getName();
//			if (! arguments[i].equals(param)) {
//				if ((arguments[i].charAt(0) != '?') && (param.charAt(0) != '?')) {
//					return false;
//				}
//			}
//		}
//		
//		return true;
//	}
	

}
