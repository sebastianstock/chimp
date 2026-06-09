package chimp.model;

public record TemporalBounds(long minimum, long maximum) {

    public TemporalBounds {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Minimum must not exceed maximum");
        }
    }
}
