package chimp.adapter.metacsp;

import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;
import resourceFluent.ResourceUsageTemplate;

public final class MetaCspResourceRequirementAdapter {

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
