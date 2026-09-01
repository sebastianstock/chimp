package planner;

import java.util.Collection;
import java.util.Map;

import chimp.model.ProblemDefinition;

public interface CHIMPProblem {

	String[] getArgumentSymbols();

	void addArgumentSymbols(Collection<String> symbols);

	Map<String, String[]> getTypesInstancesMap();

	ProblemDefinition getProblemDefinition();
	
}
