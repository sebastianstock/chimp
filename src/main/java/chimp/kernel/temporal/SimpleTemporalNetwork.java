package chimp.kernel.temporal;

import chimp.model.TemporalBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple Temporal Network — a distance graph over time points with
 * Floyd-Warshall all-pairs shortest paths propagation.
 *
 * <p>Replaces the Meta-CSP {@code APSPSolver}. Each time point is an
 * integer index. Time point 0 is the origin (value 0). The {@code horizon}
 * bounds all non-origin time points: {@code t_i ∈ [0, horizon]}.
 *
 * <p>Constraints are distance bounds {@code t_to - t_from ∈ [min, max]}.
 * Propagation computes the tightest transitive bounds via Floyd-Warshall
 * (O(n³) where n is the number of active time points).
 *
 * <p>Uses {@link TemporalBounds#INFINITY} as the sentinel for "no bound".
 */
public class SimpleTemporalNetwork {

    static final long INF = TemporalBounds.INFINITY;

    private final long horizon;
    private final int maxTimePoints;
    private final long[][] distance;
    private final List<DistanceEdge> constraints;
    private int timePointCount;

    /** Constructs an STN with the given horizon and maximum time points. */
    public SimpleTemporalNetwork(long horizon, int maxTimePoints) {
        this.horizon = horizon;
        this.maxTimePoints = maxTimePoints;
        this.distance = new long[maxTimePoints][maxTimePoints];
        this.constraints = new ArrayList<>();
        this.timePointCount = 0;
        // Reserve time point 0 = origin
        addTimePointInternal();
    }

    /** Allocates a new time point and returns its index. */
    public int addTimePoint() {
        if (timePointCount >= maxTimePoints) {
            throw new IllegalStateException("Max time points exceeded: " + maxTimePoints);
        }
        return addTimePointInternal();
    }

    private int addTimePointInternal() {
        int idx = timePointCount++;
        for (int i = 0; i < timePointCount; i++) {
            distance[idx][i] = INF;
            distance[i][idx] = INF;
        }
        distance[idx][idx] = 0;
        // Bound to origin: t_idx ∈ [0, horizon]
        if (idx > 0) {
            distance[0][idx] = horizon;   // t_idx - 0 <= horizon
            distance[idx][0] = 0;         // 0 - t_idx <= 0  =>  t_idx >= 0
        }
        return idx;
    }

    /** Returns the number of active time points (including origin). */
    public int timePointCount() { return timePointCount; }

    /** Returns the horizon value. */
    public long horizon() { return horizon; }

    // --- constraint management ---

    /**
     * Adds a distance constraint: {@code t_to - t_from ∈ [min, max]}.
     * @return true if the network remains consistent after propagation
     */
    public boolean addConstraint(int from, int to, long min, long max) {
        addEdge(from, to, max);
        addEdge(to, from, -min);
        if (!propagate()) {
            // Propagation failed — don't store the constraint
            return false;
        }
        constraints.add(new DistanceEdge(from, to, min, max));
        return true;
    }

    /**
     * Removes a distance constraint but does NOT rebuild the matrix.
     * Call {@link #rebuild()} after all silent removals to recompute.
     */
    public void removeConstraintSilent(int from, int to, long min, long max) {
        constraints.removeIf(c -> c.fromTimePoint() == from && c.toTimePoint() == to
                && c.min() == min && c.max() == max);
    }

    /** Rebuilds the distance matrix from stored constraints and propagates. */
    public boolean rebuild() {
        return fromScratch();
    }

    /**
     * Removes a previously-added distance constraint.
     * Rebuilds the entire distance matrix from scratch.
     * @return true if the network remains consistent after propagation
     */
    public boolean removeConstraint(int from, int to, long min, long max) {
        removeConstraintSilent(from, to, min, max);
        return fromScratch();
    }

    private void addEdge(int from, int to, long bound) {
        if (bound < distance[from][to]) {
            distance[from][to] = bound;
        }
    }

    // --- propagation ---

    /**
     * Runs Floyd-Warshall all-pairs shortest paths.
     * @return true if the network is consistent (no negative cycles)
     */
    public boolean propagate() {
        int n = timePointCount;
        for (int k = 0; k < n; k++) {
            long[] dk = distance[k];
            if (dk[k] < 0) return false; // negative cycle via k
            for (int i = 0; i < n; i++) {
                long dik = distance[i][k];
                if (dik == INF) continue;
                long[] di = distance[i];
                for (int j = 0; j < n; j++) {
                    long dkj = dk[j];
                    if (dkj == INF) continue;
                    long newDist = dik + dkj;
                    if (newDist < di[j]) {
                        di[j] = newDist;
                    }
                }
            }
        }
        // Final consistency check
        for (int i = 0; i < n; i++) {
            if (distance[i][i] < 0) return false;
        }
        return true;
    }

    /** Rebuilds the distance matrix from stored constraints. */
    private boolean fromScratch() {
        // Re-initialize
        for (int i = 0; i < timePointCount; i++) {
            for (int j = 0; j < timePointCount; j++) {
                distance[i][j] = (i == j) ? 0 : INF;
            }
        }
        // Re-apply origin bounds
        for (int i = 1; i < timePointCount; i++) {
            distance[0][i] = horizon;
            distance[i][0] = 0;
        }
        // Re-apply all stored constraints
        for (DistanceEdge c : constraints) {
            addEdge(c.fromTimePoint(), c.toTimePoint(), c.max());
            addEdge(c.toTimePoint(), c.fromTimePoint(), -c.min());
        }
        return propagate();
    }

    // --- queries ---

    /**
     * Returns the admissible distance bounds for {@code t_to - t_from}.
     * The lower bound is {@code -d[to][from]}, the upper bound is {@code d[from][to]}.
     */
    public TemporalBounds getBounds(int from, int to) {
        long upper = distance[from][to];
        long lower = -distance[to][from];
        return new TemporalBounds(lower, upper);
    }

    /**
     * Returns the minimum possible value of {@code t_to - t_from}.
     * Equivalent to {@code getBounds(from, to).minimum()}.
     */
    public long getMinDistance(int from, int to) {
        return -distance[to][from];
    }

    /**
     * Returns the maximum possible value of {@code t_to - t_from}.
     * Equivalent to {@code getBounds(from, to).maximum()}.
     */
    public long getMaxDistance(int from, int to) {
        return distance[from][to];
    }

    /** For debugging: dumps the distance matrix. */
    public String dumpMatrix() {
        StringBuilder sb = new StringBuilder();
        int n = timePointCount;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append('\t');
                long v = distance[i][j];
                sb.append(v == INF ? "INF" : Long.toString(v));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
