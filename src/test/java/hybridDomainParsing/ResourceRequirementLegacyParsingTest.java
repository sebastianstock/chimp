package hybridDomainParsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;

public class ResourceRequirementLegacyParsingTest {

    @Test
    public void parsesActionRequirementIntoNeutralValue() {
        ResourceRequirement requirement = HybridDomain.parseResourceUsage(
                "(Usage armCapacity 2) (Param 1 ur5)", false);

        assertEquals("armCapacity", requirement.resourceName());
        assertEquals(2, requirement.usageLevel());
        assertFalse(requirement.fluentType().isPresent());
        assertEquals(new ResourceParameterRequirement(1, "ur5"),
                requirement.parameterRequirements().get(0));
    }

    @Test
    public void parsesFluentRequirementIntoNeutralValue() {
        ResourceRequirement requirement = HybridDomain.parseResourceUsage(
                "(Usage armCapacity 1) (Fluent Holding) (Param 1 ur5)", true);

        assertEquals("Holding", requirement.fluentType().orElseThrow());
        assertEquals(new ResourceParameterRequirement(1, "ur5"),
                requirement.parameterRequirements().get(0));
    }
}
