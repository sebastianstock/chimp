package chimp.kernel.temporal;

import chimp.kernel.Variable;
import chimp.model.TemporalBounds;

/**
 * CHIMP-native temporal variable — an interval with start and end time points
 * backed by a {@link SimpleTemporalNetwork}.
 *
 * <p>Port of {@code org.metacsp.multi.allenInterval.AllenInterval}. Provides
 * earliest/latest start/end times (EST/LST/EET/LET) queried from the STN.
 */
public class TemporalVariable extends Variable {

    private final int startTimePoint;
    private final int endTimePoint;
    private final SimpleTemporalNetwork stn;

    /**
     * Creates a temporal variable allocating two new time points in the STN.
     */
    public TemporalVariable(TemporalSolver solver, int id, SimpleTemporalNetwork stn) {
        super(solver, id);
        this.stn = stn;
        this.startTimePoint = stn.addTimePoint();
        this.endTimePoint = stn.addTimePoint();
        // Implicit Duration[0, INF]: start ≤ end (end - start ≥ 0)
        // Add edges directly without propagation; the constraint is always
        // consistent with origin bounds, so no need for Floyd-Warshall now.
        stn.addEdgeSilent(startTimePoint, endTimePoint,
                TemporalBounds.INFINITY);
        stn.addEdgeSilent(endTimePoint, startTimePoint, 0);
        // Record the implicit duration edge so STN rebuilds keep it.
        stn.recordConstraint(new chimp.kernel.temporal.DistanceEdge(
                startTimePoint, endTimePoint, 0, TemporalBounds.INFINITY));
    }

    /** Index of the start time point in the STN. */
    public int start() { return startTimePoint; }

    /** Index of the end time point in the STN. */
    public int end() { return endTimePoint; }

    /** Use {@link #start()} in new code. */
    public int getStart() { return startTimePoint; }

    /** Use {@link #end()} in new code. */
    public int getEnd() { return endTimePoint; }

    // -- temporal bounds (from STN) --

    /** Earliest possible start time. */
    public long getEST() { return stn.getMinDistance(0, startTimePoint); }

    /** Latest possible start time. */
    public long getLST() { return stn.getMaxDistance(0, startTimePoint); }

    /** Earliest possible end time. */
    public long getEET() { return stn.getMinDistance(0, endTimePoint); }

    /** Latest possible end time. */
    public long getLET() { return stn.getMaxDistance(0, endTimePoint); }

    /** Returns the admissible distance between two time points. */
    public TemporalBounds getBoundsBetween(int fromTp, int toTp) {
        return stn.getBounds(fromTp, toTp);
    }
}
