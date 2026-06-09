package chimp.model;

public record FluentId(long value) {

    public FluentId {
        if (value < 0) {
            throw new IllegalArgumentException("Fluent ID must be non-negative");
        }
    }
}
