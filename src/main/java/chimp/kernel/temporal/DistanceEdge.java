package chimp.kernel.temporal;

import chimp.model.TemporalBounds;

/**
 * A distance constraint between two time points in a
 * {@link SimpleTemporalNetwork}.
 *
 * <p>Encodes: {@code t_toTimePoint - t_fromTimePoint ∈ [min, max]}.
 */
public record DistanceEdge(int fromTimePoint, int toTimePoint,
                            long min, long max) {
    public DistanceEdge {
        if (min > max) {
            throw new IllegalArgumentException(
                    "min=" + min + " > max=" + max);
        }
    }

    public TemporalBounds toBounds() {
        return new TemporalBounds(min, max);
    }
}
