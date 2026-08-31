package chimp.search;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A node in the CHIMP search tree, replacing Meta-CSP {@code MetaVariable}
 * for JUNG visualization.
 *
 * <p>Each node has a unique id and an optional label (typically the
 * resolver description). Terminal nodes (SUCCESS/FAILURE) use the
 * predefined {@link #SUCCESS} and {@link #FAILURE} constants.
 */
public class SearchTreeNode {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    /** Predefined terminal node for successful branches. */
    public static final SearchTreeNode SUCCESS = new SearchTreeNode("SUCCESS", true);

    /** Predefined terminal node for failed branches. */
    public static final SearchTreeNode FAILURE = new SearchTreeNode("FAILURE", false);

    private final int id;
    private final String label;
    private final boolean terminal;

    /** Creates a search tree node with an auto-generated id. */
    public SearchTreeNode(String label) {
        this(label, false);
    }

    /** Creates a search tree node. For internal use only. */
    protected SearchTreeNode(String label, boolean terminal) {
        this.id = NEXT_ID.getAndIncrement();
        this.label = label;
        this.terminal = terminal;
    }

    public int id() { return id; }
    public String label() { return label; }
    public boolean isTerminal() { return terminal; }

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
