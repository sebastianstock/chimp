package hybridDomainParsing;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;

public interface HybridDomainPlanner {
	
	public FluentNetworkSolver getFluentNetworkSolver();
	
	public Map<String, String[]> getTypesInstancesMap();
	

}
