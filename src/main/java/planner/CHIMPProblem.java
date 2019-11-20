package planner;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.HybridDomain;

public interface CHIMPProblem {

	String[] getArgumentSymbols();

	Map<String, String[]> getTypesInstancesMap();
	
	void createState(FluentNetworkSolver fluentSolver, ClassicHybridDomain domain);
	
}
