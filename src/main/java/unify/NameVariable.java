package unify;

import java.util.Arrays;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;


public class NameVariable extends Variable {

	private static final long serialVersionUID = -7696577558183026849L;
	private NameDomain domain;
	
	protected NameVariable(ConstraintSolver cs, int id, int symbolcount) {
		super(cs, id);
		domain = new NameDomain(this, symbolcount);
	}

	@Override
	public int compareTo(Variable arg0) {
		return this.domain.compareTo(arg0);
	}

	@Override
	public Domain getDomain() {
		return this.domain;
	}

	@Override
	public void setDomain(Domain arg0) {
		if (arg0 != null) {
			this.domain = (NameDomain)arg0;
		} else {
			throw new IllegalArgumentException("Domain must be != null");
		}
	}
	
	/** Makes the variable ground and sets the domain to only that symbol.
	 * 
	 * @param symbol New ground value of the variable.
	 */
	public void setConstant(String symbol) {
		this.domain.setConstantValue(
				((NameMatchingConstraintSolver) this.getConstraintSolver()).getIndexOfSymbol(symbol));
	}

	public String[] getPossibleSymbols() {
		int[] indizes = domain.getPossibleIndizes();
		String[] ret = new String[indizes.length];
		String[] symbols = ((NameMatchingConstraintSolver) this.getConstraintSolver()).getSymbols();
		for (int i = 0; i < indizes.length; i++) {
			ret[i] = symbols[indizes[i]];
		}
		return ret;
	}

	@Override
	public String toString() {
//		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
		String[] possibleSymbols = getPossibleSymbols();
		if (possibleSymbols.length == 1) {
			return possibleSymbols[0];
		} else {
			return Arrays.toString(possibleSymbols);
		}
	}
	
	public String toStringShort() {
		String[] possibleSymbols = getPossibleSymbols();
		if (possibleSymbols.length == 1) {
			return possibleSymbols[0];
		} else {
			return "{...}";
		}
	}
	
	public boolean isGround() {
		return domain.isGround();
	}
	
	public boolean hasModel() {
		return domain.hasModel();
	}

}
