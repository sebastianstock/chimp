package chimp.model;

import java.util.Objects;

public record InitialTaskDefinition(
        String id,
        FluentSignature signature,
        ProblemMarking marking) {

    public InitialTaskDefinition {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(marking, "marking");
    }

    public InitialTaskDefinition(String id, FluentSignature signature) {
        this(id, signature, ProblemMarking.UNPLANNED);
    }
}
