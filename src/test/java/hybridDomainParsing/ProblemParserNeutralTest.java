package hybridDomainParsing;

import static org.junit.Assert.assertEquals;

import chimp.model.FluentSignature;
import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;
import chimp.model.ProblemMarking;
import chimp.model.ProblemOrderingEdge;
import chimp.model.ProblemTemporalConstraint;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ProblemParserNeutralTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parsesCompleteNeutralProblemInDeclarationOrder() throws Exception {
        File problemFile = temporaryFolder.newFile("problem.pdl");
        Files.writeString(problemFile.toPath(), """
                (Problem
                  (ArgumentSymbols r1 room1)
                  (Instances Robot r1)
                  (Instances Room room1 room2)
                  (Fluent f0 At(r1 room1))
                  (Fluent f1 BatteryLevel()(100))
                  (Task t0 move(r1 room1))
                  (Task t1 deliver(r1))
                  (Constraint Release[0,0](f0))
                  (Constraint Duration[5,INF](t0))
                  (Constraint Before(t0,t1))
                  (Ordering t0 t1)
                )
                """);

        ProblemDefinition problem = new ProblemParser(problemFile.getPath()).getProblemDefinition();

        assertEquals(List.of("r1", "room1", "n"), problem.argumentSymbols());
        assertEquals(List.of("Robot", "Room"), List.copyOf(problem.typeInstances().keySet()));
        assertEquals(Map.of("Robot", List.of("r1"), "Room", List.of("room1", "room2")),
                problem.typeInstances());
        assertEquals(List.of(
                new InitialFluentDefinition(
                        "f0", new FluentSignature("At", "r1", "room1"), List.of(), ProblemMarking.OPEN),
                new InitialFluentDefinition(
                        "f1", new FluentSignature("BatteryLevel"), List.of(100), ProblemMarking.OPEN)),
                problem.initialFluents());
        assertEquals(List.of(
                new InitialTaskDefinition(
                        "t0", new FluentSignature("move", "r1", "room1"), ProblemMarking.UNPLANNED),
                new InitialTaskDefinition(
                        "t1", new FluentSignature("deliver", "r1"), ProblemMarking.UNPLANNED)),
                problem.initialTasks());
        assertEquals(List.of(
                new ProblemTemporalConstraint("f0", "f0", new TemporalConstraintTemplate(
                        TemporalRelation.Release, new TemporalBounds(0, 0))),
                new ProblemTemporalConstraint("t0", "t0", new TemporalConstraintTemplate(
                        TemporalRelation.Duration, new TemporalBounds(5, TemporalBounds.INFINITY))),
                new ProblemTemporalConstraint("t0", "t1", new TemporalConstraintTemplate(
                        TemporalRelation.Before))),
                problem.temporalConstraints());
        assertEquals(List.of(new ProblemOrderingEdge("t0", "t1")), problem.orderingEdges());
    }
}
