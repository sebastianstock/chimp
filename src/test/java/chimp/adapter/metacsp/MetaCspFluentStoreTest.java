package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import org.junit.Before;
import org.junit.Test;

import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentMarking;
import chimp.model.FluentType;
import chimp.model.FluentView;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;

public class MetaCspFluentStoreTest {

    private FluentNetworkSolver solver;
    private MetaCspFluentStore store;
    private Fluent first;
    private Fluent second;

    @Before
    public void setUp() {
        solver = new FluentNetworkSolver(
                0,
                100,
                new String[][] {
                    {"move", "deliver"},
                    {"robot1", "robot2"},
                    {"area1", "area2"}
                },
                new int[] {1, 1, 1},
                0,
                10,
                2);
        store = new MetaCspFluentStore(solver);

        first = createFluent(Fluent.ACTIVITY_TYPE_STR, "move", "robot1", "area1");
        first.setMarking(HTNMetaConstraint.markings.OPEN);

        second = createFluent(Fluent.TASK_TYPE_STR, "deliver", "robot2", "area2");
        second.setMarking(HTNMetaConstraint.markings.UNPLANNED);

        first.getIntegerVariables()[0].setValue(4);
        first.getIntegerVariables()[1].setConstantValue(7);
    }

    @Test
    public void preservesSolverOrderAndSupportsLookup() {
        List<FluentView> fluents = store.fluents();

        assertEquals(List.of(new FluentId(first.getID()), new FluentId(second.getID())),
                fluents.stream().map(FluentView::id).toList());
        assertEquals(first.getID(), store.fluent(new FluentId(first.getID())).orElseThrow().id().value());
        assertEquals(first.toString(), fluents.get(0).toString());
        assertTrue(store.fluent(new FluentId(999)).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> fluents.add(fluents.get(0)));
    }

    @Test
    public void filtersByTypeAndMarking() {
        assertEquals(List.of(new FluentId(first.getID())),
                store.fluents(FluentType.ACTIVITY).stream().map(FluentView::id).toList());
        assertEquals(List.of(new FluentId(second.getID())),
                store.fluents(FluentMarking.UNPLANNED).stream().map(FluentView::id).toList());
    }

    @Test
    public void exposesImmutableIntegerState() {
        FluentView view = store.fluent(new FluentId(first.getID())).orElseThrow();

        assertEquals(OptionalInt.of(4), view.integerValues().get(0).value());
        assertEquals(OptionalInt.of(7), view.integerValues().get(1).value());
        assertTrue(view.integerValues().get(1).constant());
        assertThrows(UnsupportedOperationException.class,
                () -> view.integerValues().add(view.integerValues().get(0)));
    }

    @Test
    public void queriesConstraintsByDirectionKindAndSolverOrder() {
        FluentConstraint before = constraint(FluentConstraint.Type.BEFORE, first, second);
        FluentConstraint decomposition = constraint(FluentConstraint.Type.DC, first, second);
        assertTrue(solver.addConstraints(before, decomposition));

        List<ChimpConstraint> outgoing = store.outgoing(new FluentId(first.getID()));
        List<ConstraintKind> solverOrder = Arrays.stream(solver.getConstraints())
                .filter(FluentConstraint.class::isInstance)
                .map(FluentConstraint.class::cast)
                .filter(constraint -> constraint.getFrom().equals(first))
                .map(constraint -> constraint.getType() == FluentConstraint.Type.DC
                        ? ConstraintKind.DECOMPOSITION
                        : ConstraintKind.BEFORE)
                .toList();
        assertEquals(solverOrder, outgoing.stream().map(ChimpConstraint::kind).toList());
        assertEquals(List.of(ConstraintKind.DECOMPOSITION),
                store.incoming(new FluentId(second.getID()), ConstraintKind.DECOMPOSITION)
                        .stream().map(ChimpConstraint::kind).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> outgoing.add(outgoing.get(0)));
    }

    @Test
    public void rejectsUnknownQueryIdsAndUnsupportedMarkings() {
        FluentId unknown = new FluentId(999);
        assertThrows(IllegalArgumentException.class, () -> store.incoming(unknown));
        assertThrows(IllegalArgumentException.class, () -> store.outgoing(unknown));

        first.setMarking("unsupported");
        assertThrows(IllegalArgumentException.class,
                () -> store.fluent(new FluentId(first.getID())));
    }

    @Test
    public void mapsEverySupportedMarking() {
        for (HTNMetaConstraint.markings marking : HTNMetaConstraint.markings.values()) {
            first.setMarking(marking);
            assertEquals(FluentMarking.valueOf(marking.name()),
                    store.fluent(new FluentId(first.getID())).orElseThrow().marking());
        }
    }

    private Fluent createFluent(String type, String predicate, String... arguments) {
        Fluent fluent = (Fluent) solver.createVariable(type);
        fluent.setName(predicate, arguments);
        return fluent;
    }

    private FluentConstraint constraint(FluentConstraint.Type type, Fluent from, Fluent to) {
        FluentConstraint constraint = new FluentConstraint(type);
        constraint.setFrom(from);
        constraint.setTo(to);
        return constraint;
    }
}
