package chimp.kernel.fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.kernel.integer.IntegerDomain;
import chimp.kernel.integer.IntegerVariable;
import chimp.kernel.temporal.TemporalSolver;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentMarking;
import chimp.model.FluentNode;
import chimp.model.FluentStore;
import chimp.model.FluentType;
import chimp.model.FluentView;
import chimp.model.IntegerValue;
import chimp.model.TemporalBounds;
import chimp.model.TemporalEndpoint;
import chimp.model.TemporalInterval;

/**
 * CHIMP-native implementation of {@link FluentStore}.
 *
 * <p>Backed by a {@link FluentNetworkSolver} and provides both read and
 * write operations for fluent variables, their temporal intervals, and
 * their constraints.
 */
public final class FluentStoreImpl implements FluentStore {

    private final FluentNetworkSolver solver;
    private final ModelAdapter modelAdapter = new ModelAdapter();

    public FluentStoreImpl(FluentNetworkSolver solver) {
        this.solver = Objects.requireNonNull(solver, "solver");
    }

    @Override
    public Optional<TemporalBounds> temporalDistance(FluentId from, FluentId to,
                                                     TemporalEndpoint fromEndpoint,
                                                     TemporalEndpoint toEndpoint) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(fromEndpoint, "fromEndpoint");
        Objects.requireNonNull(toEndpoint, "toEndpoint");

        Fluent fromFluent = requireFluent(from);
        Fluent toFluent = requireFluent(to);

        TemporalSolver temporalSolver = solver.getTemporalSolver();
        int fromPoint = timePoint(fromFluent, fromEndpoint);
        int toPoint = timePoint(toFluent, toEndpoint);
        return Optional.of(temporalSolver.getAdmissibleDistanceBounds(fromPoint, toPoint));
    }

    @Override
    public List<FluentView> fluents() {
        List<FluentView> result = new ArrayList<>();
        for (Variable variable : solver.getVariables()) {
            if (variable instanceof Fluent fluent) {
                result.add(toView(fluent));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public Optional<FluentView> fluent(FluentId id) {
        Objects.requireNonNull(id, "id");
        for (Variable variable : solver.getVariables()) {
            if (variable instanceof Fluent fluent && fluent.getID() == id.value()) {
                return Optional.of(toView(fluent));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FluentView> fluents(FluentType type) {
        Objects.requireNonNull(type, "type");
        return fluents().stream().filter(fluent -> fluent.type() == type).toList();
    }

    @Override
    public List<FluentView> fluents(FluentMarking marking) {
        Objects.requireNonNull(marking, "marking");
        return fluents().stream().filter(fluent -> fluent.marking() == marking).toList();
    }

    @Override
    public List<ChimpConstraint> incoming(FluentId id) {
        return constraints(id, null, false);
    }

    @Override
    public List<ChimpConstraint> incoming(FluentId id, ConstraintKind kind) {
        return constraints(id, Objects.requireNonNull(kind, "kind"), false);
    }

    @Override
    public List<ChimpConstraint> outgoing(FluentId id) {
        return constraints(id, null, true);
    }

    @Override
    public List<ChimpConstraint> outgoing(FluentId id, ConstraintKind kind) {
        return constraints(id, Objects.requireNonNull(kind, "kind"), true);
    }

    // --- Write operations ---

    @Override
    public FluentId createFluent(String component, String name, String[] arguments, FluentMarking marking) {
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(arguments, "arguments");
        Objects.requireNonNull(marking, "marking");
        Fluent fluent = (Fluent) solver.createVariable(component);
        fluent.setName(name, arguments);
        fluent.setMarking(marking.ordinal());
        return new FluentId(fluent.getID());
    }

    @Override
    public void removeFluents(List<FluentId> ids) {
        Objects.requireNonNull(ids, "ids");
        Variable[] toRemove = new Variable[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            toRemove[i] = requireFluent(ids.get(i));
        }
        solver.removeVariables(toRemove);
    }

    @Override
    public void setMarking(FluentId id, FluentMarking marking) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(marking, "marking");
        Fluent fluent = requireFluent(id);
        fluent.setMarking(marking.ordinal());
    }

    // --- helpers ---

    private FluentView toView(Fluent fluent) {
        FluentNode node = modelAdapter.toFluentNode(fluent);
        List<IntegerValue> integerValues = new ArrayList<>();
        for (IntegerVariable variable : fluent.getIntegerVariables()) {
            IntegerDomain domain = variable.getDomain();
            OptionalInt value = domain.isInstantiated()
                    ? OptionalInt.of(domain.getValue())
                    : OptionalInt.empty();
            integerValues.add(new IntegerValue(value, domain.isConstant()));
        }
        return new FluentViewRecord(
                node.id(),
                node.type(),
                node.signature(),
                FluentMarking.fromIntValue(fluent.getMarking()),
                node.temporalInterval(),
                List.copyOf(integerValues),
                fluent.toString());
    }

    private List<ChimpConstraint> constraints(FluentId id, ConstraintKind kind, boolean outgoing) {
        Objects.requireNonNull(id, "id");
        if (fluent(id).isEmpty()) {
            throw new IllegalArgumentException("Unknown fluent ID: " + id.value());
        }

        List<ChimpConstraint> result = new ArrayList<>();
        for (Constraint constraint : solver.getConstraints()) {
            if (constraint instanceof FluentConstraint fluentConstraint) {
                ChimpConstraint converted = modelAdapter.toChimpConstraint(fluentConstraint);
                FluentId endpoint = outgoing ? converted.from() : converted.to();
                if (endpoint.equals(id) && (kind == null || converted.kind() == kind)) {
                    result.add(converted);
                }
            }
        }
        return List.copyOf(result);
    }

    private Fluent requireFluent(FluentId id) {
        for (Variable variable : solver.getVariables()) {
            if (variable instanceof Fluent fluent && fluent.getID() == id.value()) {
                return fluent;
            }
        }
        throw new IllegalArgumentException("Unknown fluent ID: " + id.value());
    }

    private int timePoint(Fluent fluent, TemporalEndpoint endpoint) {
        TemporalVariable interval = fluent.getTemporalVariable();
        return endpoint == TemporalEndpoint.START ? interval.getStart() : interval.getEnd();
    }

    private record FluentViewRecord(
            FluentId id,
            FluentType type,
            chimp.model.FluentSignature signature,
            FluentMarking marking,
            TemporalInterval temporalInterval,
            List<IntegerValue> integerValues,
            String displayValue) implements FluentView {

        @Override
        public String toString() {
            return displayValue;
        }
    }
}
