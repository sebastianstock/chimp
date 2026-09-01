package chimp.model;

import java.util.Objects;

public record ProblemOrderingEdge(String fromId, String toId) {

    public ProblemOrderingEdge {
        fromId = requireText(fromId, "fromId");
        toId = requireText(toId, "toId");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
