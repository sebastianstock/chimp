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
import chimp.search.ResolverCandidate;
import chimp.search.ResolverCandidateKind;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import integers.IntegerConstraint;
import org.junit.Test;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import unify.CompoundSymbolicValueConstraint;

import java.util.ArrayList;
import java.util.Collections;
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
        first.setMarking(HTNMetaConstraint.markings.OPEN);
        Fluent second = (Fluent) solver.createVariable();
        second.setName("at", new String[] {"r1", "room2"});
        second.setMarking(HTNMetaConstraint.markings.OPEN);

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

        List<ConstraintNetwork> legacy =
                operator.materializeResolverDescriptions(task, 3, solver, openFluents);
        List<ConstraintNetwork> delegated = operator.expand(task, 3, solver, openFluents);

        assertEquals(selectedPreconditionIdMultiset(legacy), selectedPreconditionIdMultiset(delegated));
        assertEquals(summarizeAsMultiset(legacy), summarizeAsMultiset(delegated));
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

    private List<List<Integer>> selectedPreconditionIds(List<ConstraintNetwork> networks) {
        List<List<Integer>> selections = new ArrayList<>();
        for (ConstraintNetwork network : networks) {
            List<Integer> ids = new ArrayList<>();
            for (Constraint constraint : network.getConstraints()) {
                if (constraint instanceof FluentConstraint fluent
                        && fluent.getType() == FluentConstraint.Type.PRE) {
                    ids.add(fluent.getFrom().getID());
                }
            }
            Collections.sort(ids);
            selections.add(ids);
        }
        return selections;
    }

    private List<String> selectedPreconditionIdMultiset(List<ConstraintNetwork> networks) {
        List<String> selections = selectedPreconditionIds(networks).stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        Collections.sort(selections);
        return selections;
    }

    private List<String> summarizeAsMultiset(List<ConstraintNetwork> networks) {
        List<String> result = new ArrayList<>();
        for (List<String> network : summarize(networks)) {
            Collections.sort(network);
            result.add(String.join(",", network));
        }
        Collections.sort(result);
        return result;
    }

    private List<List<String>> summarize(List<ConstraintNetwork> networks) {
        List<List<String>> result = new ArrayList<>();
        for (ConstraintNetwork network : networks) {
            List<String> constraints = new ArrayList<>();
            for (Constraint constraint : network.getConstraints()) {
                constraints.add(kind(constraint));
            }
            result.add(constraints);
        }
        return result;
    }

    private String kind(Constraint constraint) {
        if (constraint instanceof FluentConstraint fluent) {
            return "fluent:" + fluent.getType();
        }
        if (constraint instanceof AllenIntervalConstraint temporal) {
            return "temporal:" + temporal.getType();
        }
        if (constraint instanceof CompoundSymbolicValueConstraint symbolic) {
            return "symbolic:" + symbolic.getEdgeLabel();
        }
        if (constraint instanceof IntegerConstraint integer) {
            return "integer:" + integer.getType();
        }
        return constraint.getClass().getName();
    }
}
