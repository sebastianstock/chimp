package chimp.model;

import java.util.List;
import java.util.Objects;

public record TemporalConstraintTemplate(
        TemporalRelation relation,
        List<TemporalBounds> bounds) {

    public TemporalConstraintTemplate {
        Objects.requireNonNull(relation, "relation");
        bounds = List.copyOf(Objects.requireNonNull(bounds, "bounds"));
        for (TemporalBounds bound : bounds) {
            Objects.requireNonNull(bound, "bound");
        }
    }

    public TemporalConstraintTemplate(TemporalRelation relation, TemporalBounds... bounds) {
        this(relation, List.of(bounds));
    }
}
