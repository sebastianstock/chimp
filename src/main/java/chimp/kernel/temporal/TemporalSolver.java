package chimp.kernel.temporal;

import java.util.ArrayList;
import java.util.List;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;

/**
 * CHIMP-native temporal constraint solver.
 *
 * <p>Manages {@link TemporalVariable}s and {@link TemporalConstraint}s
 * over a shared {@link SimpleTemporalNetwork}. Delegates propagation
 * to the STN's Floyd-Warshall engine.
 *
 * <p>Port of
 * {@code org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver}.
 */
public class TemporalSolver extends ConstraintSolver {

    private final SimpleTemporalNetwork stn;
    private final long origin;
    private final long horizon;

    public TemporalSolver(long origin, long horizon, int maxTimePoints) {
        this.stn = new SimpleTemporalNetwork(horizon, maxTimePoints);
        this.origin = origin;
        this.horizon = horizon;
    }

    public TemporalSolver(long origin, long horizon) {
        this(origin, horizon, 1000);
    }

    public SimpleTemporalNetwork stn() { return stn; }

    public long origin() { return origin; }
    public long horizon() { return horizon; }

    /** Use {@link #origin()} in new code. */
    public long getOrigin() { return origin; }

    /** Use {@link #horizon()} in new code. */
    public long getHorizon() { return horizon; }

    // -- variable creation --

    public TemporalVariable[] createVariables(int num) {
        TemporalVariable[] ret = new TemporalVariable[num];
        for (int i = 0; i < num; i++) {
            ret[i] = new TemporalVariable(this, nextVariableId(), stn);
            registerVariable(ret[i]);
        }
        return ret;
    }

    // -- constraint management --

    @Override
    public boolean addConstraint(Constraint c) {
        if (c instanceof TemporalConstraint tc) {
            // Compile to distance constraints and add to STN
            List<DistanceEdge> edges = tc.compileToDistanceConstraints();
            // Record which edges we added first, for rollback
            List<DistanceEdge> added = new ArrayList<>();
            for (DistanceEdge edge : edges) {
                if (!stn.addConstraint(edge.fromTimePoint(), edge.toTimePoint(),
                        edge.min(), edge.max())) {
                    // Rollback: remove the edges we already added
                    for (DistanceEdge ae : added) {
                        stn.removeConstraint(ae.fromTimePoint(), ae.toTimePoint(),
                                ae.min(), ae.max());
                    }
                    return false;
                }
                added.add(edge);
            }
        }
        constraintList().add(c);
        return true; // propagation already done per-edge by STN
    }

    @Override
    public void removeConstraint(Constraint c) {
        if (c instanceof TemporalConstraint tc) {
            for (DistanceEdge edge : tc.compileToDistanceConstraints()) {
                stn.removeConstraint(edge.fromTimePoint(), edge.toTimePoint(),
                        edge.min(), edge.max());
            }
        }
        constraintList().remove(c);
    }

    @Override
    public boolean propagate() {
        return stn.propagate();
    }

    // -- bound queries --

    /**
     * Returns the admissible distance bounds between two time points.
     * Port of {@code AllenIntervalNetworkSolver.getAdmissibleDistanceBounds}.
     */
    public chimp.model.TemporalBounds getAdmissibleDistanceBounds(
            int fromTimePoint, int toTimePoint) {
        return stn.getBounds(fromTimePoint, toTimePoint);
    }
}
