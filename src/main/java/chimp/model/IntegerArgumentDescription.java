package chimp.model;

import java.util.Objects;
import java.util.Optional;

public record IntegerArgumentDescription(Optional<String> variableName, Optional<Integer> constant) {

    public IntegerArgumentDescription {
        variableName = Objects.requireNonNull(variableName, "variableName");
        constant = Objects.requireNonNull(constant, "constant");
        if (variableName.isPresent() == constant.isPresent()) {
            throw new IllegalArgumentException("Exactly one integer argument value must be present");
        }
    }

    public static IntegerArgumentDescription variable(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new IntegerArgumentDescription(Optional.of(name), Optional.empty());
    }

    public static IntegerArgumentDescription constant(int value) {
        return new IntegerArgumentDescription(Optional.empty(), Optional.of(value));
    }
}
