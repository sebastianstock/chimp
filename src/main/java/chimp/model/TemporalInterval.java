package chimp.model;

public record TemporalInterval(long earliestStart, long latestStart, long earliestEnd, long latestEnd) {

    public TemporalInterval {
        if (earliestStart > latestStart) {
            throw new IllegalArgumentException("Earliest start must not exceed latest start");
        }
        if (earliestEnd > latestEnd) {
            throw new IllegalArgumentException("Earliest end must not exceed latest end");
        }
        if (earliestStart > earliestEnd) {
            throw new IllegalArgumentException("Earliest end must not precede earliest start");
        }
        if (latestStart > latestEnd) {
            throw new IllegalArgumentException("Latest end must not precede latest start");
        }
    }
}
