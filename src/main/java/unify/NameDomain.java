package unify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.metacsp.framework.Domain;

public class NameDomain extends Domain {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2722021570836332910L;
	
	private boolean[] domain;
	// -1 if not a constant, if it is a constant it is the index of the domain value.
	private int isConstant = -1;
	
//	public NameDomain(NameVariable v) {
//		super(v);
//		this.domain = "";
//	}
	
	public NameDomain(NameVariable v, int symbolcount) {
		super(v);
		if (symbolcount < 1) {
			throw new IllegalArgumentException("Number of symbols has to be > 0");
		}
		this.domain = new boolean[symbolcount];
		Arrays.fill(this.domain, true);
	}
	
	public NameDomain(NameDomain dom) {
		super(dom.getVariable());
		this.domain = Arrays.copyOf(dom.domain, dom.domain.length);
		this.isConstant = dom.isConstant;
	}
	
	protected void reset() {
		if(isConstant > -1) {
			Arrays.fill(this.domain, false);
			this.domain[isConstant] = true;
		} else {
			Arrays.fill(this.domain, true);
		}
		
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int[] getPossibleIndizes() {
		if (isConstant > -1) {
			return new int[] {isConstant};
		} else {
			List<Integer> possibles = new ArrayList<Integer>();
			for (int i = 0; i < domain.length; i++) {
				if (domain[i]) {
					possibles.add(new Integer(i));
				}
			}
			int[] ret = new int[possibles.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = possibles.get(i).intValue();
	    }
			return ret;
		}
	}
	
	public boolean hasModel() {
		for (int i = 0; i < domain.length; i++) {
			if (domain[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 *
	 * @return true if it is ground, i.e., only one model exists, false if unground
	 */
	public boolean isGround() {
		if (isConstant > -1)
			return true;
		// unground if at least two models
		boolean foundModel = false;
		for (int i = 0; i < domain.length; i++) {
			if (domain[i]) {
				if (foundModel)
					return false;
				foundModel = true;
			}
		}
		return true;
	}

	public void setConstantValue(int index) {
		if (index >= this.domain.length || index < 0)
			throw new IllegalArgumentException();
		Arrays.fill(this.domain, false);
		this.domain[index] = true;
		this.isConstant = index;
	}

	@Override
	public String toString() {
		return Arrays.toString(getPossibleIndizes());
	}

	/**
	 * 
	 * @param unaryValues Sorted array of values.
	 * @return True if an internal variable has been changed.
	 */
	protected boolean setUnaryEquals(int[] unaryValues) {
		// sets each internal variable that is not in unaryValues to false
		boolean changed = false; // indicates if a value has been changed
		int valueIndex = 0;
		for (int domainIndex = 0; domainIndex < domain.length; domainIndex++) {
			if (domainIndex != unaryValues[valueIndex]) {
				if (domain[domainIndex]){
					domain[domainIndex] = false;
					changed = true;
				}
			} else {
				if(valueIndex < unaryValues.length -1)
					valueIndex++;
			}
		}
		return changed;
	}
	
	/**
	 * 
	 * @param unaryValues Values that cannot be true.
	 * @return True if an internal variable has been changed.
	 */
	protected boolean setUnaryDifferent(int[] unaryValues) {
		boolean changed = false; // indicates if a value has been changed
		for (int i : unaryValues) {
			if (domain[i]) {
				domain[i] = false;
				changed = true;
			}
		}
		return changed;
	}
	
	public int isConstant() {
		return isConstant;
	}
	
	protected int applyEqualsConstr(NameDomain to) {
		if (this.domain.length != to.domain.length) {
			throw new IllegalArgumentException("Domains need to have the same number of symbols");
		}
		
		boolean changedFrom = false;
		boolean changedTo = false;
		for (int i = 0; i < domain.length; i++) {
			if (this.domain[i]) {
				if (!to.domain[i]) {
					this.domain[i] = false;
					changedFrom = true;
				}
			} else{
				if (to.domain[i]) {
					to.domain[i] = false;
					changedTo = true;
				}
			}
		}
		
		int ret = 0;
		if (changedFrom) {
			ret += 1;
		}
		if (changedTo) {
			ret += 2;
		}
		return ret;
	}
	
	protected int applyDifferentConstr(NameDomain to) {
		if (this.domain.length != to.domain.length) {
			throw new IllegalArgumentException("Domains need to have the same number of symbols");
		}
	
		boolean toIsGround = to.isGround();
		if (this.isGround()) {
			int fromIndex = this.getPossibleIndizes()[0];
			if (toIsGround) {
				int toIndex = to.getPossibleIndizes()[0];
				if (fromIndex == toIndex) { // only need to be update if both are equal
					this.domain[toIndex] = false;
					to.domain[toIndex] = false;
					return 3;
				}
			} else {
				if (to.domain[fromIndex]) {
					to.domain[fromIndex] = false;
					return 2;
				}
			}
		} else {
			if (toIsGround) {
				int toIndex = to.getPossibleIndizes()[0];
				if (this.domain[toIndex]) {
					this.domain[toIndex] = false;
					return 1;
				}
			}
		}
		
		return 0;
	}
	

}
