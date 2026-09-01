package chimp.model;

import java.util.Objects;

public record ResourceCapacity(String resourceName, int capacity) {

    public ResourceCapacity {
        Objects.requireNonNull(resourceName, "resourceName");
        if (resourceName.isBlank()) {
            throw new IllegalArgumentException("resourceName must not be blank");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
    }
}
