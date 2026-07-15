package chimp.model;

public enum FluentMarking {
    UNPLANNED,
    SELECTED,
    PLANNED,
    OPEN,
    CLOSED,
    UNJUSTIFIED,
    JUSTIFIED,
    UNIFIED;

    /**
     * Returns the FluentMarking for the given ordinal (matching
     * {@code HTNMetaConstraint.markings} ordinals).
     */
    public static FluentMarking fromIntValue(int ordinal) {
        return values()[ordinal];
    }
}
