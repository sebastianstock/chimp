package planner;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomain;

public interface CHIMPProblem {

	String[] getArgumentSymbols();

	Map<String, String[]> getTypesInstancesMap();
	
	void createState(FluentNetworkSolver fluentSolver, HybridDomain domain);
	
}
