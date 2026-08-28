package chimp.kernel;

import java.util.Objects;

/**
 * CHIMP-native variable base type.
 *
 * <p>Every variable has a unique {@code id}, belongs to a
 * {@link ConstraintSolver}, and carries a {@code component} name that
 * groups variables by type (e.g. {@code "Activity"}, {@code "Task"},
 * {@code "State"}).  A {@code marking} tracks the variable's
 * search-state (OPEN, CLOSED, UNIFIED, etc.).
 *
 * <p>This type uses reference equality (inherited from {@code Object})
 * so that two distinct variables are never equal, even if they have
 * the same id.
 */
public class Variable {

    private final int id;
    private ConstraintSolver solver;
    private String component;
    private int marking;

    /**
     * Creates a variable with the given solver and id.
     * The solver may be {@code null} for variables that are not yet
     * associated with a ground solver (e.g. edge labels).
     */
    public Variable(ConstraintSolver solver, int id) {
        this.solver = solver;
        this.id = id;
    }

    // -- basic accessors --

    public int id() { return id; }

    /** Use {@link #id()} in new code; this is kept for Meta-CSP compatibility. */
    public int getID() { return id; }

    public ConstraintSolver solver() { return solver; }

    /** Use {@link #solver()} in new code; this is kept for Meta-CSP compatibility. */
    public ConstraintSolver getConstraintSolver() { return solver; }

    void setSolver(ConstraintSolver solver) { this.solver = solver; }

    public String component() { return component; }

    public void setComponent(String component) { this.component = component; }

    /** Use {@link #component()} in new code; kept for Meta-CSP compatibility. */
    public String getComponent() { return component; }

    // -- marking --

    public int marking() { return marking; }

    public void setMarking(int marking) { this.marking = marking; }

    /** Use {@link #marking()} in new code; kept for Meta-CSP compatibility. */
    public int getMarking() { return marking; }

    // -- standard --

    /**
     * Uses reference equality. Consistent with {@link ConstraintNetwork}'s
     * reference-equality policy (M15 decision 5).
     */
    @Override
    public final boolean equals(Object obj) { return this == obj; }

    @Override
    public final int hashCode() { return id; }

    @Override
    public String toString() {
        return "(" + id + ")";
    }
}
