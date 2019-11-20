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

    int getMaxArgs(); // Maximum number of arguments of a fluent.
    String[] getPredicateSymbols();
}
