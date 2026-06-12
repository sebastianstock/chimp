package chimp.model;

public record TemporalBounds(long minimum, long maximum) {

    public static final long INFINITY = Long.MAX_VALUE - 1;

    public TemporalBounds {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Minimum must not exceed maximum");
        }
    }
}
