package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import chimp.model.ResourceCapacity;
import resourceFluent.FluentResourceUsageScheduler;

public class MetaCspResourceCapacityAdapterTest {

    @Test
    public void createsLegacySchedulerFromNeutralCapacity() {
        FluentResourceUsageScheduler scheduler = new MetaCspResourceCapacityAdapter()
                .toResourceScheduler(new ResourceCapacity("armCapacity", 2));

        assertEquals("FluentResourceUsageScheduler armCapacity : 2", scheduler.toString());
    }
}
