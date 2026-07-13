package chimp.adapter.metacsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;

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
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import integers.IntegerDomain;
import integers.IntegerVariable;
import org.metacsp.framework.Variable;

public final class MetaCspFluentStore implements FluentStore {

    private final FluentNetworkSolver solver;
    private final MetaCspModelAdapter modelAdapter = new MetaCspModelAdapter();

    public MetaCspFluentStore(FluentNetworkSolver solver) {
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

        TimePoint fromTimePoint = timePoint(fromFluent, fromEndpoint);
        TimePoint toTimePoint = timePoint(toFluent, toEndpoint);

        SimpleDistanceConstraint distance = apspSolver().getConstraint(fromTimePoint, toTimePoint);
        if (distance == null) {
            return Optional.empty();
        }
        return Optional.of(new TemporalBounds(distance.getMinimum(), distance.getMaximum()));
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
        fluent.setMarking(fromMarking(marking));
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
        fluent.setMarking(fromMarking(marking));
    }

    private HTNMetaConstraint.markings fromMarking(FluentMarking marking) {
        return HTNMetaConstraint.markings.valueOf(marking.name());
    }

    private FluentView toView(Fluent fluent) {
        FluentNode node = modelAdapter.toFluentNode(fluent);
        List<IntegerValue> integerValues = new ArrayList<>();
        for (IntegerVariable variable : fluent.getIntegerVariables()) {
            IntegerDomain domain = (IntegerDomain) variable.getDomain();
            OptionalInt value = domain.isInstantiated()
                    ? OptionalInt.of(domain.getValue())
                    : OptionalInt.empty();
            integerValues.add(new IntegerValue(value, domain.isConstant()));
        }
        return new MetaCspFluentView(
                node.id(),
                node.type(),
                node.signature(),
                toMarking(fluent.getMarking()),
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

    private TimePoint timePoint(Fluent fluent, TemporalEndpoint endpoint) {
        AllenInterval interval = fluent.getAllenInterval();
        return endpoint == TemporalEndpoint.START ? interval.getStart() : interval.getEnd();
    }

    private APSPSolver apspSolver() {
        AllenIntervalNetworkSolver allenSolver = (AllenIntervalNetworkSolver) solver.getConstraintSolvers()[1];
        return (APSPSolver) allenSolver.getConstraintSolvers()[0];
    }

    private FluentMarking toMarking(Object marking) {
        if (!(marking instanceof HTNMetaConstraint.markings metaCspMarking)) {
            throw new IllegalArgumentException("Unsupported fluent marking: " + marking);
        }
        return FluentMarking.valueOf(metaCspMarking.name());
    }

    private record MetaCspFluentView(
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
