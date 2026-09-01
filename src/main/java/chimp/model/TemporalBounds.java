package chimp.model;

import java.util.Arrays;

/**
 * Immutable min/max value pair. CHIMP-owned replacement for
 * {@code org.metacsp.time.Bounds}. Unlike the Meta-CSP {@code Bounds} class
 * (which has public mutable fields), this is an immutable record.
 */
public record TemporalBounds(long minimum, long maximum) implements Comparable<TemporalBounds> {

    public static final long INFINITY = Long.MAX_VALUE - 1;

    public TemporalBounds {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Minimum must not exceed maximum");
        }
    }

    /**
     * Returns {@code true} if this bounds is a single point.
     */
    public boolean isSingleton() {
        return minimum == maximum;
    }

    /**
     * Intersection that treats singletons {@code [n,n]} as empty.
     * Matches {@code Bounds.intersectStrict} semantics — necessary for
     * scheduling where "a meets b" should not create a conflict.
     *
     * @return the intersection, or {@code null} if empty
     */
    public TemporalBounds intersectStrict(TemporalBounds other) {
        long min = Math.max(this.minimum, other.minimum);
        long max = Math.min(this.maximum, other.maximum);
        if (min < max) return new TemporalBounds(min, max);
        return null;
    }

    /**
     * Returns {@code true} iff this bounds intersects with the given bounds.
     */
    public boolean isIntersecting(TemporalBounds other) {
        return intersectStrict(other) != null;
    }

    /**
     * Intersection that treats singletons as non-empty.
     *
     * @return the intersection, or {@code null} if empty
     */
    public TemporalBounds intersect(TemporalBounds other) {
        long min = Math.max(this.minimum, other.minimum);
        long max = Math.min(this.maximum, other.maximum);
        if (min <= max) return new TemporalBounds(min, max);
        return null;
    }

    /**
     * Union of multiple bounds.
     */
    public static TemporalBounds union(TemporalBounds... bounds) {
        long min = INFINITY;
        long max = Long.MIN_VALUE + 1;
        for (TemporalBounds b : bounds) {
            min = Math.min(min, b.minimum);
            max = Math.max(max, b.maximum);
        }
        if (min <= max) return new TemporalBounds(min, max);
        return null;
    }

    @Override
    public int compareTo(TemporalBounds other) {
        return 2 * Long.signum(other.minimum - this.minimum)
                + Long.signum(other.maximum - this.maximum);
    }
}
