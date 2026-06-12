package chimp.model;

import java.util.List;
import java.util.Objects;

public record InitialFluentDefinition(
        String id,
        FluentSignature signature,
        List<Integer> integerConstants,
        ProblemMarking marking) {

    public InitialFluentDefinition {
        id = requireText(id, "id");
        Objects.requireNonNull(signature, "signature");
        integerConstants = List.copyOf(Objects.requireNonNull(integerConstants, "integerConstants"));
        for (Integer constant : integerConstants) {
            Objects.requireNonNull(constant, "integerConstant");
        }
        Objects.requireNonNull(marking, "marking");
    }

    public InitialFluentDefinition(String id, FluentSignature signature, List<Integer> integerConstants) {
        this(id, signature, integerConstants, ProblemMarking.OPEN);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
