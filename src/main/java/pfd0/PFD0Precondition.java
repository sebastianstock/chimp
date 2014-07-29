package pfd0;

public class PFD0Precondition {
	
	private String fluenttype;
	
	private String[] arguments;
	
	private int[] connections;

	public PFD0Precondition(String fluenttype, String[] arguments, int[] connections) {
		this.fluenttype = fluenttype;
		this.arguments = arguments;
		this.connections = connections;
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
	
	
	
}
