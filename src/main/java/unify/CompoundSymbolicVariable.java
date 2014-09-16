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
	private static final long serialVersionUID = 8181652775095849182L;

	private static Pattern namepattern = Pattern.compile("^(.+)\\((.*)\\)$");
	
	private static int internalVarsCount;
	
	
	public CompoundNameVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers,
			Variable[] internalVars) {
		super(cs, id, internalSolvers, internalVars);
		internalVarsCount = internalVars.length;
		
	}
	
//	/**
//	 * Sets the Domain of the internal variable at position.
//	 * @param position Index of internal variable to set.
//	 * @param symbols Domain to which the internal variable will be set.
//	 */
//	public void setDomainAtPosition(int position, String... symbols) {
//		((NameVariable)this.getInternalVariables()[position]).setDomain(symbols);
//	}
	
//	/**
//	 * Sets domain of all internal variables.
//	 * @param symbols Array of symbols for the domain of each internal variable.
//	 */
//	public void setDomain(String[][] symbols) {
//		for(int i = 0; i < symbols.length; i++) {
//			this.setDomainAtPosition(i, symbols[i]);
//		}
//	}
	
	
	/**
	 * Sets the ground name of the variable.
	 * @param type the predicate's name.
	 * @param arguments Each internal variable is set to the value of the corresponding string in the array. These should be ground.
	 * @throws IllegalArgumentException If size of argument array does not match number of internal variables.
	 */
	public void setName(String type, String ...arguments) {
		Variable[] vars = this.getInternalVariables();
		if ((arguments.length + 1) != internalVarsCount) {
			throw new IllegalArgumentException("Number of arguments does not match number of internal variables");
		}
		((NameVariable) vars[0]).setConstant(type);
		for (int i = 1; i < internalVarsCount; i++) {
			if (arguments[i-1].charAt(0) != '?') {  // '?' indicates variables -> nothing to set
				((NameVariable) vars[i]).setConstant(arguments[i-1]);
			}
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
	
	// TODO: UPDATE
//	/**
//	 * @return Only the name of the head.
//	 */
//	public String getHeadName() {  
//		return ((NameVariable) getInternalVariables()[0]).getName();
//	}

	@Override
	public String toString() {
		return getName();
	}

	public String[] getPossiblePredicateNames() {
		return getSymbolsAt(0);
	}
	
	public String getPredicateName() {
		String[] possibleValues = getSymbolsAt(0);
		if (possibleValues.length == 1) {
			return possibleValues[0];
		} else {
			throw new IllegalStateException("Predicate names should be ground!");
		}
	}

	/**
	 * Checks if it can be possibly be matched to another fluent.
	 * This is the case when the type and all constant parameters are the same.
	 * @param fluenttype
	 * @param arguments
	 * @return
	 */
	public boolean possibleMatch(String fluenttype, String[] arguments) {
		if (fluenttype == null) {
			return false;
		}
		boolean found = false;
		for (String symbol : getSymbolsAt(0)) {
			if (fluenttype.equals(symbol)) {
				found = true;
				break;
			}
		}
		if (!found) {
			return false;
		}
		// TODO use full checking of arguments to reduce branching factor

		return true;
	}
	
	public String[] getSymbolsAt(int position) {
		if (position >= internalVarsCount) {
			throw new IllegalArgumentException();
		}
		return ((NameVariable) getInternalVariables()[position]).getPossibleSymbols();
	}
	
	public String getGroundSymbolAt(int position) {
		String[] possibleValues = getSymbolsAt(position);
		if (possibleValues.length == 1) {
			return possibleValues[0];
		} else {
			return "";
		}
	}
	

}
