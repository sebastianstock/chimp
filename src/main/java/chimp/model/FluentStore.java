package chimp.model;

import java.util.List;
import java.util.Optional;

public interface FluentStore {

    Optional<TemporalBounds> temporalDistance(FluentId from, FluentId to,
                                               TemporalEndpoint fromEndpoint,
                                               TemporalEndpoint toEndpoint);

    List<FluentView> fluents();

    Optional<FluentView> fluent(FluentId id);

    List<FluentView> fluents(FluentType type);

    List<FluentView> fluents(FluentMarking marking);

    List<ChimpConstraint> incoming(FluentId id);

    List<ChimpConstraint> incoming(FluentId id, ConstraintKind kind);

    List<ChimpConstraint> outgoing(FluentId id);

    List<ChimpConstraint> outgoing(FluentId id, ConstraintKind kind);
}
