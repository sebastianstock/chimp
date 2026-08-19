package htn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import chimp.model.ProblemMarking;
import chimp.model.ResolverDescription;
import chimp.model.ResolverOrderingEdge;
import chimp.model.ResolverSemanticKind;
import chimp.model.ResolverTemporalConstraintDescription;
import chimp.model.SymbolicRelationKind;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import chimp.search.ExpansionCandidateDescription;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.model.FluentMarking;
import chimp.search.ResolverCandidate;
import chimp.search.ResolverCandidateKind;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PlanRepositoryItemResolverDescriptionTest {

    @Test
    public void describesCandidatesAndPreservesCompiledConstraintOrder() {
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                1000,
                new String[][] {
                        {"move", "at", "visited"},
                        {"r1", "room1", "room2", "n"}
                },
                new int[] {1, 2});
        Fluent task = (Fluent) solver.createVariable(Fluent.TASK_TYPE_STR);
        task.setName("move", new String[] {"r1", "room2"});
        Fluent first = (Fluent) solver.createVariable();
        first.setName("at", new String[] {"r1", "room1"});
        first.setMarking(FluentMarking.OPEN.ordinal());
        Fluent second = (Fluent) solver.createVariable();
        second.setName("at", new String[] {"r1", "room2"});
        second.setMarking(FluentMarking.OPEN.ordinal());

        HTNPrecondition precondition = new HTNPrecondition(
                "at", new String[] {"?robot", "?place"}, new int[] {0, 0}, 2, "n", "p0");
        precondition.addAdditionalConstraint(new AdditionalConstraintTemplate(
                new TemporalConstraintTemplate(TemporalRelation.Meets), "p0", "task"));
        EffectTemplate effect = new EffectTemplate(
                "e0", "visited", new String[] {"?place"}, 2, Fluent.PLANNED_STATE_TYPE_STR);
        effect.addAdditionalConstraint(new AdditionalConstraintTemplate(
                new TemporalConstraintTemplate(
                        TemporalRelation.Overlaps, new TemporalBounds(1, 10)),
                "task",
                "e0"));
        HTNOperator operator = new HTNOperator(
                "move",
                new String[] {"?robot", "?destination"},
                new HTNPrecondition[] {precondition},
                new EffectTemplate[] {effect},
                7);
        operator.setAdditionalConstraints(new AdditionalConstraintTemplate[0]);
        operator.setVariablesImpossibleValuesMap(Map.of());
        operator.setSubDifferentDefinitions(new hybridDomainParsing.SubDifferentDefinition[0]);
        operator.setIntegerConstraintTemplates(new IntegerConstraintTemplate[0]);

        Fluent[] openFluents = {first, second};
        List<ResolverDescription> descriptions = operator.describeResolvers(task, 3, openFluents);

        assertEquals(2, descriptions.size());
        assertEquals(7, descriptions.get(0).preferenceWeight());
        assertEquals(3, descriptions.get(0).depth());
        assertEquals(ProblemMarking.OPEN, descriptions.get(0).prototypes().get(0).marking());
        assertTrue(descriptions.get(0).semanticKinds().contains(ResolverSemanticKind.PRECONDITION));
        assertTrue(descriptions.get(0).semanticKinds().contains(ResolverSemanticKind.EFFECT));
        assertEquals(1, descriptions.get(0).bindingCount());
        assertEquals(SymbolicRelationKind.EQUAL,
                descriptions.get(0).symbolicConstraints().get(0).kind());
        assertEquals(List.of(
                new ResolverTemporalConstraintDescription(
                        "p0", "task", new TemporalConstraintTemplate(TemporalRelation.Meets)),
                new ResolverTemporalConstraintDescription(
                        "task", "e0", new TemporalConstraintTemplate(
                                TemporalRelation.Overlaps, new TemporalBounds(1, 10)))),
                descriptions.get(0).temporalConstraints());
        assertTrue(descriptions.stream().anyMatch(
                description -> description.selectedPreconditions().get("p0") == first.getID()));
        assertTrue(descriptions.stream().anyMatch(
                description -> description.selectedPreconditions().get("p0") == second.getID()));

        List<ConstraintNetwork> delegated = operator.expand(task, 3, solver, openFluents);
        assertEquals(2, delegated.size());
        for (ConstraintNetwork network : delegated) {
            ResolverCandidate candidate = (ResolverCandidate) network.getSpecilizedAnnotation();
            assertEquals(ResolverCandidateKind.EXPANSION, candidate.metadata().kind());
            assertEquals(task.getID(), candidate.metadata().taskId());
            assertEquals(operator.getId(), candidate.metadata().repositoryItemId().getAsInt());
            assertEquals(3, candidate.metadata().depth());
            assertEquals(7, candidate.metadata().weight().getAsInt());
            assertEquals(1, candidate.metadata().bindingCount());
            assertTrue(candidate.description() instanceof ExpansionCandidateDescription);
        }
    }

    @Test
    public void describesMethodDecompositionAndOrderingWithoutMaterializingPrototypes() {
        FluentNetworkSolver solver = new FluentNetworkSolver(
                0,
                1000,
                new String[][] {
                        {"transport", "pickup", "place"},
                        {"item1", "room1", "n"}
                },
                new int[] {1, 2});
        Fluent task = (Fluent) solver.createVariable(Fluent.TASK_TYPE_STR);
        task.setName("transport", new String[] {"item1", "room1"});
        EffectTemplate pickup = new EffectTemplate(
                "s0", "pickup", new String[] {"?item"}, 2, Fluent.TASK_TYPE_STR);
        EffectTemplate place = new EffectTemplate(
                "s1", "place", new String[] {"?item", "?room"}, 2, Fluent.TASK_TYPE_STR);
        HTNMethod method = new HTNMethod(
                "transport",
                new String[] {"?item", "?room"},
                new HTNPrecondition[0],
                new EffectTemplate[] {pickup, place},
                new OrderingConstraintTemplate[] {new OrderingConstraintTemplate(pickup, place)},
                2);
        method.setAdditionalConstraints(new AdditionalConstraintTemplate[0]);
        method.setVariablesImpossibleValuesMap(Map.of());
        method.setSubDifferentDefinitions(new hybridDomainParsing.SubDifferentDefinition[0]);
        method.setIntegerConstraintTemplates(new IntegerConstraintTemplate[0]);

        ResolverDescription description = method.describeResolvers(task, 1, new Fluent[0]).get(0);

        assertEquals(2, description.prototypes().size());
        assertEquals(ProblemMarking.UNPLANNED, description.prototypes().get(0).marking());
        assertTrue(description.semanticKinds().contains(ResolverSemanticKind.DECOMPOSITION));
        assertTrue(description.semanticKinds().contains(ResolverSemanticKind.ORDERING));
        assertEquals(List.of(new ResolverOrderingEdge("s0", "s1")), description.orderingEdges());
    }
}
