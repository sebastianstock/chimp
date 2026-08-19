package chimp.kernel.fluent;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import chimp.kernel.Variable;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentNode;
import chimp.model.FluentSignature;
import chimp.model.FluentType;
import chimp.model.TemporalBounds;
import chimp.model.TemporalInterval;

/**
 * Converts CHIMP-native fluent/constraint objects into CHIMP model objects
 * used by the public API (FluentStore, PlanExtractor, etc.).
 */
public final class ModelAdapter {

    public FluentNode toFluentNode(Fluent fluent) {
        Objects.requireNonNull(fluent, "fluent");

        chimp.kernel.symbolic.CompoundSymbolicVariable symbolic =
                fluent.getCompoundSymbolicVariable();
        chimp.kernel.temporal.TemporalVariable interval = fluent.getTemporalVariable();
        return new FluentNode(
                new FluentId(fluent.getID()),
                toFluentType(fluent.getTypeStr()),
                new FluentSignature(symbolic.getPredicateName(), Arrays.asList(symbolic.getGroundArgs())),
                new TemporalInterval(
                        interval.getEST(),
                        interval.getLST(),
                        interval.getEET(),
                        interval.getLET()));
    }

    public ChimpConstraint toChimpConstraint(FluentConstraint constraint) {
        Objects.requireNonNull(constraint, "constraint");

        FluentId from = toFluentId(constraint.getFrom(), "from");
        FluentId to = toFluentId(constraint.getTo(), "to");
        Optional<TemporalBounds> bounds = Optional.ofNullable(constraint.getBounds());
        return new ChimpConstraint(toConstraintKind(constraint.getType()), from, to, bounds);
    }

    private FluentType toFluentType(String component) {
        for (FluentType type : FluentType.values()) {
            if (type.componentName().equals(component)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported fluent component: " + component);
    }

    private ConstraintKind toConstraintKind(FluentConstraint.Type type) {
        return switch (type) {
            case MATCHES -> ConstraintKind.MATCHES;
            case DC -> ConstraintKind.DECOMPOSITION;
            case PRE -> ConstraintKind.PRECONDITION;
            case OPENS -> ConstraintKind.OPENS;
            case CLOSES -> ConstraintKind.CLOSES;
            case BEFORE -> ConstraintKind.BEFORE;
            case UNARYAPPLIED -> ConstraintKind.APPLIED;
            case MOVEDURATION -> ConstraintKind.MOVE_DURATION;
            case AXIOM -> ConstraintKind.AXIOM;
            case RESOURCEUSAGE -> ConstraintKind.RESOURCE_USAGE;
        };
    }

    private FluentId toFluentId(Variable variable, String endpoint) {
        if (!(variable instanceof Fluent)) {
            throw new IllegalArgumentException("Constraint " + endpoint + " endpoint must be a Fluent");
        }
        return new FluentId(variable.getID());
    }
}
