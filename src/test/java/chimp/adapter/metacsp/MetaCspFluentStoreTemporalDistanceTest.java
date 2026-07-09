package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;

import chimp.model.FluentId;
import chimp.model.TemporalBounds;
import chimp.model.TemporalEndpoint;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

/**
 * Characterizes {@link MetaCspFluentStore#temporalDistance} against the raw
 * APSP solver of a real solved plan. A bare solver does not propagate
 * distances, so a planned network is required for non-null assertions.
 */
public class MetaCspFluentStoreTemporalDistanceTest {

    private static final String DOMAIN = "domains/ordered_domain.ddl";
    private static final String PROBLEM = "problems/test_m_drive_robot_1.pdl";

    @Test
    public void matchesApspSolverForEveryEndpointCombination() throws Exception {
        CHIMP chimp = plan();
        FluentNetworkSolver solver = chimp.getFluentSolver();
        MetaCspFluentStore store = new MetaCspFluentStore(solver);

        Fluent[] actions = actions(chimp);
        assertTrue("Expected at least two planned actions", actions.length >= 2);

        boolean anyNonEmpty = false;
        for (Fluent from : actions) {
            for (Fluent to : actions) {
                for (TemporalEndpoint fromEndpoint : TemporalEndpoint.values()) {
                    for (TemporalEndpoint toEndpoint : TemporalEndpoint.values()) {
                        SimpleDistanceConstraint sdc = apspConstraint(
                                solver, from, fromEndpoint, to, toEndpoint);
                        Optional<TemporalBounds> actual = store.temporalDistance(
                                new FluentId(from.getID()), new FluentId(to.getID()),
                                fromEndpoint, toEndpoint);

                        if (sdc == null) {
                            assertTrue("Expected empty for " + from.getID() + "->" + to.getID()
                                    + " " + fromEndpoint + "->" + toEndpoint,
                                    actual.isEmpty());
                        } else {
                            anyNonEmpty = true;
                            assertEquals(
                                    from.getID() + "->" + to.getID() + " " + fromEndpoint + "->" + toEndpoint,
                                    new TemporalBounds(sdc.getMinimum(), sdc.getMaximum()),
                                    actual.orElseThrow());
                        }
                    }
                }
            }
        }
        assertTrue("Expected at least one non-empty distance (test would pass vacuously otherwise)",
                anyNonEmpty);
    }

    private Fluent[] actions(CHIMP chimp) {
        Variable[] extracted = chimp.extractActions();
        Fluent[] actions = new Fluent[extracted.length];
        for (int i = 0; i < extracted.length; i++) {
            actions[i] = (Fluent) extracted[i];
        }
        return actions;
    }

    private SimpleDistanceConstraint apspConstraint(FluentNetworkSolver solver,
                                                    Fluent from, TemporalEndpoint fromEndpoint,
                                                    Fluent to, TemporalEndpoint toEndpoint) {
        APSPSolver apsp = (APSPSolver) ((AllenIntervalNetworkSolver) solver.getConstraintSolvers()[1])
                .getConstraintSolvers()[0];
        TimePoint fromTp = fromEndpoint == TemporalEndpoint.START
                ? from.getAllenInterval().getStart() : from.getAllenInterval().getEnd();
        TimePoint toTp = toEndpoint == TemporalEndpoint.START
                ? to.getAllenInterval().getStart() : to.getAllenInterval().getEnd();
        return apsp.getConstraint(fromTp, toTp);
    }

    private CHIMP plan() throws DomainParsingException {
        CHIMP chimp = new CHIMP.CHIMPBuilder(DOMAIN, PROBLEM)
                .horizon(3600000)
                .valHeuristic(new UnifyDeepestWeightNewestbindingsValOH())
                .build();
        assertTrue(chimp.generatePlan());
        return chimp;
    }
}
