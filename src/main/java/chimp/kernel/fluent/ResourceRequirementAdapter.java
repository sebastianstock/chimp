package chimp.kernel.fluent;

import java.util.stream.IntStream;

import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;
import resourceFluent.ResourceUsageTemplate;

/**
 * Converts a CHIMP {@link ResourceRequirement} into the
 * {@link ResourceUsageTemplate} used by resource schedulers.
 */
public final class ResourceRequirementAdapter {

    public ResourceUsageTemplate toResourceUsageTemplate(ResourceRequirement requirement) {
        int[] positions = requirement.parameterRequirements().stream()
                .mapToInt(ResourceParameterRequirement::position)
                .toArray();
        String[] values = requirement.parameterRequirements().stream()
                .map(ResourceParameterRequirement::value)
                .toArray(String[]::new);

        if (requirement.fluentType().isPresent()) {
            return new ResourceUsageTemplate(
                    requirement.resourceName(),
                    requirement.fluentType().orElseThrow(),
                    positions,
                    values,
                    requirement.usageLevel());
        }
        return new ResourceUsageTemplate(
                requirement.resourceName(), positions, values, requirement.usageLevel());
    }
}
