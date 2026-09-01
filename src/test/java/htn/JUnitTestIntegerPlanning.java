package htn;

import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.junit.Test;
import htn.valOrderingHeuristics.CHIMPValOH;
import chimp.model.PlannedAction;
import planner.CHIMP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end behavioral contract for planning with integer constraints.
 *
 * <p>The domain {@code domains/simple_integer_domain.ddl} models a battery
 * level as an integer variable. The {@code !move_base} operator requires
 * {@code ?batteryStart > 2} and sets {@code ?batteryEnd = ?batteryStart - 2}.
 * The problem starts the battery at 100. A successful plan therefore contains
 * {@code !move_base} actions whose integer variable values reflect the
 * decrement (100&rarr;98, 98&rarr;96), proving the integer solver participates
 * in search and propagates the arithmetic constraints.
 *
 * <p>This is the behavioral contract for the Milestone 7 integer-solver
 * isolation work: the plan structure and battery transitions must remain
 * identical before and after the integer runtime is moved behind a
 * CHIMP-owned port.
 */
public class JUnitTestIntegerPlanning {

    private static final String domainFile = "domains/simple_integer_domain.ddl";
    private static final String problemFile = "problems/simple_integer_problem.pdl";

    @Test
    public void testPlanWithIntegerConstraints() throws DomainParsingException {
        CHIMPValOH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                .valHeuristic(valOH)
                .htnUnification(true);
        CHIMP chimp = builder.build();

        assertTrue("Planning with integer constraints should find a plan", chimp.generatePlan());

        PlannedAction[] planVector = chimp.extractActions();
        StringBuilder planText = new StringBuilder();
        int actionCount = 0;
        for (PlannedAction act : planVector) {
            if (act.component() != null) {
                actionCount++;
                planText.append(act).append('\n');
            }
        }

        assertEquals("plan should contain 7 actions", 7, actionCount);
        String plan = planText.toString();
        assertTrue("plan should contain two !move_base actions\n" + plan,
                plan.split("!move_base").length - 1 == 2);
        assertTrue("first move_base should decrement battery 100->98\n" + plan,
                plan.contains("!move_base(table1Area)(100, 98)"));
        assertTrue("second move_base should decrement battery 98->96\n" + plan,
                plan.contains("!move_base(table0Area)(98, 96)"));
    }
}
