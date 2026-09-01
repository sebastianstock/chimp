package chimp.model;

import java.util.List;
import java.util.Objects;

public record ResolverIntegerConstraintDescription(
        IntegerConstraintKind kind,
        List<ResolverArgumentReference> arguments,
        String operator1,
        String operator2,
        int constant) {

    public ResolverIntegerConstraintDescription {
        Objects.requireNonNull(kind, "kind");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
    }
}
