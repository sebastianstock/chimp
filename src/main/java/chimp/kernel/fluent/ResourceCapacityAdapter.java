package chimp.kernel.fluent;

import chimp.model.ResourceCapacity;
import resourceFluent.FluentResourceUsageScheduler;

/**
 * Converts a {@link ResourceCapacity} model object into the corresponding
 * {@link FluentResourceUsageScheduler}.
 */
public final class ResourceCapacityAdapter {

    public FluentResourceUsageScheduler toResourceScheduler(ResourceCapacity resourceCapacity) {
        return new FluentResourceUsageScheduler(
                null,
                null,
                resourceCapacity.resourceName(),
                resourceCapacity.capacity());
    }
}
