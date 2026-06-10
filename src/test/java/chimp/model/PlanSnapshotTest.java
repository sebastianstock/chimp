package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PlanSnapshotTest {

    @Test
    public void preservesOrderAndProvidesGraphQueries() {
        FluentNode task = node(1, FluentType.TASK, "task");
        FluentNode action = node(2, FluentType.ACTIVITY, "action");
        ChimpConstraint decomposition = new ChimpConstraint(
                ConstraintKind.DECOMPOSITION, task.id(), action.id());
        ChimpConstraint ordering = new ChimpConstraint(
                ConstraintKind.BEFORE, action.id(), task.id());
        PlanSnapshot snapshot = new PlanSnapshot(
                List.of(task, action),
                List.of(decomposition, ordering));

        assertEquals(List.of(task, action), snapshot.fluents());
        assertEquals(List.of(decomposition, ordering), snapshot.constraints());
        assertEquals(action, snapshot.fluent(action.id()).orElseThrow());
        assertEquals(List.of(action), snapshot.fluents(FluentType.ACTIVITY));
        assertEquals(List.of(decomposition), snapshot.incoming(action.id()));
        assertEquals(List.of(decomposition), snapshot.outgoing(task.id()));
        assertEquals(List.of(ordering), snapshot.constraints(ConstraintKind.BEFORE));
        assertFalse(snapshot.fluent(new FluentId(99)).isPresent());
    }

    @Test
    public void defensivelyCopiesInputLists() {
        List<FluentNode> fluents = new ArrayList<>(List.of(node(1, FluentType.ACTIVITY, "action")));
        List<ChimpConstraint> constraints = new ArrayList<>();
        PlanSnapshot snapshot = new PlanSnapshot(fluents, constraints);

        fluents.clear();
        constraints.add(new ChimpConstraint(
                ConstraintKind.BEFORE, new FluentId(1), new FluentId(1)));

        assertEquals(1, snapshot.fluents().size());
        assertEquals(0, snapshot.constraints().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.fluents().clear());
    }

    @Test
    public void rejectsDuplicateIdsAndUnknownConstraintEndpoints() {
        FluentNode action = node(1, FluentType.ACTIVITY, "action");

        assertThrows(IllegalArgumentException.class,
                () -> new PlanSnapshot(List.of(action, action), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new PlanSnapshot(
                        List.of(action),
                        List.of(new ChimpConstraint(
                                ConstraintKind.BEFORE, action.id(), new FluentId(2)))));
        assertThrows(IllegalArgumentException.class,
                () -> new PlanSnapshot(List.of(action), List.of()).incoming(new FluentId(2)));
    }

    private static FluentNode node(long id, FluentType type, String predicate) {
        return new FluentNode(
                new FluentId(id),
                type,
                new FluentSignature(predicate),
                new TemporalInterval(0, 10, 1, 20));
    }
}
