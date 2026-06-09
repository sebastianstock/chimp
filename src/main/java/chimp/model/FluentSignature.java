package chimp.model;

import java.util.List;
import java.util.Objects;

public record FluentSignature(String predicate, List<String> arguments) {

    public FluentSignature {
        predicate = requireText(predicate, "predicate");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        for (String argument : arguments) {
            requireText(argument, "argument");
        }
    }

    public FluentSignature(String predicate, String... arguments) {
        this(predicate, List.of(arguments));
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
