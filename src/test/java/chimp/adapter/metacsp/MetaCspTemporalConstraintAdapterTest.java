package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;

import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;

public class MetaCspTemporalConstraintAdapterTest {

    private final MetaCspTemporalConstraintAdapter adapter = new MetaCspTemporalConstraintAdapter();

    @Test
    public void compilesBoundedTemplateWithoutChangingValues() {
        AllenIntervalConstraint constraint = adapter.toAllenIntervalConstraint(
                new TemporalConstraintTemplate(
                        TemporalRelation.Duration,
                        new TemporalBounds(5, TemporalBounds.INFINITY)));

        assertEquals(AllenIntervalConstraint.Type.Duration, constraint.getType());
        assertEquals(1, constraint.getBounds().length);
        assertEquals(5, constraint.getBounds()[0].min);
        assertEquals(APSPSolver.INF, constraint.getBounds()[0].max);
    }

    @Test
    public void compilesUnboundedTemplateUsingFrameworkDefaults() {
        AllenIntervalConstraint constraint = adapter.toAllenIntervalConstraint(
                new TemporalConstraintTemplate(TemporalRelation.Meets));

        assertEquals(AllenIntervalConstraint.Type.Meets, constraint.getType());
        assertEquals(0, constraint.getBounds().length);
    }
}
