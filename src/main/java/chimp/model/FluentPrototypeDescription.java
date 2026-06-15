package chimp.model;

import java.util.List;
import java.util.Objects;

public record FluentPrototypeDescription(
        String key,
        String component,
        FluentSignature signature,
        List<IntegerArgumentDescription> integerArguments,
        ProblemMarking marking) {

    public FluentPrototypeDescription {
        key = requireText(key, "key");
        component = requireText(component, "component");
        Objects.requireNonNull(signature, "signature");
        integerArguments = List.copyOf(Objects.requireNonNull(integerArguments, "integerArguments"));
        Objects.requireNonNull(marking, "marking");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
