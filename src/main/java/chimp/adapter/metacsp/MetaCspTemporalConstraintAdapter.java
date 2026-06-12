package chimp.adapter.metacsp;

import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

public final class MetaCspTemporalConstraintAdapter {

    public AllenIntervalConstraint toAllenIntervalConstraint(TemporalConstraintTemplate template) {
        AllenIntervalConstraint.Type type = AllenIntervalConstraint.Type.valueOf(template.relation().name());
        if (template.bounds().isEmpty()) {
            return new AllenIntervalConstraint(type);
        }

        Bounds[] bounds = template.bounds().stream()
                .map(this::toBounds)
                .toArray(Bounds[]::new);
        return new AllenIntervalConstraint(type, bounds);
    }

    private Bounds toBounds(TemporalBounds bounds) {
        return new Bounds(bounds.minimum(), bounds.maximum());
    }
}
