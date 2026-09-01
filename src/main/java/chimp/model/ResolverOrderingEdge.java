package chimp.model;

import java.util.Objects;

public record ResolverOrderingEdge(String fromKey, String toKey) {

    public ResolverOrderingEdge {
        fromKey = requireText(fromKey, "fromKey");
        toKey = requireText(toKey, "toKey");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
