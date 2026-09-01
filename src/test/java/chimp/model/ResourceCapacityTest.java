package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ResourceCapacityTest {

    @Test
    public void storesResourceCapacity() {
        ResourceCapacity capacity = new ResourceCapacity("armCapacity", 2);

        assertEquals("armCapacity", capacity.resourceName());
        assertEquals(2, capacity.capacity());
    }

    @Test
    public void rejectsInvalidCapacityDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceCapacity(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new ResourceCapacity("armCapacity", -1));
    }
}
