package chimp.search;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.Variable;
import chimp.kernel.VariablePrototype;

/**
 * CHIMP-owned resolver representation for the backtracking search engine.
 *
 * <p>A resolver carries constraints to apply to ground solvers, variable
 * prototypes for materialization, and a substitution map for scope repair.
 * This class uses the CHIMP-native {@link ConstraintNetwork} delegate.
 *
 * <p>The public API is intentionally narrow — it exposes only what
 * {@link SearchHooks} and {@link ChimpSearchEngine} need. Internal
 * mutation methods ({@link #addConstraint}, {@link #removeConstraint},
 * {@link #addSubstitution}) are package-visible for {@link SearchHooks}
 * implementations.
 */
public class ChimpResolver {

    /** Internal delegate backed by CHIMP-native types. */
    public final ConstraintNetwork delegate;

    public ChimpResolver(ConstraintNetwork delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
        this.delegate = delegate;
    }

    // --- Read operations (used by ChimpSearchEngine and SearchHooks) ---

    public Variable[] getVariables() {
        return delegate.getVariables();
    }

    public Constraint[] getConstraints() {
        return delegate.getConstraints();
    }

    public Object getAnnotation() {
        return delegate.getAnnotation();
    }

    public Variable getSubstitution(VariablePrototype prototype) {
        return delegate.getSubstitution(prototype);
    }

    // --- Mutation operations (used by SearchHooks only) ---

    /** Used by HTNPlanner for prototype → real tracking. */
    public void addSubstitution(VariablePrototype prototype, Variable real) {
        delegate.addSubstitution(prototype, real);
    }

    /** Used by HTNPlanner for scope repair. */
    public void addConstraint(Constraint constraint) {
        delegate.addConstraint(constraint);
    }

    /** Used by HTNPlanner for scope repair. */
    public void removeConstraint(Constraint constraint) {
        delegate.removeConstraint(constraint);
    }

    // --- Object methods ---

    @Override
    public String toString() {
        return "ChimpResolver[" + delegate + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChimpResolver other)) return false;
        return delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
