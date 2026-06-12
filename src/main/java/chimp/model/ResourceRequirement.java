package chimp.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ResourceRequirement(
        String resourceName,
        Optional<String> fluentType,
        List<ResourceParameterRequirement> parameterRequirements,
        int usageLevel) {

    public ResourceRequirement {
        resourceName = requireText(resourceName, "resourceName");
        fluentType = Objects.requireNonNull(fluentType, "fluentType");
        fluentType.ifPresent(value -> requireText(value, "fluentType"));
        parameterRequirements = List.copyOf(
                Objects.requireNonNull(parameterRequirements, "parameterRequirements"));
        for (ResourceParameterRequirement requirement : parameterRequirements) {
            Objects.requireNonNull(requirement, "parameterRequirement");
        }
        if (usageLevel < 0) {
            throw new IllegalArgumentException("usageLevel must not be negative");
        }
    }

    public ResourceRequirement(
            String resourceName,
            List<ResourceParameterRequirement> parameterRequirements,
            int usageLevel) {
        this(resourceName, Optional.empty(), parameterRequirements, usageLevel);
    }

    public ResourceRequirement(
            String resourceName,
            String fluentType,
            List<ResourceParameterRequirement> parameterRequirements,
            int usageLevel) {
        this(resourceName, Optional.of(fluentType), parameterRequirements, usageLevel);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
