package htn;

/**
 * Marker interface for meta-constraints that hold a direct reference to
 * the ground solver, set by {@code HTNPlanner.addMetaConstraint}.
 *
 * <p>This replaces the framework's {@code MetaConstraint.setMetaSolver}
 * mechanism, which required {@code HTNPlanner} to extend
 * {@code MetaConstraintSolver}. With this interface,
 * {@code HTNPlanner} sets the ground solver directly without needing
 * the Meta-CSP inheritance.
 */
public interface GroundSolverHolder {

    /**
     * Sets the ground solver reference used by this meta-constraint.
     *
     * @param groundSolver the fluent network solver
     */
    void setGroundSolver(fluentSolver.FluentNetworkSolver groundSolver);
}
