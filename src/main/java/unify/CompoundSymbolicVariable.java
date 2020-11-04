package unify;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;

public class CompoundSymbolicVariable extends MultiVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8181652775095849182L;

	private static Pattern namepattern = Pattern.compile("^(.+)\\((.*)\\)$");
	
	private static int internalVarsCount;
	
	public static final String NONESYMBOL = "n";
	
	private static final String[] noStrs = {};
	
	
	public CompoundSymbolicVariable(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers,
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
		if (arguments == null) {
			arguments = new String[0];
		}
		Variable[] vars = this.getInternalVariables();
		if ((arguments.length + 1) > internalVarsCount) {
			throw new IllegalArgumentException("Number of arguments is bigger than number of internal variables:" + type);
		}
		((NameVariable) vars[0]).setConstant(type);
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].charAt(0) != '?') {  // '?' indicates variables -> nothing to set
				((NameVariable) vars[i+1]).setConstant(arguments[i]);
			}
		}
		for (int i = arguments.length + 1; i < internalVarsCount; i++) {
			((NameVariable) vars[i]).setConstant(NONESYMBOL);
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
			String varStr = internalvars[i].toString();
			if(varStr.length() > 0 && ! varStr.equals(NONESYMBOL) ) {
				ret.append(" ");
				ret.append(varStr);
			} else {
				break;
			}
			
		}
		ret.append(")");
		return ret.toString();
	}
	
	public String getShortName() {
		Variable[] internalvars = this.getInternalVariables();
		StringBuilder ret = new StringBuilder(internalvars[0].toString());
		ret.append("(");
		if (internalvars.length > 1 && ((NameVariable) internalvars[1]).toStringShort().length() > 0) {
			ret.append(((NameVariable) internalvars[1]).toStringShort());
		}
		for (int i = 2; i < internalvars.length; i++) {
			String varStr = ((NameVariable) internalvars[i]).toStringShort();
			if(varStr.length() > 0 && ! varStr.equals(NONESYMBOL) ) {
				ret.append(" ");
				ret.append(varStr);
			} else {
				break;
			}
			
		}
		ret.append(")");
		return ret.toString();
	}
	
	/**
	 * Computes an array of ground arguments that are not NONESYMBOL.
	 * @return Array of ground arguments.
	 * @throws IllegalStateException if an argument is not ground.
	 */
	public String[] getGroundArgs() {
		Variable[] internalVars = this.getInternalVariables();
		if (internalVars.length < 2) {
			return noStrs;
		}
		
		List<String> ret = new ArrayList<String>(internalVars.length - 1);
		for (int i = 1; i < internalVars.length; i++) {
			NameVariable nv = (NameVariable) internalVars[i];
			if (nv.isGround()) {
				String str = nv.toString();
				if (!str.equals(NONESYMBOL)) {
					ret.add(str);
				} else {
					break;
				}
			} else {
				throw new IllegalStateException(i - 1  + "th Argument is not ground!");
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * Computes an array of ground arguments that are not NONESYMBOL.
	 * @return Array of ground arguments.
	 */
	public String[] getArgs() {
		Variable[] internalVars = this.getInternalVariables();
		if (internalVars.length < 2) {
			return noStrs;
		}

		List<String> ret = new ArrayList<String>(internalVars.length - 1);
		for (int i = 1; i < internalVars.length; i++) {
			NameVariable nv = (NameVariable) internalVars[i];
			String str = nv.toString();
			if (!str.equals(NONESYMBOL)) {
				ret.add(str);
			} else {
				break;
			}
		}
		return ret.toArray(new String[ret.size()]);
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
	 * @param fluenttype The type of the fluent.
	 * @param arguments The fluent's arguments.
	 * @return True if the fluent can be matched to the other fluent, otherwise false.
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
        return found;
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
	
	public boolean possibleArgumentsMatch(CompoundSymbolicVariable other, int[] connections) {
		if (connections.length > 0) {
			for (int i = 0; i < connections.length; i+=2) {
				if ( ! possibleArgumentMatch(other, connections[i], connections[i+1])) {
					return false;
				}
			}
		}	
		return true;
	}
	
	public boolean possibleArgumentMatch(CompoundSymbolicVariable other, int from, int to) {
		for (String symbol : getSymbolsAt(from+1)) {
			for (String otherSymbol : other.getSymbolsAt(to+1)) {
				if (symbol.equals(otherSymbol))
					return true;
			}
		}
		return false;
	}

	public boolean possibleArgumentDifferent(CompoundSymbolicVariable toVar,
			int fromId, int toId) {
		// at the moment we only check if both variables are ground
		String[] fromSymbols = this.getSymbolsAt(fromId+1);
		String[] toSymbols = toVar.getSymbolsAt(toId+1);
		if (fromSymbols.length != 1 || toSymbols.length != 1)
			return true;
        return fromSymbols[0] != toSymbols[0];
	}
	

}
