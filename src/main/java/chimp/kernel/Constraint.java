package chimp.kernel;

import java.util.List;

/**
 * CHIMP-native constraint base type.
 *
 * <p>A constraint relates a {@link #scope()} of {@link Variable}s.
 * Binary constraints additionally expose {@link #from()} / {@link #to()}
 * convenience accessors.  Constraints must be cloneable and support
 * equivalence checks against other constraints.
 */
public abstract class Constraint {

    private Variable[] scope;

    protected Constraint() { }

    // -- scope --

    public Variable[] scope() { return scope; }

    /** Use {@link #scope()} in new code; kept for Meta-CSP compatibility. */
    public Variable[] getScope() { return scope; }

    public void setScope(Variable[] scope) { this.scope = scope; }

    public Variable from() {
        return scope != null && scope.length > 0 ? scope[0] : null;
    }

    public Variable to() {
        return scope != null && scope.length > 1 ? scope[1] : null;
    }

    /** Use {@link #from()} in new code; kept for Meta-CSP compatibility. */
    public Variable getFrom() { return from(); }

    /** Use {@link #to()} in new code; kept for Meta-CSP compatibility. */
    public Variable getTo() { return to(); }

    // -- abstract contract --

    /** Deep copy of this constraint (for scope repair). */
    public abstract Constraint clone();

    /** True if the two constraints have equivalent semantics. */
    public abstract boolean isEquivalent(Constraint other);

    /** Human-readable label for graph edges. */
    public abstract String getEdgeLabel();
}
