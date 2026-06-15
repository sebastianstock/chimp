package chimp.search;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

public record ResolverCandidateMetadata(
        ResolverCandidateKind kind,
        long taskId,
        OptionalInt repositoryItemId,
        OptionalLong matchedTaskId,
        int depth,
        OptionalInt weight,
        int subtaskCount,
        long taskEst,
        long selectedPreconditionEstSum,
        int bindingCount) {
    public ResolverCandidateMetadata {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(repositoryItemId, "repositoryItemId");
        Objects.requireNonNull(matchedTaskId, "matchedTaskId");
        Objects.requireNonNull(weight, "weight");
        if (taskId < 0 || depth < 0 || subtaskCount < 0 || bindingCount < 0) {
            throw new IllegalArgumentException("IDs and counts must not be negative");
        }
        if (kind == ResolverCandidateKind.EXPANSION && repositoryItemId.isEmpty()) {
            throw new IllegalArgumentException("Expansion candidates require a repository item ID");
        }
        if (kind == ResolverCandidateKind.TASK_UNIFICATION && matchedTaskId.isEmpty()) {
            throw new IllegalArgumentException("Task-unification candidates require a matched task ID");
        }
    }
}
