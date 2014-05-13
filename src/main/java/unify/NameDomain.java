package unify;

import org.metacsp.framework.Domain;

public class NameDomain extends Domain {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2722021570836332910L;
	
	private String domain;
	
	public NameDomain(NameVariable v) {
		super(v);
		this.domain = "";
	}
	
	public NameDomain(NameVariable v, String value) {
		super(v);
		this.domain = value;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "(" + domain + ")";
	}
	
	public void setName(String name) {
		this.domain = name;
	}
	
	public String getName() {
		if (domain != null) {
			return domain;
		} else {
			return "";
		}
	}
	
	public boolean isGround() {
		return domain.indexOf('?') < 0;
	}

}
