package chimp.model;

import java.util.Objects;

public record FluentNode(
        FluentId id,
        FluentType type,
        FluentSignature signature,
        TemporalBounds temporalBounds) {

    public FluentNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(temporalBounds, "temporalBounds");
    }
}
