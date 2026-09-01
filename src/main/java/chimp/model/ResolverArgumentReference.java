package chimp.model;

import java.util.Objects;

public record ResolverArgumentReference(String fluentKey, int argumentIndex) {

    public ResolverArgumentReference {
        Objects.requireNonNull(fluentKey, "fluentKey");
        if (fluentKey.isBlank()) {
            throw new IllegalArgumentException("fluentKey must not be blank");
        }
        if (argumentIndex < 0) {
            throw new IllegalArgumentException("argumentIndex must not be negative");
        }
    }
}
