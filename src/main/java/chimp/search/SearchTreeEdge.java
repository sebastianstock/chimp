package chimp.search;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An edge in the CHIMP search tree, replacing Meta-CSP
 * {@code ConstraintNetwork} as the edge type for JUNG visualization.
 *
 * <p>Each edge carries a label describing the resolver that was applied
 * (or "FAILURE" for failed branches). Uses reference equality to avoid
 * JUNG duplicate-edge issues.
 */
public final class SearchTreeEdge {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final String label;

    public SearchTreeEdge(String label) {
        this.id = NEXT_ID.getAndIncrement();
        this.label = label;
    }

    public int id() { return id; }
    public String label() { return label; }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
