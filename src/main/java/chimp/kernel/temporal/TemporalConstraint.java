package chimp.kernel.temporal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.model.TemporalBounds;
import chimp.model.TemporalRelation;

/**
 * CHIMP-native temporal constraint — an Allen interval relation between two
 * {@link TemporalVariable}s.
 *
 * <p>Port of {@code org.metacsp.multi.allenInterval.AllenIntervalConstraint}.
 * Each constraint type compiles to one or more {@link DistanceEdge}s that are
 * added to the {@link SimpleTemporalNetwork} by {@link TemporalSolver}.
 */
public class TemporalConstraint extends Constraint {

    private final TemporalRelation type;
    private final TemporalBounds[] bounds;

    /**
     * Creates a constraint with the given relation type and custom bounds.
     * If bounds is empty, the default bounds for the type are used.
     */
    public TemporalConstraint(TemporalRelation type, TemporalBounds... bounds) {
        this.type = Objects.requireNonNull(type);
        this.bounds = (bounds != null && bounds.length > 0)
                ? bounds.clone()
                : defaultBounds(type);
    }

    public TemporalRelation type() { return type; }

    /** Use {@link #type()} in new code. */
    public TemporalRelation getType() { return type; }

    public TemporalBounds[] bounds() { return bounds.clone(); }

    // -- convenience setters (for Meta-CSP compatibility) --

    public void setFrom(TemporalVariable f) { setScope(new Variable[]{f, toVariable()}); }
    public void setTo(TemporalVariable t) { setScope(new Variable[]{fromVariable(), t}); }

    private TemporalVariable fromVariable() {
        return (TemporalVariable) (scope() != null && scope().length > 0 ? scope()[0] : null);
    }

    private TemporalVariable toVariable() {
        return (TemporalVariable) (scope() != null && scope().length > 1 ? scope()[1] : null);
    }

    // -- compilation to distance constraints (core of Allen algebra) --

    /**
     * Compiles this Allen relation into distance constraints between the
     * start/end time points of the two variables.
     *
     * <p>Full compilation for all ~35 types is in Step 4c. This stub handles
     * the most common types.
     */
    public List<DistanceEdge> compileToDistanceConstraints() {
        TemporalVariable f = fromVariable();
        TemporalVariable t = toVariable();
        if (f == null || t == null) return List.of();

        int fs = f.start(), fe = f.end();
        int ts = t.start(), te = t.end();

        List<DistanceEdge> result = new ArrayList<>();

        switch (type) {
            case Equals -> {
                result.add(new DistanceEdge(fs, ts, 0, 0));   // starts equal
                result.add(new DistanceEdge(te, fe, 0, 0));   // ends equal (fe >= te)
            }
            case Before -> {
                // from ends before to starts: bounds[0] is [min, max] on to_start - from_end
                result.add(new DistanceEdge(fe, ts, bnd(0).minimum(), bnd(0).maximum()));
            }
            case Meets -> {
                result.add(new DistanceEdge(fe, ts, 0, 0));   // from_end = to_start
            }
            case MetBy -> {
                result.add(new DistanceEdge(te, fs, 0, 0));   // to_end = from_start
            }
            case After -> {
                // from starts after to ends: bounds[0] on from_start - to_end
                result.add(new DistanceEdge(te, fs, bnd(0).minimum(), bnd(0).maximum()));
            }
            case Duration -> {
                // duration of from: fs → fe
                result.add(new DistanceEdge(fs, fe, bnd(0).minimum(), bnd(0).maximum()));
            }
            case Starts -> {
                result.add(new DistanceEdge(fs, ts, 0, 0));   // starts equal
                result.add(new DistanceEdge(ts, fe, bnd(0).minimum(), bnd(0).maximum()));  // from_end after to_start
            }
            case Finishes -> {
                result.add(new DistanceEdge(fe, te, 0, 0));   // ends equal
                result.add(new DistanceEdge(fs, ts, bnd(0).minimum(), bnd(0).maximum()));  // from_start after to_start
            }
            case During -> {
                result.add(new DistanceEdge(ts, fs, bnd(0).minimum(), bnd(0).maximum()));  // from_start after to_start
                result.add(new DistanceEdge(fe, te, bnd(1).minimum(), bnd(1).maximum()));  // to_end after from_end
            }
            case Contains -> {
                result.add(new DistanceEdge(fs, ts, bnd(0).minimum(), bnd(0).maximum()));  // to_start after from_start
                result.add(new DistanceEdge(te, fe, bnd(1).minimum(), bnd(1).maximum()));  // from_end after to_end
            }
            case StartedBy -> {
                result.add(new DistanceEdge(ts, fs, 0, 0));   // starts equal (to_start = from_start... wait, "A StartedBy B" → B starts A → t_start = f_start)
                result.add(new DistanceEdge(fs, te, bnd(0).minimum(), bnd(0).maximum()));  // to_end after from_start
            }
            case FinishedBy -> {
                result.add(new DistanceEdge(te, fe, 0, 0));   // ends equal (to_end = from_end... wait, "A FinishedBy B" → B finishes A → t_end = f_end)
                result.add(new DistanceEdge(fs, ts, bnd(0).minimum(), bnd(0).maximum()));  // from_start after to_start
            }
            case Overlaps -> {
                // from starts before to, from ends during to
                result.add(new DistanceEdge(fs, ts, bnd(0).minimum(), bnd(0).maximum()));  // to_start after from_start
                result.add(new DistanceEdge(ts, fe, bnd(1).minimum(), bnd(1).maximum()));  // from_end after to_start
                result.add(new DistanceEdge(fe, te, bnd(2).minimum(), bnd(2).maximum()));  // to_end after from_end
            }
            case OverlappedBy -> {
                result.add(new DistanceEdge(ts, fs, bnd(0).minimum(), bnd(0).maximum()));  // from_start after to_start
                result.add(new DistanceEdge(fs, te, bnd(1).minimum(), bnd(1).maximum()));  // to_end after from_start
                result.add(new DistanceEdge(te, fe, bnd(2).minimum(), bnd(2).maximum()));  // from_end after to_end
            }
            case Release -> {
                result.add(new DistanceEdge(0, fs, bnd(0).minimum(), bnd(0).maximum()));
            }
            case Deadline -> {
                result.add(new DistanceEdge(fs, 0, bnd(0).minimum(), bnd(0).maximum()));
            }
            default -> {
                // Composite types and remaining — fully handled in Step 4c
                // For now, fall through to the default bounds via the four-interval pattern
                compileComposite(fs, fe, ts, te, result);
            }
        }
        return result;
    }

    /** Stub for composite relations — uses the 4-bounds template from Meta-CSP. */
    private void compileComposite(int fs, int fe, int ts, int te,
                                   List<DistanceEdge> result) {
        // bnd(0): fs → ts
        if (!isUnconstrained(0)) result.add(new DistanceEdge(fs, ts,
                bnd(0).minimum(), bnd(0).maximum()));
        // bnd(1): fs → te
        if (!isUnconstrained(1)) result.add(new DistanceEdge(fs, te,
                bnd(1).minimum(), bnd(1).maximum()));
        // bnd(2): fe → ts
        if (!isUnconstrained(2)) result.add(new DistanceEdge(fe, ts,
                bnd(2).minimum(), bnd(2).maximum()));
        // bnd(3): fe → te
        if (!isUnconstrained(3)) result.add(new DistanceEdge(fe, te,
                bnd(3).minimum(), bnd(3).maximum()));
    }

    private boolean isUnconstrained(int i) {
        TemporalBounds b = bnd(i);
        return b.minimum() == -TemporalBounds.INFINITY
                && b.maximum() == TemporalBounds.INFINITY;
    }

    private TemporalBounds bnd(int i) {
        if (bounds != null && i < bounds.length) return bounds[i];
        return new TemporalBounds(-TemporalBounds.INFINITY, TemporalBounds.INFINITY);
    }

    // -- default bounds for common relation types --

    private static TemporalBounds[] defaultBounds(TemporalRelation type) {
        long INF = TemporalBounds.INFINITY;
        return switch (type) {
            case Before        -> bounds(1, INF);
            case After         -> bounds(1, INF);
            case Meets         -> bounds(0, 0);
            case MetBy         -> bounds(0, 0);
            case Overlaps      -> bounds(1, INF, 1, INF, 1, INF);
            case OverlappedBy  -> bounds(1, INF, 1, INF, 1, INF);
            case Starts        -> bounds(1, INF);
            case StartedBy     -> bounds(1, INF);
            case During        -> bounds(1, INF, 1, INF);
            case Contains      -> bounds(1, INF, 1, INF);
            case Finishes      -> bounds(1, INF);
            case FinishedBy    -> bounds(1, INF);
            case Equals        -> bounds(0, 0);
            case Duration      -> bounds(0, INF);
            case Release       -> bounds(0, INF);
            case Deadline      -> bounds(0, INF);
            case BeforeOrMeets -> bounds(0, INF);
            case MetByOrAfter  -> bounds(0, INF);
            default            -> bounds(0, INF, 0, INF, 0, INF, 0, INF); // 4-bounds default
        };
    }

    private static TemporalBounds[] bounds(long... pairs) {
        TemporalBounds[] ret = new TemporalBounds[pairs.length / 2];
        for (int i = 0; i < pairs.length; i += 2) {
            ret[i / 2] = new TemporalBounds(pairs[i], pairs[i + 1]);
        }
        return ret;
    }

    // -- Constraint contract --

    @Override
    public TemporalConstraint clone() {
        TemporalConstraint c = new TemporalConstraint(type, bounds);
        if (scope() != null) c.setScope(scope().clone());
        return c;
    }

    @Override
    public String getEdgeLabel() { return type.name(); }

    @Override
    public boolean isEquivalent(Constraint other) {
        if (!(other instanceof TemporalConstraint tc)) return false;
        return this.type == tc.type
                && Arrays.equals(this.bounds, tc.bounds);
    }

    @Override
    public String toString() {
        return "[" + type + " " + Arrays.toString(bounds) + "]";
    }
}
