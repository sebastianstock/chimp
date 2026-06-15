package chimp.search;

import java.util.Objects;

public record ResolverCandidate(
        ResolverCandidateMetadata metadata,
        ResolverCandidateDescription description) {
    public ResolverCandidate {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(description, "description");
        if (metadata.kind() == ResolverCandidateKind.EXPANSION
                && !(description instanceof ExpansionCandidateDescription)) {
            throw new IllegalArgumentException("Expansion metadata requires an expansion description");
        }
        if (metadata.kind() == ResolverCandidateKind.TASK_UNIFICATION
                && !(description instanceof TaskUnificationCandidateDescription)) {
            throw new IllegalArgumentException("Task-unification metadata requires a unification description");
        }
    }
}
