package chimp.model;

import java.util.List;
import java.util.Optional;

public interface FluentStore {

    // --- Read operations ---

    Optional<TemporalBounds> temporalDistance(FluentId from, FluentId to,
                                               TemporalEndpoint fromEndpoint,
                                               TemporalEndpoint toEndpoint);

    List<FluentView> fluents();

    Optional<FluentView> fluent(FluentId id);

    List<FluentView> fluents(FluentType type);

    List<FluentView> fluents(FluentMarking marking);

    List<ChimpConstraint> incoming(FluentId id);

    List<ChimpConstraint> incoming(FluentId id, ConstraintKind kind);

    List<ChimpConstraint> outgoing(FluentId id);

    List<ChimpConstraint> outgoing(FluentId id, ConstraintKind kind);

    // --- Write operations ---

    /**
     * Creates a new fluent in the store with the given component, name,
     * arguments, and marking. Returns the ID of the created fluent.
     *
     * @param component  the fluent component (e.g., "Activity", "Task")
     * @param name       the predicate name (e.g., "!move_base")
     * @param arguments  the symbolic arguments (padded to maxArgs)
     * @param marking    the initial marking
     * @return the ID of the created fluent
     */
    FluentId createFluent(String component, String name, String[] arguments, FluentMarking marking);

    /**
     * Removes the given fluents from the store.
     *
     * @param ids the IDs of the fluents to remove
     */
    void removeFluents(List<FluentId> ids);

    /**
     * Sets the marking of a fluent.
     *
     * @param id      the fluent ID
     * @param marking the new marking
     */
    void setMarking(FluentId id, FluentMarking marking);
}
