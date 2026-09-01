package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ResourceRequirementTest {

    @Test
    public void storesImmutableActionRequirement() {
        List<ResourceParameterRequirement> parameters = new ArrayList<>(List.of(
                new ResourceParameterRequirement(1, "ur5")));
        ResourceRequirement requirement = new ResourceRequirement("armCapacity", parameters, 2);

        parameters.clear();

        assertEquals("armCapacity", requirement.resourceName());
        assertEquals(2, requirement.usageLevel());
        assertFalse(requirement.fluentType().isPresent());
        assertEquals(List.of(new ResourceParameterRequirement(1, "ur5")),
                requirement.parameterRequirements());
        assertThrows(UnsupportedOperationException.class,
                () -> requirement.parameterRequirements().clear());
    }

    @Test
    public void rejectsInvalidRequirementValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResourceParameterRequirement(-1, "ur5"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResourceRequirement("armCapacity", List.of(), -1));
    }
}
