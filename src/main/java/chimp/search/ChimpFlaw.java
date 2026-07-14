package chimp.search;

import java.util.List;
import java.util.Objects;

import chimp.model.FluentId;

/**
 * A CHIMP-owned flaw representation for the search engine.
 *
 * <p>A flaw is an open sub-problem — typically one or more task fluents
 * that need a resolver. The flaw carries the IDs of the task fluents
 * (not Meta-CSP {@code Variable} references), making it independent
 * of the Meta-CSP storage layer.
 *
 * <p>This is the CHIMP-owned replacement for
 * {@code ConstraintNetwork} in the flaw role within
 * {@link FlawProvider#currentFlaw()}. Meta-constraints produce flaws
 * by collecting task fluent IDs; the search engine selects a flaw and
 * asks the provider for resolvers.
 */
public final class ChimpFlaw {

    private final List<FluentId> taskIds;

    public ChimpFlaw(List<FluentId> taskIds) {
        Objects.requireNonNull(taskIds, "taskIds");
        if (taskIds.isEmpty()) {
            throw new IllegalArgumentException("Flaw must have at least one task ID");
        }
        for (FluentId id : taskIds) {
            Objects.requireNonNull(id, "taskId");
        }
        this.taskIds = List.copyOf(taskIds);
    }

    /**
     * Returns the IDs of the task fluents that compose this flaw.
     */
    public List<FluentId> taskIds() {
        return taskIds;
    }

    @Override
    public String toString() {
        return "ChimpFlaw" + taskIds;
    }
}
