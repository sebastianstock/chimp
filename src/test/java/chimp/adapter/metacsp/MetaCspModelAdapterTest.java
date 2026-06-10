package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.metacsp.time.Bounds;

import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentNode;
import chimp.model.FluentSignature;
import chimp.model.FluentType;
import chimp.model.PlanSnapshot;
import chimp.model.TemporalBounds;
import chimp.model.TemporalInterval;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class MetaCspModelAdapterTest {

    private MetaCspModelAdapter adapter;
    private FluentNetworkSolver solver;
    private Fluent from;
    private Fluent to;

    @Before
    public void setUp() {
        adapter = new MetaCspModelAdapter();
        solver = new FluentNetworkSolver(
                0,
                100,
                new String[][] {
                    {"move", "robot_at"},
                    {"robot1", "robot2"},
                    {"area1", "area2"}
                },
                new int[] {1, 1, 1});
        from = (Fluent) solver.createVariable(Fluent.ACTIVITY_TYPE_STR);
        from.setName("move", new String[] {"robot1", "area1"});
        to = (Fluent) solver.createVariable(Fluent.TASK_TYPE_STR);
        to.setName("robot_at", new String[] {"robot2", "area2"});
    }

    @Test
    public void convertsGroundFluentToIndependentSnapshot() {
        FluentNode node = adapter.toFluentNode(from);

        assertEquals(new FluentId(from.getID()), node.id());
        assertEquals(FluentType.ACTIVITY, node.type());
        assertEquals(new FluentSignature("move", "robot1", "area1"), node.signature());
        assertEquals(new TemporalInterval(
                from.getAllenInterval().getEST(),
                from.getAllenInterval().getLST(),
                from.getAllenInterval().getEET(),
                from.getAllenInterval().getLET()), node.temporalInterval());
    }

    @Test
    public void mapsEveryFluentComponentType() {
        Map<String, FluentType> expected = Map.of(
                Fluent.ACTIVITY_TYPE_STR, FluentType.ACTIVITY,
                Fluent.STATE_TYPE_STR, FluentType.STATE,
                Fluent.PLANNED_STATE_TYPE_STR, FluentType.PLANNED_STATE,
                Fluent.TASK_TYPE_STR, FluentType.TASK);

        for (Map.Entry<String, FluentType> entry : expected.entrySet()) {
            Fluent fluent = (Fluent) solver.createVariable(entry.getKey());
            fluent.setName("move", new String[] {"robot1", "area1"});
            assertEquals(entry.getValue(), adapter.toFluentNode(fluent).type());
        }
    }

    @Test
    public void mapsEveryFluentConstraintKind() {
        Map<FluentConstraint.Type, ConstraintKind> expected = new EnumMap<>(FluentConstraint.Type.class);
        expected.put(FluentConstraint.Type.MATCHES, ConstraintKind.MATCHES);
        expected.put(FluentConstraint.Type.DC, ConstraintKind.DECOMPOSITION);
        expected.put(FluentConstraint.Type.PRE, ConstraintKind.PRECONDITION);
        expected.put(FluentConstraint.Type.OPENS, ConstraintKind.OPENS);
        expected.put(FluentConstraint.Type.CLOSES, ConstraintKind.CLOSES);
        expected.put(FluentConstraint.Type.BEFORE, ConstraintKind.BEFORE);
        expected.put(FluentConstraint.Type.UNARYAPPLIED, ConstraintKind.APPLIED);
        expected.put(FluentConstraint.Type.MOVEDURATION, ConstraintKind.MOVE_DURATION);
        expected.put(FluentConstraint.Type.AXIOM, ConstraintKind.AXIOM);
        expected.put(FluentConstraint.Type.RESOURCEUSAGE, ConstraintKind.RESOURCE_USAGE);

        for (Map.Entry<FluentConstraint.Type, ConstraintKind> entry : expected.entrySet()) {
            FluentConstraint source = constraint(entry.getKey());
            ChimpConstraint converted = adapter.toChimpConstraint(source);

            assertEquals(entry.getValue(), converted.kind());
            assertEquals(new FluentId(from.getID()), converted.from());
            assertEquals(new FluentId(to.getID()), converted.to());
        }
    }

    @Test
    public void copiesMoveDurationBounds() {
        FluentConstraint source = new FluentConstraint(
                FluentConstraint.Type.MOVEDURATION,
                new Bounds(5, 20));
        source.setFrom(from);
        source.setTo(from);

        ChimpConstraint converted = adapter.toChimpConstraint(source);

        assertTrue(converted.temporalBounds().isPresent());
        assertEquals(new TemporalBounds(5, 20), converted.temporalBounds().get());
    }

    @Test
    public void leavesUnboundedConstraintsWithoutBounds() {
        assertFalse(adapter.toChimpConstraint(constraint(FluentConstraint.Type.BEFORE))
                .temporalBounds().isPresent());
    }

    @Test
    public void rejectsUnknownFluentComponents() {
        Fluent unknown = (Fluent) solver.createVariable("Unknown");
        unknown.setName("move", new String[] {"robot1", "area1"});

        assertThrows(IllegalArgumentException.class, () -> adapter.toFluentNode(unknown));
    }

    @Test
    public void rejectsConstraintsWithoutConcreteFluentEndpoints() {
        FluentConstraint constraint = new FluentConstraint(FluentConstraint.Type.BEFORE);
        constraint.setTo(to);

        assertThrows(IllegalArgumentException.class, () -> adapter.toChimpConstraint(constraint));
    }

    @Test
    public void createsSnapshotFromGroundedActivityAndTaskGraph() {
        FluentConstraint before = constraint(FluentConstraint.Type.BEFORE);
        assertTrue(solver.addConstraint(before));

        PlanSnapshot snapshot = adapter.toPlanSnapshot(solver);

        assertEquals(2, snapshot.fluents().size());
        assertEquals(1, snapshot.constraints().size());
        assertEquals(ConstraintKind.BEFORE, snapshot.constraints().get(0).kind());
    }

    @Test
    public void rejectsUngroundedPlannedFluentsInSnapshot() {
        Fluent ungrounded = (Fluent) solver.createVariable(Fluent.ACTIVITY_TYPE_STR);
        ungrounded.setName("move", new String[] {"?robot", "area1"});

        assertThrows(IllegalStateException.class, () -> adapter.toPlanSnapshot(solver));
    }

    private FluentConstraint constraint(FluentConstraint.Type type) {
        FluentConstraint constraint = new FluentConstraint(type);
        constraint.setFrom(from);
        constraint.setTo(to);
        return constraint;
    }
}
