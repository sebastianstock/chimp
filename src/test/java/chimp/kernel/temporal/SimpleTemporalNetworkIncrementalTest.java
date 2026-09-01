package chimp.kernel.temporal;

import chimp.model.TemporalBounds;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that {@link SimpleTemporalNetwork} incremental propagation always
 * yields the exact all-pairs shortest-path matrix that a full Floyd-Warshall
 * recomputation from the stored constraints would produce — under random
 * interleavings of time-point creation, constraint addition, and constraint
 * removal.
 */
public class SimpleTemporalNetworkIncrementalTest {

    private static final long INF = TemporalBounds.INFINITY;

    private record Edge(int from, int to, long min, long max) {}

    /** Independent reference: all-pairs shortest paths over stored edges. */
    private static final class Reference {
        final int n;
        final long horizon;
        final List<Edge> edges = new ArrayList<>();
        long[][] d;

        Reference(int maxPoints, long horizon) {
            this.n = maxPoints;
            this.horizon = horizon;
            d = makeInit();
        }

        long[][] makeInit() {
            long[][] m = new long[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    m[i][j] = i == j ? 0 : INF;
                }
            }
            for (int i = 1; i < n; i++) {
                m[0][i] = horizon;
                m[i][0] = 0;
            }
            return m;
        }

        void addTimePoint(int idx) {
            // Fresh points do not touch existing entries (matches STN).
        }

        boolean add(int from, int to, long min, long max) {
            long[][] candidate = makeInit();
            for (Edge e : edges) {
                relax(e, candidate);
            }
            relax(new Edge(from, to, min, max), candidate);
            if (!consistent(candidate)) return false;
            edges.add(new Edge(from, to, min, max));
            d = candidate;
            return true;
        }

        void remove(int from, int to, long min, long max) {
            edges.removeIf(e -> e.from == from && e.to == to && e.min == min && e.max == max);
            long[][] m = makeInit();
            for (Edge e : edges) {
                relax(e, m);
            }
            close(m);
            d = m;
        }

        private void relax(Edge e, long[][] m) {
            m[e.from][e.to] = Math.min(m[e.from][e.to], e.max);
            m[e.to][e.from] = Math.min(m[e.to][e.from], -e.min);
        }

        private boolean consistent(long[][] m) {
            close(m);
            for (int i = 0; i < n; i++) {
                if (m[i][i] < 0) return false;
            }
            return true;
        }

        private void close(long[][] m) {
            // Floyd-Warshall
            for (int k = 0; k < n; k++) {
                for (int i = 0; i < n; i++) {
                    if (m[i][k] == INF) continue;
                    for (int j = 0; j < n; j++) {
                        if (m[k][j] == INF) continue;
                        m[i][j] = Math.min(m[i][j], m[i][k] + m[k][j]);
                    }
                }
            }
        }

        long min(int from, int to) { return -d[to][from]; }

        long max(int from, int to) { return d[from][to]; }
    }

    @Test
    public void incrementalMatchesFullFloydWarshall() {
        Random rnd = new Random(20260828);
        int maxPoints = 40;
        long horizon = 500;
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(horizon, maxPoints);
        Reference ref = new Reference(maxPoints, horizon);

        // Create all time points up front (interleaving is covered in the
        // second test; here exercise dense random constraint traffic).
        for (int i = 0; i < maxPoints - 1; i++) {
            stn.addTimePoint();
            ref.addTimePoint(i + 1);
        }

        int ops = 4000;
        int added = 0;
        for (int op = 0; op < ops; op++) {
            int from = rnd.nextInt(maxPoints);
            int to = rnd.nextInt(maxPoints);
            long min = -200 + rnd.nextInt(400);
            long max = min + rnd.nextInt(200) + 10;

            boolean remove = rnd.nextDouble() < 0.35 && !ref.edges.isEmpty();
            if (remove) {
                Edge e = ref.edges.get(rnd.nextInt(ref.edges.size()));
                stn.removeConstraint(e.from, e.to, e.min, e.max);
                ref.remove(e.from, e.to, e.min, e.max);
            } else {
                boolean ok = ref.add(from, to, min, max);
                boolean okStn = stn.addConstraint(from, to, min, max);
                if (!okStn) {
                    // STN polluted its matrix; caller rebuilds (as TemporalSolver does)
                    stn.rebuild();
                }
                assertEquals("add outcome at op " + op, ok, okStn);
                if (ok) added++;
            }
            assertBoundsEqual(stn, ref, maxPoints);
        }
        assertTrue("should have added constraints", added > 100);
        assertTrue("should have removed constraints", ops - added > 100);
    }

    @Test
    public void incrementalMatchesWithInterleavedTimePoints() {
        Random rnd = new Random(7);
        int maxPoints = 30;
        long horizon = 300;
        SimpleTemporalNetwork stn = new SimpleTemporalNetwork(horizon, maxPoints);
        Reference ref = new Reference(maxPoints, horizon);

        int points = 1;
        for (int op = 0; op < 1500; op++) {
            if (rnd.nextDouble() < 0.25 && points < maxPoints) {
                stn.addTimePoint();
                ref.addTimePoint(points++);
            } else {
                int from = rnd.nextInt(points);
                int to = rnd.nextInt(points);
                long min = -100 + rnd.nextInt(200);
                long max = min + rnd.nextInt(100) + 10;
                boolean remove = rnd.nextDouble() < 0.3 && !ref.edges.isEmpty();
                if (remove) {
                    Edge e = ref.edges.get(rnd.nextInt(ref.edges.size()));
                    stn.removeConstraint(e.from, e.to, e.min, e.max);
                    ref.remove(e.from, e.to, e.min, e.max);
                } else {
                    boolean ok = ref.add(from, to, min, max);
                    boolean okStn = stn.addConstraint(from, to, min, max);
                    if (!okStn) stn.rebuild();
                    assertEquals("add outcome at op " + op, ok, okStn);
                }
            }
            assertBoundsEqual(stn, ref, points);
        }
    }

    private void assertBoundsEqual(SimpleTemporalNetwork stn, Reference ref, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                assertEquals("min(" + i + "," + j + ")", ref.min(i, j), stn.getMinDistance(i, j));
                assertEquals("max(" + i + "," + j + ")", ref.max(i, j), stn.getMaxDistance(i, j));
            }
        }
    }
}