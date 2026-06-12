package hybridDomainParsing;

import htn.PlanReportroryItem;
import chimp.model.ResourceRequirement;
import chimp.model.ResourceCapacity;
import chimp.model.StateVariableDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ClassicHybridDomain {

    List<PlanReportroryItem> getOperators();
    List<PlanReportroryItem> getMethods();
    List<StateVariableDefinition> getStateVariableDefinitions();
    String getName();
    List<ResourceCapacity> getResourceCapacities();
    List<ResourceRequirement> getFluentResourceUsages();

    /**
     * @return Maximum number of arguments of a fluent.
     */
    int getMaxArgs();

    /**
     * @return Maximum number of integer variables of a fluent
     */
    int getMaxIntArgs();

    /**
     * @return Minimum value of an integer variable
     */
    int getMinIntValue();

    /**
     * @return Maximum value of an integer variable
     */
    int getMaxIntValue();

    String[] getPredicateSymbols();

    Set<String> getOperatorNames();
}
