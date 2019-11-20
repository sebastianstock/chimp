package hybridDomainParsing;

import htn.PlanReportroryItem;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;

import java.util.ArrayList;
import java.util.List;

public interface ClassicHybridDomain {

    public List<PlanReportroryItem> getOperators();
    public List<PlanReportroryItem> getMethods();
    public List<FluentScheduler> getFluentSchedulers();
    public String getName();
    public List<FluentResourceUsageScheduler> getResourceSchedulers();
    public List<ResourceUsageTemplate> getFluentResourceUsages();

    public int getMaxArgs(); // Maximum number of arguments of a fluent.
    public List<String> getPredicateSymbols = new ArrayList<>();
}
