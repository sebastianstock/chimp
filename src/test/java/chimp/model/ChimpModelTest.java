package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ChimpModelTest {

    @Test
    public void fluentIdRejectsNegativeValues() {
        assertEquals(7L, new FluentId(7).value());
        assertThrows(IllegalArgumentException.class, () -> new FluentId(-1));
    }

    @Test
    public void fluentTypeRetainsCurrentComponentNames() {
        assertEquals("Activity", FluentType.ACTIVITY.componentName());
        assertEquals("State", FluentType.STATE.componentName());
        assertEquals("PlannedState", FluentType.PLANNED_STATE.componentName());
        assertEquals("Task", FluentType.TASK.componentName());
    }

    @Test
    public void fluentSignatureDefensivelyCopiesArguments() {
        List<String> arguments = new ArrayList<>(List.of("robot1", "area1"));
        FluentSignature signature = new FluentSignature("robot_at", arguments);

        arguments.set(0, "robot2");

        assertEquals(List.of("robot1", "area1"), signature.arguments());
        assertThrows(UnsupportedOperationException.class,
                () -> signature.arguments().add("area2"));
        assertEquals(new FluentSignature("robot_at", "robot1", "area1"), signature);
    }

    @Test
    public void fluentSignatureRejectsMissingNames() {
        assertThrows(NullPointerException.class, () -> new FluentSignature(null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new FluentSignature(" ", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new FluentSignature("robot_at", List.of("robot1", " ")));
    }

    @Test
    public void temporalBoundsExposeCurrentIntervalShape() {
        TemporalBounds bounds = new TemporalBounds(1, 10, 6, 20);

        assertEquals(1, bounds.earliestStart());
        assertEquals(10, bounds.latestStart());
        assertEquals(6, bounds.earliestEnd());
        assertEquals(20, bounds.latestEnd());
        assertThrows(IllegalArgumentException.class, () -> new TemporalBounds(10, 1, 11, 12));
        assertThrows(IllegalArgumentException.class, () -> new TemporalBounds(5, 6, 4, 7));
    }

    @Test
    public void fluentNodeIsAValueSnapshot() {
        FluentNode node = new FluentNode(
                new FluentId(3),
                FluentType.ACTIVITY,
                new FluentSignature("move", "robot1", "area2"),
                new TemporalBounds(1, 10, 6, 20));

        assertEquals(new FluentId(3), node.id());
        assertEquals("move", node.signature().predicate());
        assertEquals(FluentType.ACTIVITY, node.type());
    }

    @Test
    public void chimpConstraintSupportsOptionalTemporalBounds() {
        FluentId from = new FluentId(1);
        FluentId to = new FluentId(2);
        ChimpConstraint ordering = new ChimpConstraint(ConstraintKind.BEFORE, from, to);

        assertFalse(ordering.temporalBounds().isPresent());

        TemporalBounds bounds = new TemporalBounds(0, 10, 5, 20);
        ChimpConstraint duration = new ChimpConstraint(
                ConstraintKind.MOVE_DURATION, from, from, bounds);

        assertTrue(duration.temporalBounds().isPresent());
        assertEquals(bounds, duration.temporalBounds().get());
    }

}
