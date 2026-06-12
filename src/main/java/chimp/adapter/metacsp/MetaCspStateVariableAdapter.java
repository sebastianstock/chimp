package chimp.adapter.metacsp;

import chimp.model.StateVariableDefinition;
import resourceFluent.FluentScheduler;

import java.util.List;

public final class MetaCspStateVariableAdapter {

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
