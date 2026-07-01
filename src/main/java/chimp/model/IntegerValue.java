package chimp.model;

import java.util.Objects;
import java.util.OptionalInt;

public record IntegerValue(OptionalInt value, boolean constant) {

    public IntegerValue {
        Objects.requireNonNull(value, "value");
        if (constant && value.isEmpty()) {
            throw new IllegalArgumentException("A constant integer value must be instantiated");
        }
    }
}
