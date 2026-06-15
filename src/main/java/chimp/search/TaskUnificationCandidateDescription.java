package chimp.search;

public record TaskUnificationCandidateDescription(long matchedTaskId)
        implements ResolverCandidateDescription {
    public TaskUnificationCandidateDescription {
        if (matchedTaskId < 0) {
            throw new IllegalArgumentException("Matched task ID must not be negative");
        }
    }
}
