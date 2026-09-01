package characterization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import chimp.kernel.Constraint;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.ModelAdapter;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentNode;
import chimp.model.FluentType;
import chimp.model.PlannedAction;
import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.CHIMPValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestPlannerCharacterization {

    private final ModelAdapter modelAdapter = new ModelAdapter();

    @Test
    public void plansPrimitiveDwrMove() throws DomainParsingException {
        CHIMP chimp = plan("domains/dwr/dwr.ddl", "domains/dwr/test/test_op_move.pdl");

        PlannedAction[] actions = extractActions(chimp);
        assertTrue("Expected the primitive DWR problem to produce an action", actions.length > 0);
        assertTrue("Expected the DWR move action", actions[0].name().contains("!move"));

        FluentNode snapshot = modelAdapter.toFluentNode(findFluentById(chimp, actions[0].id()));
        assertEquals(new FluentId(actions[0].id()), snapshot.id());
        assertEquals(FluentType.ACTIVITY, snapshot.type());
        assertEquals("!move", snapshot.signature().predicate());
        assertEquals(actions[0].est(), snapshot.temporalInterval().earliestStart());
        assertEquals(actions[0].let(), snapshot.temporalInterval().latestEnd());
    }

    @Test
    public void decomposesDwrNavigationIntoActions() throws DomainParsingException {
        CHIMP chimp = plan("domains/dwr/dwr.ddl", "domains/dwr/test/test_m_navigate0.pdl");

        PlannedAction[] actions = extractActions(chimp);
        assertTrue("Expected the DWR navigation method to produce an action", actions.length > 0);
        assertTrue("Expected navigation to contain a move action", actions[0].name().contains("!move"));
    }

    @Test
    public void preservesMobipickActionOrderingAndTemporalSeparation() throws DomainParsingException {
        CHIMP chimp = plan(
                "domains/mobipick/simple_navigation/domain.ddl",
                "domains/mobipick/simple_navigation/problem.pdl");

        PlannedAction[] actions = extractActions(chimp);
        assertTrue("Expected at least two actions in the Mobipick plan", actions.length >= 2);
        assertTrue("Expected the first action to finish before the second starts",
                actions[0].eet() < actions[1].est());

        FluentConstraint before = findBeforeConstraint(chimp);
        assertNotNull("Expected the method ordering to remain in the fluent network", before);
        Fluent from = (Fluent) before.getFrom();
        Fluent to = (Fluent) before.getTo();
        assertTrue("Expected the BEFORE edge to be reflected by the temporal solution",
                from.getAllenInterval().getEET() < to.getAllenInterval().getEST());

        ChimpConstraint snapshot = modelAdapter.toChimpConstraint(before);
        assertEquals(ConstraintKind.BEFORE, snapshot.kind());
        assertEquals(new FluentId(from.getID()), snapshot.from());
        assertEquals(new FluentId(to.getID()), snapshot.to());
    }

    private CHIMP plan(String domainFile, String problemFile) throws DomainParsingException {
        CHIMPValOH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        CHIMP chimp = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                .valHeuristic(valOH)
                .mbEstimator(new LookUpTableDurationEstimator())
                .htnUnification(true)
                .build();

        assertTrue("Planner failed for " + problemFile, chimp.generatePlan());
        return chimp;
    }

    private PlannedAction[] extractActions(CHIMP chimp) {
        PlannedAction[] actions = chimp.extractActions();
        assertNotNull("Action extraction returned null", actions);
        assertTrue("Expected a non-empty plan", actions.length > 0);

        long previousEst = Long.MIN_VALUE;
        for (PlannedAction act : actions) {
            long est = act.est();
            long lst = act.lst();
            long eet = act.eet();
            long let = act.let();
            assertTrue("Extracted actions must be sorted by earliest start", est >= previousEst);
            assertTrue("Earliest start must not exceed latest start", est <= lst);
            assertTrue("Earliest end must not exceed latest end", eet <= let);
            assertTrue("An action must not end before it starts", eet >= est);
            previousEst = est;
        }
        return actions;
    }

    private Fluent findFluentById(CHIMP chimp, int id) {
        for (Object obj : chimp.getFluentSolver().getVariables()) {
            if (obj instanceof Fluent f && f.getID() == id) {
                return f;
            }
        }
        fail("No Fluent found with ID " + id);
        return null;
    }

    private FluentConstraint findBeforeConstraint(CHIMP chimp) {
        for (Constraint constraint : chimp.getFluentSolver().getConstraints()) {
            if (constraint instanceof FluentConstraint) {
                FluentConstraint fluentConstraint = (FluentConstraint) constraint;
                if (fluentConstraint.getType() == FluentConstraint.Type.BEFORE) {
                    return fluentConstraint;
                }
            }
        }
        return null;
    }
}
