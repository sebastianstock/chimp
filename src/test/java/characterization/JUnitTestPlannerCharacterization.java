package characterization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;

import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestPlannerCharacterization {

    @Test
    public void plansPrimitiveDwrMove() throws DomainParsingException {
        CHIMP chimp = plan("domains/dwr/dwr.ddl", "domains/dwr/test/test_op_move.pdl");

        Fluent[] actions = extractActions(chimp);
        assertTrue("Expected the primitive DWR problem to produce an action", actions.length > 0);
        assertTrue("Expected the DWR move action", actions[0].getName().contains("!move"));
    }

    @Test
    public void decomposesDwrNavigationIntoActions() throws DomainParsingException {
        CHIMP chimp = plan("domains/dwr/dwr.ddl", "domains/dwr/test/test_m_navigate0.pdl");

        Fluent[] actions = extractActions(chimp);
        assertTrue("Expected the DWR navigation method to produce an action", actions.length > 0);
        assertTrue("Expected navigation to contain a move action", actions[0].getName().contains("!move"));
    }

    @Test
    public void preservesMobipickActionOrderingAndTemporalSeparation() throws DomainParsingException {
        CHIMP chimp = plan(
                "domains/mobipick/simple_navigation/domain.ddl",
                "domains/mobipick/simple_navigation/problem.pdl");

        Fluent[] actions = extractActions(chimp);
        assertTrue("Expected at least two actions in the Mobipick plan", actions.length >= 2);
        assertTrue("Expected the first action to finish before the second starts",
                actions[0].getAllenInterval().getEET() < actions[1].getAllenInterval().getEST());

        FluentConstraint before = findBeforeConstraint(chimp);
        assertNotNull("Expected the method ordering to remain in the fluent network", before);
        Fluent from = (Fluent) before.getFrom();
        Fluent to = (Fluent) before.getTo();
        assertTrue("Expected the BEFORE edge to be reflected by the temporal solution",
                from.getAllenInterval().getEET() < to.getAllenInterval().getEST());
    }

    private CHIMP plan(String domainFile, String problemFile) throws DomainParsingException {
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        CHIMP chimp = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                .valHeuristic(valOH)
                .mbEstimator(new LookUpTableDurationEstimator())
                .htnUnification(true)
                .build();

        assertTrue("Planner failed for " + problemFile, chimp.generatePlan());
        return chimp;
    }

    private Fluent[] extractActions(CHIMP chimp) {
        Variable[] extracted = chimp.extractActions();
        assertNotNull("Action extraction returned null", extracted);
        assertTrue("Expected a non-empty plan", extracted.length > 0);

        Fluent[] actions = new Fluent[extracted.length];
        long previousEst = Long.MIN_VALUE;
        for (int i = 0; i < extracted.length; i++) {
            assertTrue("Expected extracted actions to be fluents", extracted[i] instanceof Fluent);
            actions[i] = (Fluent) extracted[i];

            long est = actions[i].getAllenInterval().getEST();
            long lst = actions[i].getAllenInterval().getLST();
            long eet = actions[i].getAllenInterval().getEET();
            long let = actions[i].getAllenInterval().getLET();
            assertTrue("Extracted actions must be sorted by earliest start", est >= previousEst);
            assertTrue("Earliest start must not exceed latest start", est <= lst);
            assertTrue("Earliest end must not exceed latest end", eet <= let);
            assertTrue("An action must not end before it starts", eet >= est);
            previousEst = est;
        }
        return actions;
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
