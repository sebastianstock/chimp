package hybridDomainParsing;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;

public interface HybridDomainPlanner {
	
	FluentNetworkSolver getFluentNetworkSolver();
	
	Map<String, String[]> getTypesInstancesMap();
	

}
