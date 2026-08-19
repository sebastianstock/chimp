package chimp.kernel.fluent;

import java.util.List;

import chimp.model.StateVariableDefinition;
import resourceFluent.FluentScheduler;

/**
 * Converts a {@link StateVariableDefinition} into the
 * {@link FluentScheduler}s that enforce mutual exclusion of its values.
 */
public final class StateVariableAdapter {

    public List<FluentScheduler> toFluentSchedulers(StateVariableDefinition definition) {
        return definition.exclusiveValues().stream()
                .map(value -> new FluentScheduler(
                        null,
                        null,
                        definition.predicate(),
                        definition.fieldIndex(),
                        value))
                .toList();
    }
}
