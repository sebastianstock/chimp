package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import chimp.model.FluentSignature;
import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;
import chimp.model.ProblemOrderingEdge;
import chimp.model.ProblemTemporalConstraint;
import chimp.model.ResourceCapacity;
import chimp.model.ResourceRequirement;
import chimp.model.StateVariableDefinition;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.PlanReportroryItem;
import hybridDomainParsing.ClassicHybridDomain;
import integers.IntegerDomain;
import org.junit.Test;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MetaCspProblemCompilerTest {

    @Test
    public void materializesNeutralProblemWithoutChangingLegacyMetadata() {
        ProblemDefinition problem = new ProblemDefinition(
                List.of("r1", "room1", "n"),
                java.util.Map.of(),
                List.of(
                        new InitialFluentDefinition(
                                "f0", new FluentSignature("At", "r1", "room1"), List.of()),
                        new InitialFluentDefinition(
                                "f1", new FluentSignature("BatteryLevel"), List.of(100))),
                List.of(
                        new InitialTaskDefinition(
                                "t0", new FluentSignature("!move", "r1", "room1")),
                        new InitialTaskDefinition(
                                "t1", new FluentSignature("deliver", "r1"))),
                List.of(new ProblemTemporalConstraint(
                        "t0", "t0", new TemporalConstraintTemplate(
                                TemporalRelation.Duration, new TemporalBounds(5, 10)))),
                List.of(new ProblemOrderingEdge("t0", "t1")));
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                1000,
                new String[][] {
                        {"At", "BatteryLevel", "!move", "deliver"},
                        {"r1", "room1", "n"}
                },
                new int[] {1, 2},
                0,
                200,
                1);

        new MetaCspProblemCompiler().compile(problem, solver, new TestDomain());

        Variable[] variables = solver.getVariables();
        assertEquals(4, variables.length);
        Fluent f0 = (Fluent) variables[0];
        Fluent f1 = (Fluent) variables[1];
        Fluent t0 = (Fluent) variables[2];
        Fluent t1 = (Fluent) variables[3];
        assertEquals(HTNMetaConstraint.markings.OPEN, f0.getMarking());
        assertEquals(100, ((IntegerDomain) f1.getIntegerVariables()[0].getDomain()).getValue());
        assertTrue(f1.getIntegerVariables()[0].isConstant());
        assertEquals(Fluent.ACTIVITY_TYPE_STR, t0.getComponent());
        assertEquals(Fluent.TASK_TYPE_STR, t1.getComponent());
        assertEquals(HTNMetaConstraint.markings.UNPLANNED, t0.getMarking());
        assertTrue(Arrays.stream(solver.getConstraints()).anyMatch(
                constraint -> constraint instanceof AllenIntervalConstraint allen
                        && allen.getType() == AllenIntervalConstraint.Type.Duration));
        assertTrue(Arrays.stream(solver.getConstraints()).anyMatch(
                constraint -> constraint instanceof FluentConstraint fluent
                        && fluent.getType() == FluentConstraint.Type.BEFORE));
    }

    private static final class TestDomain implements ClassicHybridDomain {
        @Override public List<PlanReportroryItem> getOperators() { return List.of(); }
        @Override public List<PlanReportroryItem> getMethods() { return List.of(); }
        @Override public List<StateVariableDefinition> getStateVariableDefinitions() { return List.of(); }
        @Override public String getName() { return "test"; }
        @Override public List<ResourceCapacity> getResourceCapacities() { return List.of(); }
        @Override public List<ResourceRequirement> getFluentResourceUsages() { return List.of(); }
        @Override public int getMaxArgs() { return 2; }
        @Override public int getMaxIntArgs() { return 1; }
        @Override public int getMinIntValue() { return 0; }
        @Override public int getMaxIntValue() { return 200; }
        @Override public String[] getPredicateSymbols() { return new String[0]; }
        @Override public Set<String> getOperatorNames() { return Set.of("!move"); }
    }
}
