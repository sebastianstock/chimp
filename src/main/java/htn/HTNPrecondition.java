package htn;

import java.util.Vector;


public class HTNPrecondition {
	
	private static String component = "PRE";
	
	private String fluenttype;
	
	private String[] arguments;

	private IntArg[] integerArguments;
	
	private int[] connections;
	
	private boolean isNegativeEffect;
	
	private String key;
	
	private final Vector<AdditionalConstraintTemplate> additionalConstraints = 
			new Vector<AdditionalConstraintTemplate>();
	
	public HTNPrecondition(String fluenttype, String[] arguments, int[] connections, IntArg[] intArgs,
			int maxargs, String emptyStr, String key) {
		this.fluenttype = fluenttype;
		this.arguments = new String[maxargs];
		for (int i = 0; i < arguments.length; i++) {
			this.arguments[i] = arguments[i];
		}
		for (int i = arguments.length; i < maxargs; i++) {
			this.arguments[i] = emptyStr;
		}
		this.connections = connections;
		this.integerArguments = intArgs;
		this.key = key;
	}

	public HTNPrecondition(String fluenttype, String[] arguments, int[] connections,
						   int maxargs, String emptyStr, String key) {
		this(fluenttype, arguments, connections, new IntArg[0], maxargs, emptyStr, key);
	}
	
	public void setNegativeEffect(boolean isNegativeEffect) {
		this.isNegativeEffect = isNegativeEffect;
	}
	
	public boolean isNegativeEffect() {
		return isNegativeEffect;
	}

	public String getFluenttype() {
		return fluenttype;
	}

	public String[] getArguments() {
		return arguments;
	}
	
	public int[] getConnections() {
		return connections;
	}
	
	public String getKey() {
		return key;
	}
	
	public void addAdditionalConstraint(AdditionalConstraintTemplate con) {
		this.additionalConstraints.add(con);
	}
	
	public Vector<AdditionalConstraintTemplate> getAdditionalConstraints() {
		return additionalConstraints;
	}

	public IntArg[] getIntegerArguments() {
		return integerArguments;
	}
}
