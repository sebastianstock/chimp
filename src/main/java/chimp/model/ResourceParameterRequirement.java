package chimp.model;

import java.util.Objects;

public record ResourceParameterRequirement(int position, String value) {

    public ResourceParameterRequirement {
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
