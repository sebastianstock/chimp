package planner;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomain;

public interface CHIMPProblem {

	public String[] getArgumentSymbols();

	public Map<String, String[]> getTypesInstancesMap();
	
	public void createState(FluentNetworkSolver fluentSolver, HybridDomain domain);
	
}
