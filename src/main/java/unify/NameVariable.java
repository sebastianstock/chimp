package unify;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class NameVariable extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7855257305948524604L;
	
	private NameDomain domain;
	
	protected NameVariable(ConstraintSolver cs, int id) {
		super(cs, id);
		domain = new NameDomain(this);
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
		this.domain = (NameDomain)arg0;

	}
	
	public void setName(String name) {
		if(this.domain != null)
			this.domain.setName(name);
	}
	
	public String getName() {
		return ((NameDomain)domain).getName();
	}

	@Override
	public String toString() {
//		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
		return getName();
	}

}
