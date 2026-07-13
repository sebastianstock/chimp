package chimp.model;

/**
 * Identifies which sub-variable of a {@code Fluent} a constraint operates on.
 * Used during resolver application to extract the correct variable type from
 * materialized fluents when repairing constraint scopes.
 */
public enum SubVariableKind {
    /** The Fluent itself (used by FluentConstraint). */
    FLUENT,
    /** The compound symbolic sub-variable (used by CompoundSymbolicValueConstraint). */
    SYMBOLIC,
    /** The AllenInterval temporal sub-variable (used by AllenIntervalConstraint). */
    TEMPORAL,
    /** An IntegerVariable (used by IntegerConstraint). */
    INTEGER
}
