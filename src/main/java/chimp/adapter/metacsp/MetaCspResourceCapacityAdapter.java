package chimp.adapter.metacsp;

import chimp.model.ResourceCapacity;
import resourceFluent.FluentResourceUsageScheduler;

public final class MetaCspResourceCapacityAdapter {

    public FluentResourceUsageScheduler toResourceScheduler(ResourceCapacity resourceCapacity) {
        return new FluentResourceUsageScheduler(
                null,
                null,
                resourceCapacity.resourceName(),
                resourceCapacity.capacity());
    }
}
