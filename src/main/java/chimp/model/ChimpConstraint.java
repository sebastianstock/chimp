package chimp.model;

import java.util.Objects;
import java.util.Optional;

public record ChimpConstraint(
        ConstraintKind kind,
        FluentId from,
        FluentId to,
        Optional<TemporalBounds> temporalBounds) {

    public ChimpConstraint {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        temporalBounds = Objects.requireNonNull(temporalBounds, "temporalBounds");
    }

    public ChimpConstraint(ConstraintKind kind, FluentId from, FluentId to) {
        this(kind, from, to, Optional.empty());
    }

    public ChimpConstraint(
            ConstraintKind kind,
            FluentId from,
            FluentId to,
            TemporalBounds temporalBounds) {
        this(kind, from, to, Optional.of(temporalBounds));
    }
}
