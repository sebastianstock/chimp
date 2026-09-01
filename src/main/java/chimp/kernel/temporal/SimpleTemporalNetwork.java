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
 * Propagation computes the tightest transitive bounds. Two strategies:
 * <ul>
 *   <li><b>Incremental</b> (O(n²)) — propagates only through newly-tightened
 *       edges. Used by {@link #propagate()} when {@link #addEdgeSilent} was
 *       called since the last propagation.</li>
 *   <li><b>Full Floyd-Warshall</b> (O(n³)) — recomputes entire distance
 *       matrix. Used by {@link #rebuild()} for constraint removal.</li>
 * </ul>
 *
 * <p>Uses {@link TemporalBounds#INFINITY} as the sentinel for "no bound".
 */
public class SimpleTemporalNetwork {

    static final long INF = TemporalBounds.INFINITY;

    private final long horizon;
    private final int maxTimePoints;
    private final long[][] distance;
    private final List<DistanceEdge> constraints;
    private final List<int[]> tightenedEdges;
    private final List<Integer> freeTimePoints = new ArrayList<>();
    private int timePointCount;

    /** Constructs an STN with the given horizon and maximum time points. */
    public SimpleTemporalNetwork(long horizon, int maxTimePoints) {
        this.horizon = horizon;
        this.maxTimePoints = maxTimePoints;
        this.distance = new long[maxTimePoints][maxTimePoints];
        this.constraints = new ArrayList<>();
        this.tightenedEdges = new ArrayList<>();
        this.timePointCount = 0;
        // Reserve time point 0 = origin
        addTimePoint();
    }

    /**
     * Allocates a new time point (reusing a freed index when available) and
     * returns its index. Reuses the lowest freed index and shrinks the used
     * range when the top index is freed, so the matrix stays compact.
     */
    public int addTimePoint() {
        int idx;
        if (!freeTimePoints.isEmpty()) {
            idx = freeTimePoints.remove(0);
        } else {
            if (timePointCount >= maxTimePoints) {
                throw new IllegalStateException("Max time points exceeded: " + maxTimePoints);
            }
            idx = timePointCount++;
        }
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
        // A fresh time point only adds INF edges (plus its origin bounds);
        // it cannot improve any existing distance, so the matrix stays
        // fully propagated and only needs incremental propagation later.
        // Close paths through the new point's origin edges right away so
        // queries are exact even before the next constraint is added.
        closeThroughOrigin();
        return idx;
    }

    /**
     * Frees a time point for later reuse. The caller must ensure that all
     * constraints touching this time point have been removed (or will be
     * removed by a subsequent {@link #rebuild()}); the point's matrix row
     * and column are neutralized immediately.
     */
    public void freeTimePoint(int idx) {
        if (idx <= 0 || idx >= timePointCount) {
            throw new IllegalArgumentException("Bad time point: " + idx);
        }
        for (int i = 0; i < timePointCount; i++) {
            distance[idx][i] = INF;
            distance[i][idx] = INF;
        }
        distance[idx][idx] = 0;
        if (idx == timePointCount - 1) {
            // Shrink the used range; the freed index is gone for good.
            timePointCount--;
        } else if (!freeTimePoints.contains(idx)) {
            freeTimePoints.add(idx);
        }
    }

    /** Returns the number of active time points (including origin). */
    public int timePointCount() { return timePointCount; }

    /** Returns the horizon value. */
    public long horizon() { return horizon; }

    // --- constraint management ---

    /**
     * Returns true if a distance constraint {@code t_to - t_from ∈ [min, max]}
     * can be added to the current (fully propagated) network.  Adding an edge
     * is infeasible iff it closes a negative cycle: {@code max + d[to][from] < 0}
     * (cycle through the new upper-bound edge) or {@code -min + d[from][to] < 0}
     * (cycle through the new lower-bound edge).  Unbounded (INF sentinel)
     * bounds add no edge and cannot close a cycle.
     */
    public boolean isAddFeasible(int from, int to, long min, long max) {
        if (max != INF) {
            long reverse = distance[to][from];
            if (reverse != INF && max + reverse < 0) return false;
        }
        if (min != -INF) {
            long forward = distance[from][to];
            if (forward != INF && -min + forward < 0) return false;
        }
        return true;
    }

    /**
     * Adds a distance constraint: {@code t_to - t_from ∈ [min, max]}.
     * Uses incremental propagation for efficiency.
     * @return true if the network remains consistent after propagation
     */
    public boolean addConstraint(int from, int to, long min, long max) {
        addEdgeSilent(from, to, max);
        addEdgeSilent(to, from, -min);
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

    /**
     * Tightens an edge without propagating. If the bound improves the
     * existing distance, records the edge for later incremental propagation.
     * Use with {@link #propagate()} for batch-constrain-then-propagate.
     */
    public void addEdgeSilent(int from, int to, long bound) {
        if (bound < distance[from][to]) {
            distance[from][to] = bound;
            tightenedEdges.add(new int[]{from, to});
        }
    }

    /** Records a constraint in the edge list (already applied to the matrix). */
    public void recordConstraint(DistanceEdge edge) {
        constraints.add(edge);
    }

    // --- propagation ---

    /**
     * Propagates constraints. If the matrix needs full propagation
     * (new time points or constraint removal), uses full Floyd-Warshall.
     * Otherwise, if edges were tightened via {@link #addEdgeSilent} since
     * the last propagation, uses incremental O(n² × |tightenedEdges|).
     *
     * @return true if the network is consistent (no negative cycles)
     */
    public boolean propagate() {
        if (tightenedEdges.isEmpty()) return true;
        return propagateIncremental();
    }

    /**
     * Incremental propagation: for each tightened edge (u→v, w),
     * updates all paths that can use it as a shortcut.
     *
     * <p>For each pair (i,j), the new shortest path is either:
     * <ul>
     *   <li>the existing distance[i][j], or</li>
     *   <li>distance[i][u] + w + distance[v][j] (going through the new edge)</li>
     * </ul>
     *
     * <p>Since edge weights only decrease, distance[i][u] and distance[v][j]
     * are already optimal (they cannot be improved by the new edge).
     * A single O(n²) pass per tightened edge is sufficient — no cascading
     * needed.
     */
    private boolean propagateIncremental() {
        int n = timePointCount;
        // Repeatedly (a) relax all pairs through each recently-tightened edge and
        // (b) close paths that cross the origin hub, until no further tightening
        // occurs. Edge weights only decrease, so this reaches the exact all-pairs
        // fixpoint. Guard against pathological cascades with a full-FW fallback.
        for (int round = 0; round < 4; round++) {
            boolean changed = false;
            for (int[] edge : tightenedEdges) {
                int u = edge[0];
                int v = edge[1];
                long w = distance[u][v];

                // Pre-check: negative self-loop via this edge
                if (u == v && w < 0) {
                    tightenedEdges.clear();
                    return false;
                }

                for (int i = 0; i < n; i++) {
                    long diu = distance[i][u];
                    if (diu == INF) continue;
                    long[] di = distance[i];
                    long newBase = diu + w;
                    for (int j = 0; j < n; j++) {
                        long dvj = distance[v][j];
                        if (dvj == INF) continue;
                        long newDist = newBase + dvj;
                        if (newDist < di[j]) {
                            di[j] = newDist;
                            changed = true;
                            if (i == j && newDist < 0) {
                                tightenedEdges.clear();
                                return false;
                            }
                        }
                    }
                }
            }
            if (closeThroughOrigin()) {
                changed = true;
            }
            for (int i = 0; i < n; i++) {
                if (distance[i][i] < 0) {
                    tightenedEdges.clear();
                    return false;
                }
            }
            if (!changed) {
                tightenedEdges.clear();
                return true;
            }
        }
        // Slow but exact fallback for cascading tightenings.
        tightenedEdges.clear();
        return propagateFull();
    }

    /**
     * Closes all paths that cross the origin: {@code d[i][j] <= d[i][0] + d[0][j]}.
     * Every time point connects to the origin with the horizon/monotonicity
     * bounds, so the origin is a hub that full Floyd-Warshall would relabel;
     * the incremental edge sweeps alone never re-visit those hub edges.
     * @return true if any entry was tightened
     */
    private boolean closeThroughOrigin() {
        int n = timePointCount;
        boolean changed = false;
        for (int i = 0; i < n; i++) {
            long di0 = distance[i][0];
            if (di0 == INF) continue;
            long[] di = distance[i];
            for (int j = 0; j < n; j++) {
                long d0j = distance[0][j];
                if (d0j == INF) continue;
                long viaOrigin = di0 + d0j;
                if (viaOrigin < di[j]) {
                    di[j] = viaOrigin;
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Runs full Floyd-Warshall all-pairs shortest paths.
     * Used by {@link #rebuild()} after constraint removal.
     * @return true if the network is consistent (no negative cycles)
     */
    private boolean propagateFull() {
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
        tightenedEdges.clear();
        return propagateFull();
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
