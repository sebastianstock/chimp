package chimp.kernel.temporal;

import static org.junit.Assert.*;
import org.junit.Test;
import chimp.model.TemporalBounds;

/**
 * Tests for SimpleTemporalNetwork — the STN core of the temporal solver.
 */
public class SimpleTemporalNetworkTest {

    private static final long INF = SimpleTemporalNetwork.INF;

    // --- creation ---

    @Test
    public void createsWithOriginAndHorizon() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        // Origin (time point 0) should be bounded to [0, 0]
        TemporalBounds b = stn.getBounds(0, 0);
        assertEquals(0, b.minimum());
        assertEquals(0, b.maximum());
    }

    @Test
    public void addsTimePoints() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int tp1 = stn.addTimePoint();  // index 1
        int tp2 = stn.addTimePoint();  // index 2
        assertEquals(1, tp1);
        assertEquals(2, tp2);
        assertEquals(3, stn.timePointCount());
    }

    // --- origin bounds ---

    @Test
    public void newTimePointBoundedToHorizon() {
        long horizon = 1000;
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(horizon, 100);
        int tp = stn.addTimePoint();

        TemporalBounds b = stn.getBounds(0, tp);  // t_tp - t_0
        assertEquals(0, b.minimum());
        assertEquals(horizon, b.maximum());
    }

    // --- constraint propagation ---

    @Test
    public void constrainsTimePoint() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int tp1 = stn.addTimePoint();
        int tp2 = stn.addTimePoint();

        // t1 ∈ [0, 1000], t2 ∈ [0, 1000]
        // Add: t2 - t1 ∈ [10, 20]
        assertTrue(stn.addConstraint(tp1, tp2, 10, 20));

        // After propagation, t2-t1 should be exactly [10, 20]
        TemporalBounds b = stn.getBounds(tp1, tp2);
        assertEquals(10, b.minimum());
        assertEquals(20, b.maximum());

        // t2 should still be bounded by horizon from origin
        TemporalBounds b2 = stn.getBounds(0, tp2);
        assertEquals(10, b2.minimum());  // t2 >= t1 + 10 >= 0 + 10
        assertEquals(1000, b2.maximum());
    }

    @Test
    public void transitivePropagation() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();
        int c = stn.addTimePoint();

        // a → b ∈ [5, 5] (b = a + 5)
        assertTrue(stn.addConstraint(a, b, 5, 5));
        // b → c ∈ [10, 10] (c = b + 10)
        assertTrue(stn.addConstraint(b, c, 10, 10));

        // Transitive: a → c should be [15, 15]
        TemporalBounds ac = stn.getBounds(a, c);
        assertEquals(15, ac.minimum());
        assertEquals(15, ac.maximum());
    }

    @Test
    public void multipleConstraintsTightenBounds() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        // First constraint: b - a ∈ [0, 100]
        assertTrue(stn.addConstraint(a, b, 0, 100));
        // Second constraint: b - a ∈ [10, 50] (tighter)
        assertTrue(stn.addConstraint(a, b, 10, 50));

        TemporalBounds ab = stn.getBounds(a, b);
        assertEquals(10, ab.minimum());  // tighter min
        assertEquals(50, ab.maximum());  // tighter max
    }

    // --- inconsistency detection ---

    @Test
    public void detectsInconsistentBounds() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        // b - a ∈ [100, 200]
        assertTrue(stn.addConstraint(a, b, 100, 200));
        // b - a ∈ [0, 50] — contradicts the previous constraint
        assertFalse(stn.addConstraint(a, b, 0, 50));
    }

    @Test
    public void detectsNegativeCycle() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        // a → b ≥ 10
        assertTrue(stn.addConstraint(a, b, 10, INF));
        // b → a ≥ 10 (i.e., a → b ≤ -10) — contradicts a → b ≥ 10
        assertFalse(stn.addConstraint(b, a, 10, INF));
    }

    // --- constraint removal ---

    @Test
    public void removeConstraintLoosensBounds() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        // Tighten: b - a ∈ [50, 50]
        assertTrue(stn.addConstraint(a, b, 50, 50));
        assertEquals(50, stn.getBounds(a, b).minimum());
        assertEquals(50, stn.getBounds(a, b).maximum());

        // Remove the tight constraint
        assertTrue(stn.removeConstraint(a, b, 50, 50));

        // Should revert to origin-implied bounds: a,b ∈ [0,1000] → b-a ∈ [-1000,1000]
        TemporalBounds ab = stn.getBounds(a, b);
        assertEquals(-1000, ab.minimum());
        assertEquals(1000, ab.maximum());
    }

    @Test
    public void removeConstraintDoesNotAffectOthers() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();
        int c = stn.addTimePoint();

        // a → b = 10
        assertTrue(stn.addConstraint(a, b, 10, 10));
        // a → c = 20
        assertTrue(stn.addConstraint(a, c, 20, 20));

        // Remove a → c
        assertTrue(stn.removeConstraint(a, c, 20, 20));

        // a → b should still be 10
        assertEquals(10, stn.getBounds(a, b).minimum());
        assertEquals(10, stn.getBounds(a, b).maximum());
    }

    // --- edge cases ---

    @Test
    public void zeroLengthConstraint() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        // a = b (Meets: b starts exactly when a ends)
        assertTrue(stn.addConstraint(a, b, 0, 0));

        TemporalBounds ab = stn.getBounds(a, b);
        assertEquals(0, ab.minimum());
        assertEquals(0, ab.maximum());
    }

    @Test
    public void maxTimePointsEnforced() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 3);
        stn.addTimePoint(); // index 1
        stn.addTimePoint(); // index 2 — that's 3 total (0,1,2)

        try {
            stn.addTimePoint(); // should throw
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void getMinMaxDistances() {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(1000, 100);
        int a = stn.addTimePoint();
        int b = stn.addTimePoint();

        stn.addConstraint(a, b, 5, 50);

        assertEquals(5, stn.getMinDistance(a, b));
        assertEquals(50, stn.getMaxDistance(a, b));
    }

    // --- incremental vs full propagation correctness ---

    @Test
    public void incrementalMatchesFullPropagation() {
        long H = 100_000;
        int MAX = 200;
        int vars = 10;
        int cons = 20;

        // Build reference using full FW for every step
        SimpleTemporalNetwork ref = buildWithFullPropagation(H, MAX, vars, cons);

        // Build using incremental (the default mode, but simulate
        // typical usage: create vars, then add constraints one by one)
        SimpleTemporalNetwork inc = new SimpleTemporalNetwork(H, MAX);
        // Create time points like TemporalVariable does
        int[] starts = new int[vars];
        int[] ends = new int[vars];
        for (int i = 0; i < vars; i++) {
            starts[i] = inc.addTimePoint();
            ends[i] = inc.addTimePoint();
            inc.addEdgeSilent(starts[i], ends[i], TemporalBounds.INFINITY);
            inc.addEdgeSilent(ends[i], starts[i], 0);
        }
        // First propagation: will do full FW (needsFullPropagation is true)
        inc.propagate();

        // Add constraints using incremental
        for (int i = 0; i < cons; i++) {
            int from = i % vars;
            int to = (i + 1) % vars;
            inc.addConstraint(ends[from], starts[to], (i % 10) + 1, (i % 10) + 51);
        }

        // Compare
        assertMatricesEqual(ref, inc);
    }

    @Test
    public void incrementalPerformanceSmokeTest() {
        long H = 100_000;
        int MAX = 2000;
        int vars = 100;
        int cons = 50;

        SimpleTemporalNetwork inc = new SimpleTemporalNetwork(H, MAX);

        // Create time points
        int[] starts = new int[vars];
        int[] ends = new int[vars];
        for (int i = 0; i < vars; i++) {
            starts[i] = inc.addTimePoint();
            ends[i] = inc.addTimePoint();
            inc.addEdgeSilent(starts[i], ends[i], TemporalBounds.INFINITY);
            inc.addEdgeSilent(ends[i], starts[i], 0);
        }

        // First propagation (full FW)
        long t0 = System.nanoTime();
        inc.propagate();
        long fullFwTime = System.nanoTime() - t0;

        // Add constraints (each uses incremental)
        long t1 = System.nanoTime();
        for (int i = 0; i < cons; i++) {
            int from = i % vars;
            int to = (i + 1) % vars;
            inc.addConstraint(ends[from], starts[to], (i % 10) + 1, (i % 10) + 51);
        }
        long incTime = System.nanoTime() - t1;

        // Query bounds
        long t2 = System.nanoTime();
        for (int i = 0; i < vars; i++) {
            inc.getMinDistance(0, starts[i]); inc.getMaxDistance(0, starts[i]);
            inc.getMinDistance(0, ends[i]);   inc.getMaxDistance(0, ends[i]);
        }
        long queryTime = System.nanoTime() - t2;

        System.out.printf("\n=== STN Incremental Performance (smoke) ===%n");
        System.out.printf("  Vars: %d, Constraints: %d, TimePoints: %d%n",
                vars, cons, inc.timePointCount());
        System.out.printf("  Full FW (first propag): %.3fms%n",
                fullFwTime / 1_000_000.0);
        System.out.printf("  %d incremental constraints: %.3fms (%.3fms/constraint)%n",
                cons, incTime / 1_000_000.0,
                incTime / (1_000_000.0 * cons));
        System.out.printf("  Queries: %.3fms%n", queryTime / 1_000_000.0);

        // Sanity: incremental constraint time should be much less than
        // cons × full FW time (since each increment is O(n²) vs O(n³))
        long perConstraintInc = incTime / Math.max(1, cons);
        assertTrue("Incremental per-constraint should be faster than full FW",
                perConstraintInc < fullFwTime / 2);
    }

    private SimpleTemporalNetwork buildWithFullPropagation(
            long horizon, int maxTP, int vars, int cons) {
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(horizon, maxTP);
        int[] starts = new int[vars];
        int[] ends = new int[vars];
        for (int i = 0; i < vars; i++) {
            starts[i] = stn.addTimePoint();
            ends[i] = stn.addTimePoint();
            // Simulate Duration[0, INF] via addConstraint
            stn.addConstraint(starts[i], ends[i], 0, TemporalBounds.INFINITY);
        }
        for (int i = 0; i < cons; i++) {
            int from = i % vars;
            int to = (i + 1) % vars;
            stn.addConstraint(ends[from], starts[to], (i % 10) + 1, (i % 10) + 51);
        }
        return stn;
    }

    private void assertMatricesEqual(SimpleTemporalNetwork a, SimpleTemporalNetwork b) {
        int n = Math.max(a.timePointCount(), b.timePointCount());
        assertEquals("Time point count mismatch", a.timePointCount(), b.timePointCount());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long aMin = a.getMinDistance(i, j);
                long bMin = b.getMinDistance(i, j);
                long aMax = a.getMaxDistance(i, j);
                long bMax = b.getMaxDistance(i, j);
                if (aMin != bMin || aMax != bMax) {
                    fail(String.format(
                            "Mismatch at [%d][%d]: ref=(%d,%d) inc=(%d,%d)",
                            i, j, aMin, aMax, bMin, bMax));
                }
            }
        }
    }
}
