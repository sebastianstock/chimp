package chimp.search;

import java.util.List;
import java.util.Objects;

public final class TaskPlanningFlaw implements PlanningFlaw {
    private final List<Long> eligibleTaskIds;

    public TaskPlanningFlaw(List<Long> eligibleTaskIds) {
        Objects.requireNonNull(eligibleTaskIds, "eligibleTaskIds");
        if (eligibleTaskIds.isEmpty() || eligibleTaskIds.stream().anyMatch(id -> id < 0)) {
            throw new IllegalArgumentException("Eligible task IDs must be non-empty and non-negative");
        }
        this.eligibleTaskIds = List.copyOf(eligibleTaskIds);
    }

    @Override
    public List<Long> eligibleTaskIds() {
        return eligibleTaskIds;
    }
}
