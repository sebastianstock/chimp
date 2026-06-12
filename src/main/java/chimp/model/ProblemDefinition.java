package chimp.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProblemDefinition {
    private final List<String> argumentSymbols;
    private final Map<String, List<String>> typeInstances;
    private final List<InitialFluentDefinition> initialFluents;
    private final List<InitialTaskDefinition> initialTasks;
    private final List<ProblemTemporalConstraint> temporalConstraints;
    private final List<ProblemOrderingEdge> orderingEdges;

    public ProblemDefinition(
            List<String> argumentSymbols,
            Map<String, List<String>> typeInstances,
            List<InitialFluentDefinition> initialFluents,
            List<InitialTaskDefinition> initialTasks,
            List<ProblemTemporalConstraint> temporalConstraints,
            List<ProblemOrderingEdge> orderingEdges) {
        this.argumentSymbols = List.copyOf(Objects.requireNonNull(argumentSymbols, "argumentSymbols"));
        Map<String, List<String>> copiedTypes = new LinkedHashMap<>();
        Objects.requireNonNull(typeInstances, "typeInstances").forEach((type, instances) -> {
            Objects.requireNonNull(type, "type");
            copiedTypes.put(type, List.copyOf(instances));
        });
        this.typeInstances = Collections.unmodifiableMap(copiedTypes);
        this.initialFluents = List.copyOf(Objects.requireNonNull(initialFluents, "initialFluents"));
        this.initialTasks = List.copyOf(Objects.requireNonNull(initialTasks, "initialTasks"));
        this.temporalConstraints = List.copyOf(
                Objects.requireNonNull(temporalConstraints, "temporalConstraints"));
        this.orderingEdges = List.copyOf(Objects.requireNonNull(orderingEdges, "orderingEdges"));
    }

    public List<String> argumentSymbols() { return argumentSymbols; }
    public Map<String, List<String>> typeInstances() { return typeInstances; }
    public List<InitialFluentDefinition> initialFluents() { return initialFluents; }
    public List<InitialTaskDefinition> initialTasks() { return initialTasks; }
    public List<ProblemTemporalConstraint> temporalConstraints() { return temporalConstraints; }
    public List<ProblemOrderingEdge> orderingEdges() { return orderingEdges; }
}
