package chimp.adapter.metacsp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;
import resourceFluent.ResourceUsageTemplate;

public class MetaCspResourceRequirementAdapterTest {

    @Test
    public void convertsNeutralRequirementToLegacyTemplate() {
        ResourceRequirement requirement = new ResourceRequirement(
                "armCapacity",
                "Holding",
                List.of(
                        new ResourceParameterRequirement(1, "ur5"),
                        new ResourceParameterRequirement(2, "object1")),
                2);

        ResourceUsageTemplate converted = new MetaCspResourceRequirementAdapter()
                .toResourceUsageTemplate(requirement);

        assertEquals("armCapacity", converted.getResourceName());
        assertEquals("Holding", converted.getFluentType());
        assertEquals(2, converted.getResourceUsageLevel());
        assertArrayEquals(new int[] {1, 2}, converted.getResourceRequirementPositions());
        assertArrayEquals(new String[] {"ur5", "object1"}, converted.getResourceRequirements());
    }
}
