package chimp.model;

import java.util.Objects;

public record ResolverTemporalConstraintDescription(
        String fromKey,
        String toKey,
        TemporalConstraintTemplate constraint) {

    public ResolverTemporalConstraintDescription {
        Objects.requireNonNull(fromKey, "fromKey");
        Objects.requireNonNull(toKey, "toKey");
        Objects.requireNonNull(constraint, "constraint");
    }
}
