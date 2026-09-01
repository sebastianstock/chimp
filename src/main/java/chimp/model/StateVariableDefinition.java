package chimp.model;

import java.util.List;
import java.util.Objects;

public record StateVariableDefinition(
        String predicate,
        int fieldIndex,
        List<String> exclusiveValues) {

    public StateVariableDefinition {
        Objects.requireNonNull(predicate, "predicate");
        if (predicate.isBlank()) {
            throw new IllegalArgumentException("predicate must not be blank");
        }
        if (fieldIndex < 0) {
            throw new IllegalArgumentException("fieldIndex must not be negative");
        }
        exclusiveValues = List.copyOf(Objects.requireNonNull(exclusiveValues, "exclusiveValues"));
        if (exclusiveValues.isEmpty()) {
            throw new IllegalArgumentException("exclusiveValues must not be empty");
        }
        for (String value : exclusiveValues) {
            Objects.requireNonNull(value, "exclusiveValue");
            if (value.isBlank()) {
                throw new IllegalArgumentException("exclusiveValue must not be blank");
            }
        }
    }
}
