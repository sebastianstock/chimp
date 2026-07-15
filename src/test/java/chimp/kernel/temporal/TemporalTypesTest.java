package chimp.kernel.temporal;

import static org.junit.Assert.*;
import org.junit.Test;

import chimp.model.TemporalBounds;
import chimp.model.TemporalRelation;

import java.util.List;

/**
 * Tests for TemporalVariable, TemporalConstraint, DistanceEdge,
 * and TemporalSolver.
 */
public class TemporalTypesTest {

    private static final long INF = TemporalBounds.INFINITY;

    // --- TemporalVariable ---

    @Test
    public void temporalVariableHasStartAndEnd() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable v = solver.createVariables(1)[0];
        assertTrue(v.start() != v.end());
        assertTrue(v.start() > 0);
        assertTrue(v.end() > 0);
    }

    @Test
    public void temporalVariableBoundsFromOrigin() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable v = solver.createVariables(1)[0];

        // Start and end should be bounded by [0, horizon]
        assertEquals(0, v.getEST());
        assertEquals(1000, v.getLST());
        assertEquals(0, v.getEET());
        assertEquals(1000, v.getLET());
    }

    @Test
    public void temporalVariableBoundsTightenWithConstraints() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable v = solver.createVariables(1)[0];

        // Add duration constraint: duration ∈ [5, 10]
        TemporalConstraint dur = new TemporalConstraint(TemporalRelation.Duration,
                new TemporalBounds(5, 10));
        dur.setFrom(v);
        dur.setTo(v);
        assertTrue(solver.addConstraint(dur));

        // Start still bounded by [0, horizon], but duration tightens LST
        assertEquals(0, v.getEST());
        // start ≤ end - 5 ≤ 1000 - 5 = 995
        assertEquals(995, v.getLST());
        // End = start + duration: EET = 0+5=5, LET = 995+10=1005 but capped by horizon
        assertEquals(5, v.getEET());
        assertEquals(1000, v.getLET());
        // LET = max end = max start + max duration = 1000 + 10 = 1010, but capped by horizon? No, the horizon bounds are origin→end ≤ 1000
        // So end ≤ 1000 (from origin). Start ≤ 1000 (from origin). end - start ∈ [5,10].
        // max end: start at 990, end at 1000. So LET = 1000 ✓
    }

    // --- DistanceEdge ---

    @Test
    public void distanceEdgeCreates() {
        DistanceEdge e = new DistanceEdge(0, 1, 5, 20);
        assertEquals(0, e.fromTimePoint());
        assertEquals(1, e.toTimePoint());
        assertEquals(5, e.min());
        assertEquals(20, e.max());
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceEdgeRejectsInvertedBounds() {
        new DistanceEdge(0, 1, 20, 5);
    }

    // --- TemporalConstraint compilation ---

    @Test
    public void equalsCompilesTwoEdges() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint eq = new TemporalConstraint(TemporalRelation.Equals);
        eq.setFrom(vars[0]);
        eq.setTo(vars[1]);

        List<DistanceEdge> edges = eq.compileToDistanceConstraints();
        assertEquals(2, edges.size());

        // Edge 1: from.start → to.start = [0, 0]
        DistanceEdge e1 = edges.get(0);
        assertEquals(vars[0].start(), e1.fromTimePoint());
        assertEquals(vars[1].start(), e1.toTimePoint());
        assertEquals(0, e1.min());
        assertEquals(0, e1.max());

        // Edge 2: to.end → from.end = [0, 0]
        DistanceEdge e2 = edges.get(1);
        assertEquals(vars[1].end(), e2.fromTimePoint());
        assertEquals(vars[0].end(), e2.toTimePoint());
        assertEquals(0, e2.min());
        assertEquals(0, e2.max());
    }

    @Test
    public void beforeCompilesOneEdge() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Before);
        c.setFrom(vars[0]);
        c.setTo(vars[1]);

        List<DistanceEdge> edges = c.compileToDistanceConstraints();
        assertEquals(1, edges.size());

        // from.end → to.start = [1, INF]
        DistanceEdge e = edges.get(0);
        assertEquals(vars[0].end(), e.fromTimePoint());
        assertEquals(vars[1].start(), e.toTimePoint());
        assertEquals(1, e.min());
        assertEquals(INF, e.max());
    }

    @Test
    public void durationCompilesOneEdge() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable v = solver.createVariables(1)[0];

        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Duration,
                new TemporalBounds(5, 50));
        c.setFrom(v);
        c.setTo(v);

        List<DistanceEdge> edges = c.compileToDistanceConstraints();
        assertEquals(1, edges.size());

        // from.start → from.end = [5, 50]
        DistanceEdge e = edges.get(0);
        assertEquals(v.start(), e.fromTimePoint());
        assertEquals(v.end(), e.toTimePoint());
        assertEquals(5, e.min());
        assertEquals(50, e.max());
    }

    // --- TemporalConstraint defaults ---

    @Test
    public void usesDefaultBoundsWhenNoneGiven() {
        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Before);
        assertEquals(1, c.bounds()[0].minimum());
        assertEquals(INF, c.bounds()[0].maximum());
    }

    @Test
    public void usesCustomBoundsWhenGiven() {
        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(5, 100));
        assertEquals(5, c.bounds()[0].minimum());
        assertEquals(100, c.bounds()[0].maximum());
    }

    // --- TemporalConstraint contract ---

    @Test
    public void clonePreservesTypeAndBounds() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint orig = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(5, 10));
        orig.setFrom(vars[0]);
        orig.setTo(vars[1]);

        TemporalConstraint clone = orig.clone();
        assertEquals(orig.type(), clone.type());
        assertEquals(orig.bounds()[0].minimum(), clone.bounds()[0].minimum());
        assertEquals(orig.bounds()[0].maximum(), clone.bounds()[0].maximum());
    }

    @Test
    public void isEquivalent() {
        TemporalConstraint a = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(1, INF));
        TemporalConstraint b = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(1, INF));
        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(5, 10));

        assertTrue(a.isEquivalent(b));
        assertFalse(a.isEquivalent(c));
    }

    // --- TemporalSolver integration ---

    @Test
    public void propagationWithEqualsConstraint() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        // Set duration of vars[0] to [5, 5]
        TemporalConstraint dur = new TemporalConstraint(TemporalRelation.Duration,
                new TemporalBounds(5, 5));
        dur.setFrom(vars[0]);
        dur.setTo(vars[0]);
        assertTrue(solver.addConstraint(dur));

        // vars[0].end = vars[0].start + 5
        assertEquals(5, vars[0].getEET());

        // Equals: vars[0] == vars[1]
        TemporalConstraint eq = new TemporalConstraint(TemporalRelation.Equals);
        eq.setFrom(vars[0]);
        eq.setTo(vars[1]);
        assertTrue(solver.addConstraint(eq));

        // vars[1] should have same bounds as vars[0]
        assertEquals(vars[0].getEST(), vars[1].getEST());
        assertEquals(vars[0].getLST(), vars[1].getLST());
        assertEquals(vars[0].getEET(), vars[1].getEET());
        assertEquals(vars[0].getLET(), vars[1].getLET());
    }

    @Test
    public void inconsistencyWithContradictoryBounds() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(1);
        TemporalVariable v = vars[0];

        // Constrain: start ∈ [100, 200] via start-end pairs...
        // Actually, use a direct constraint on start via the origin
        // Add: start >= 500 and start <= 400 (contradiction)
        // Use Before: from.end→to.start... no.
        // Simply constrain start to [500, 500] then also [400, 400]

        // Force start=500 by constraining origin→start to [500,500]
        // We can do this with a release: but Release is from origin to start. Let me check.
        // Actually, just use a normal constraint that creates contradictory bounds.
        // Option: add contradictory distance constraints directly to the STN.

        // Simplest: constrain start to both [0, 10] and [20, 30] — no overlap
        // This requires two constraints that both bind the start.
        // But we can't easily create a constraint that ONLY binds start.
        // The Duration constraint binds start→end.
        // Let me use the STN directly.

        solver.stn().addConstraint(0, v.start(), 500, 500);  // start = 500
        assertFalse(solver.stn().addConstraint(0, v.start(), 400, 400));  // start = 400 → contradicts
    }

    @Test
    public void getAdmissibleDistanceBounds() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        // vars[0] before vars[1]
        TemporalConstraint c = new TemporalConstraint(TemporalRelation.Before,
                new TemporalBounds(10, 50));
        c.setFrom(vars[0]);
        c.setTo(vars[1]);
        solver.addConstraint(c);

        // vars[1].start - vars[0].end ∈ [10, 50]
        TemporalBounds b = solver.getAdmissibleDistanceBounds(
                vars[0].end(), vars[1].start());
        assertEquals(10, b.minimum());
        assertEquals(50, b.maximum());
    }

    // --- Composite type compilation (Step 4c) ---

    @Test
    public void duringOrEqualsOrStartsOrFinishesCompiles() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint c = new TemporalConstraint(
                TemporalRelation.DuringOrEqualsOrStartsOrFinishes);
        c.setFrom(vars[0]);
        c.setTo(vars[1]);
        assertTrue(solver.addConstraint(c));

        // Two edges: ts→fs ∈ [0,INF] and fe→te ∈ [0,INF]
        // Both end up bounded by horizon via transitive propagation
        TemporalBounds b1 = solver.getAdmissibleDistanceBounds(vars[1].start(), vars[0].start());
        assertEquals(0, b1.minimum());
        // fe→te edge: vars[0].end→vars[1].end ∈ [0, INF], propagated via origin
        TemporalBounds b2 = solver.getAdmissibleDistanceBounds(vars[0].end(), vars[1].end());
        assertEquals(0, b2.minimum());
    }

    @Test
    public void meetsOrOverlapsOrFinishedByOrContainsCompiles() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint c = new TemporalConstraint(
                TemporalRelation.MeetsOrOverlapsOrFinishedByOrContains);
        c.setFrom(vars[0]);
        c.setTo(vars[1]);
        assertTrue(solver.addConstraint(c));

        // fs→ts ∈ [1,INF], ts→fe ∈ [0,INF]
        TemporalBounds b1 = solver.getAdmissibleDistanceBounds(vars[0].start(), vars[1].start());
        assertEquals(1, b1.minimum());
        TemporalBounds b2 = solver.getAdmissibleDistanceBounds(vars[1].start(), vars[0].end());
        assertEquals(0, b2.minimum());
    }

    @Test
    public void metByOrOverlappedByOrIsFinishedByOrDuringCompiles() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint c = new TemporalConstraint(
                TemporalRelation.MetByOrOverlappedByOrIsFinishedByOrDuring);
        c.setFrom(vars[0]);
        c.setTo(vars[1]);
        assertTrue(solver.addConstraint(c));

        // ts→fs ∈ [1,INF], fs→te ∈ [0,INF]
        TemporalBounds b1 = solver.getAdmissibleDistanceBounds(vars[1].start(), vars[0].start());
        assertEquals(1, b1.minimum());
        TemporalBounds b2 = solver.getAdmissibleDistanceBounds(vars[0].start(), vars[1].end());
        assertEquals(0, b2.minimum());
    }

    @Test
    public void beforeOrMeetsCompiles() {
        TemporalSolver solver = new TemporalSolver(0, 1000, 100);
        TemporalVariable[] vars = solver.createVariables(2);

        TemporalConstraint c = new TemporalConstraint(TemporalRelation.BeforeOrMeets);
        c.setFrom(vars[0]);
        c.setTo(vars[1]);
        assertTrue(solver.addConstraint(c));

        // fe→ts ∈ [0,INF]
        TemporalBounds b = solver.getAdmissibleDistanceBounds(vars[0].end(), vars[1].start());
        assertEquals(0, b.minimum());
    }
}
