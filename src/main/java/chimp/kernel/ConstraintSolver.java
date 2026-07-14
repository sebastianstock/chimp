package chimp.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CHIMP-native constraint solver base class.
 *
 * <p>A solver owns a set of {@link Variable}s and {@link Constraint}s
 * and provides basic operations to add/remove constraints and access
 * variables by id.
 *
 * <p>Subclasses implement {@link #propagate()} and variable creation.
 * This base class provides the shared bookkeeping that {@link
 * ConstraintNetwork} and the search engine rely on.
 */
public abstract class ConstraintSolver {

    /** Next variable id. */
    protected int nextId;

    private final Map<Integer, Variable> variablesById = new HashMap<>();
    private final List<Constraint> constraints = new ArrayList<>();

    // -- variable management --

    protected int nextVariableId() { return ++nextId; }

    protected void registerVariable(Variable v) {
        variablesById.put(v.id(), v);
    }

    protected void unregisterVariable(Variable v) {
        variablesById.remove(v.id());
    }

    /** All variables currently in this solver. */
    public Variable[] getVariables() {
        return variablesById.values().toArray(new Variable[0]);
    }

    /** Look up a variable by id. */
    public Variable getVariable(int id) {
        return variablesById.get(id);
    }

    // -- constraint management --

    /** Add a single constraint. Returns {@code true} on success. */
    public boolean addConstraint(Constraint c) {
        constraints.add(c);
        return propagate();
    }

    /** Add multiple constraints. Returns {@code true} on success. */
    public boolean addConstraints(Constraint[] cs) {
        Collections.addAll(constraints, cs);
        return propagate();
    }

    /** Remove a single constraint. */
    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    /** Remove multiple constraints. */
    public void removeConstraints(Constraint[] cs) {
        for (Constraint c : cs) {
            constraints.remove(c);
        }
    }

    /** All constraints currently in this solver. */
    public Constraint[] getConstraints() {
        return constraints.toArray(new Constraint[0]);
    }

    // -- propagation --

    /**
     * Propagate constraints and return {@code true} if the current
     * state is consistent, {@code false} if a conflict is detected.
     */
    public abstract boolean propagate();
}
