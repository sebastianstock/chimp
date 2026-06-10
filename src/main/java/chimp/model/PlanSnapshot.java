package chimp.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PlanSnapshot {

    private final List<FluentNode> fluents;
    private final List<ChimpConstraint> constraints;
    private final Map<FluentId, FluentNode> fluentsById;

    public PlanSnapshot(List<FluentNode> fluents, List<ChimpConstraint> constraints) {
        this.fluents = List.copyOf(Objects.requireNonNull(fluents, "fluents"));
        this.constraints = List.copyOf(Objects.requireNonNull(constraints, "constraints"));

        Map<FluentId, FluentNode> byId = new LinkedHashMap<>();
        for (FluentNode fluent : this.fluents) {
            Objects.requireNonNull(fluent, "fluent");
            if (byId.putIfAbsent(fluent.id(), fluent) != null) {
                throw new IllegalArgumentException("Duplicate fluent ID: " + fluent.id().value());
            }
        }
        for (ChimpConstraint constraint : this.constraints) {
            Objects.requireNonNull(constraint, "constraint");
            requireEndpoint(byId, constraint.from());
            requireEndpoint(byId, constraint.to());
        }
        this.fluentsById = Map.copyOf(byId);
    }

    public List<FluentNode> fluents() {
        return fluents;
    }

    public List<ChimpConstraint> constraints() {
        return constraints;
    }

    public Optional<FluentNode> fluent(FluentId id) {
        return Optional.ofNullable(fluentsById.get(Objects.requireNonNull(id, "id")));
    }

    public List<FluentNode> fluents(FluentType type) {
        Objects.requireNonNull(type, "type");
        List<FluentNode> matching = new ArrayList<>();
        for (FluentNode fluent : fluents) {
            if (fluent.type() == type) {
                matching.add(fluent);
            }
        }
        return List.copyOf(matching);
    }

    public List<ChimpConstraint> incoming(FluentId id) {
        return constraintsFor(Objects.requireNonNull(id, "id"), false);
    }

    public List<ChimpConstraint> outgoing(FluentId id) {
        return constraintsFor(Objects.requireNonNull(id, "id"), true);
    }

    public List<ChimpConstraint> constraints(ConstraintKind kind) {
        Objects.requireNonNull(kind, "kind");
        List<ChimpConstraint> matching = new ArrayList<>();
        for (ChimpConstraint constraint : constraints) {
            if (constraint.kind() == kind) {
                matching.add(constraint);
            }
        }
        return List.copyOf(matching);
    }

    private List<ChimpConstraint> constraintsFor(FluentId id, boolean outgoing) {
        if (!fluentsById.containsKey(id)) {
            throw new IllegalArgumentException("Unknown fluent ID: " + id.value());
        }
        List<ChimpConstraint> matching = new ArrayList<>();
        for (ChimpConstraint constraint : constraints) {
            FluentId endpoint = outgoing ? constraint.from() : constraint.to();
            if (endpoint.equals(id)) {
                matching.add(constraint);
            }
        }
        return List.copyOf(matching);
    }

    private static void requireEndpoint(Map<FluentId, FluentNode> byId, FluentId endpoint) {
        if (!byId.containsKey(endpoint)) {
            throw new IllegalArgumentException("Constraint references unknown fluent ID: " + endpoint.value());
        }
    }
}
