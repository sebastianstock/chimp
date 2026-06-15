package chimp.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SymbolicConstraintDescription(
        SymbolicRelationKind kind,
        ResolverArgumentReference from,
        Optional<ResolverArgumentReference> to,
        List<String> values) {

    public SymbolicConstraintDescription {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(from, "from");
        to = Objects.requireNonNull(to, "to");
        values = List.copyOf(Objects.requireNonNull(values, "values"));
    }

    public static SymbolicConstraintDescription binary(
            SymbolicRelationKind kind,
            ResolverArgumentReference from,
            ResolverArgumentReference to) {
        return new SymbolicConstraintDescription(kind, from, Optional.of(to), List.of());
    }

    public static SymbolicConstraintDescription unary(
            SymbolicRelationKind kind,
            ResolverArgumentReference argument,
            List<String> values) {
        return new SymbolicConstraintDescription(kind, argument, Optional.empty(), values);
    }
}
