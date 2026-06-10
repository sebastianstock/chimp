package examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import chimp.adapter.metacsp.MetaCspModelAdapter;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentNode;
import chimp.model.FluentSignature;
import chimp.model.FluentType;
import chimp.model.TemporalInterval;
import examples.CHIMPConnector.FluentConstraintStruct;
import examples.CHIMPConnector.FluentStruct;
import examples.CHIMPConnector.PlanResult;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class CHIMPConnectorTest {

    private final MetaCspModelAdapter modelAdapter = new MetaCspModelAdapter();

    @Test
    public void fluentStructPreservesIndependentSnapshotValues() {
        FluentNode node = new FluentNode(
                new FluentId(7),
                FluentType.ACTIVITY,
                new FluentSignature("move", "robot1", "area2"),
                new TemporalInterval(1, 10, 6, 20));

        FluentStruct converted = new FluentStruct(node);

        assertEquals("move(robot1 area2)", converted.name);
        assertEquals("Activity", converted.type);
        assertEquals(7, converted.id);
        assertEquals(1, converted.est);
        assertEquals(10, converted.lst);
        assertEquals(6, converted.eet);
        assertEquals(20, converted.let);
        assertNull(converted.preconditions);
    }

    @Test
    public void constraintStructPreservesLegacyConstraintNames() {
        Map<ConstraintKind, String> expected = Map.of(
                ConstraintKind.DECOMPOSITION, "DC",
                ConstraintKind.PRECONDITION, "PRE",
                ConstraintKind.APPLIED, "UNARYAPPLIED",
                ConstraintKind.MOVE_DURATION, "MOVEDURATION");

        for (Map.Entry<ConstraintKind, String> entry : expected.entrySet()) {
            FluentConstraintStruct converted = new FluentConstraintStruct(
                    new ChimpConstraint(entry.getKey(), new FluentId(1), new FluentId(2)),
                    true);

            assertEquals(1, converted.fromId);
            assertEquals(2, converted.toId);
            assertEquals(entry.getValue(), converted.type);
            assertTrue(converted.negativeEffect);
        }
    }

    @Test
    public void legacyConstructorsPreserveValues() {
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                100,
                new String[][] {
                    {"move"},
                    {"robot1"},
                    {"area2"}
                },
                new int[] {1, 1, 1});
        Fluent fluent = (Fluent) solver.createVariable(Fluent.ACTIVITY_TYPE_STR);
        fluent.setName("move", new String[] {"robot1", "area2"});

        FluentNode snapshot = modelAdapter.toFluentNode(fluent);
        FluentStruct converted = new FluentStruct(fluent);

        assertEquals(snapshot.id().value(), converted.id);
        assertEquals("move(robot1 area2)", converted.name);
        assertEquals(snapshot.type().componentName(), converted.type);
        assertEquals(snapshot.temporalInterval().earliestStart(), converted.est);
        assertEquals(snapshot.temporalInterval().latestStart(), converted.lst);
        assertEquals(snapshot.temporalInterval().earliestEnd(), converted.eet);
        assertEquals(snapshot.temporalInterval().latestEnd(), converted.let);

        FluentConstraint source = new FluentConstraint(FluentConstraint.Type.DC);
        source.setFrom(fluent);
        source.setTo(fluent);
        source.setNegativeEffect(true);
        FluentConstraintStruct constraint = new FluentConstraintStruct(source);

        assertEquals(fluent.getID(), constraint.fromId);
        assertEquals(fluent.getID(), constraint.toId);
        assertEquals("DC", constraint.type);
        assertTrue(constraint.negativeEffect);
    }

    @Test
    public void legacyFluentConstructorAcceptsUngroundedSymbols() {
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                100,
                new String[][] {
                    {"move"},
                    {"robot1", "robot2"},
                    {"area1", "area2"}
                },
                new int[] {1, 1, 1});
        Fluent fluent = (Fluent) solver.createVariable(Fluent.ACTIVITY_TYPE_STR);
        fluent.setName("move", new String[] {"?robot", "area2"});

        assertFluentValuesEqual(fluent, new FluentStruct(fluent));
    }

    @Test
    public void legacyFluentConstructorAcceptsCustomComponents() {
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                100,
                new String[][] {
                    {"future"}
                },
                new int[] {1});
        Fluent fluent = (Fluent) solver.createVariable("Time");
        fluent.setName("future", new String[0]);

        assertFluentValuesEqual(fluent, new FluentStruct(fluent));
    }

    @Test
    public void plansMobipickWithoutChangingConnectorResultShape() {
        PlanResult result = new CHIMPConnector().plan(
                "domains/mobipick/simple_navigation/problem.pdl",
                "domains/mobipick/simple_navigation/domain.ddl");

        assertTrue(result.foundPlan);
        assertNotNull(result.fluents);
        assertNotNull(result.allFluents);
        assertNotNull(result.fluentConstraints);
        assertTrue(result.fluents.length > 0);
        assertTrue(result.allFluents.length >= result.fluents.length);
        assertTrue(result.fluentConstraints.length > 0);

        Map<Integer, FluentStruct> allById = Arrays.stream(result.allFluents)
                .collect(Collectors.toMap(fluent -> fluent.id, Function.identity()));
        for (FluentStruct action : result.fluents) {
            FluentStruct allFluent = allById.get(action.id);
            assertNotNull(allFluent);
            assertFluentValuesEqual(allFluent, action);
            assertNotNull(action.preconditions);
            for (FluentStruct precondition : action.preconditions) {
                assertNotNull(allById.get(precondition.id));
            }
        }

        assertTrue(Arrays.stream(result.fluentConstraints)
                .allMatch(constraint -> allById.containsKey(constraint.fromId)
                        && allById.containsKey(constraint.toId)));
        assertTrue(Arrays.stream(result.fluentConstraints)
                .anyMatch(constraint -> "BEFORE".equals(constraint.type)));
        assertFalse(Arrays.stream(result.fluentConstraints)
                .anyMatch(constraint -> "DECOMPOSITION".equals(constraint.type)
                        || "PRECONDITION".equals(constraint.type)
                        || "APPLIED".equals(constraint.type)
                        || "MOVE_DURATION".equals(constraint.type)));
    }

    private static void assertFluentValuesEqual(FluentStruct expected, FluentStruct actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.type, actual.type);
        assertEquals(expected.id, actual.id);
        assertEquals(expected.est, actual.est);
        assertEquals(expected.lst, actual.lst);
        assertEquals(expected.eet, actual.eet);
        assertEquals(expected.let, actual.let);
    }

    private static void assertFluentValuesEqual(Fluent expected, FluentStruct actual) {
        assertEquals(expected.getName(), actual.name);
        assertEquals(expected.getTypeStr(), actual.type);
        assertEquals(expected.getID(), actual.id);
        assertEquals(expected.getAllenInterval().getEST(), actual.est);
        assertEquals(expected.getAllenInterval().getLST(), actual.lst);
        assertEquals(expected.getAllenInterval().getEET(), actual.eet);
        assertEquals(expected.getAllenInterval().getLET(), actual.let);
        assertNull(actual.preconditions);
    }
}
