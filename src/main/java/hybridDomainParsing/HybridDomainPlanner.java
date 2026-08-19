package hybridDomainParsing;

import java.util.Map;

import chimp.kernel.fluent.FluentNetworkSolver;

public interface HybridDomainPlanner {

	FluentNetworkSolver getFluentNetworkSolver();
	
	Map<String, String[]> getTypesInstancesMap();
	

}
