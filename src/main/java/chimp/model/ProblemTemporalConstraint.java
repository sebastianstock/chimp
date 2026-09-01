package chimp.model;

import java.util.Objects;

public record ProblemTemporalConstraint(
        String fromId,
        String toId,
        TemporalConstraintTemplate template) {

    public ProblemTemporalConstraint {
        fromId = requireText(fromId, "fromId");
        toId = requireText(toId, "toId");
        Objects.requireNonNull(template, "template");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
