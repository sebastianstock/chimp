package hybridDomainParsing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import chimp.model.ResourceCapacity;

public class ResourceCapacityLegacyParsingTest {

    @Test
    public void parsesResourceCapacityWithoutCreatingScheduler() {
        List<ResourceCapacity> parsed = HybridDomain.processResources(
                new String[] {"armCapacity 2"});

        assertEquals(List.of(new ResourceCapacity("armCapacity", 2)), parsed);
    }
}
