package hybridDomainParsing;

import htn.PlanReportroryItem;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;

import java.util.List;

public interface ClassicHybridDomain {

    List<PlanReportroryItem> getOperators();
    List<PlanReportroryItem> getMethods();
    List<FluentScheduler> getFluentSchedulers();
    String getName();
    List<FluentResourceUsageScheduler> getResourceSchedulers();
    List<ResourceUsageTemplate> getFluentResourceUsages();

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
}
