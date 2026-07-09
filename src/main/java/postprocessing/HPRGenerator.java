package postprocessing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;

import fluentSolver.FluentNetworkSolver;
import htn.PlanReportroryItem;

import planner.CHIMP;
import postprocessing.HPRtypes.KeyValue;
import postprocessing.HPRtypes.*;

import chimp.adapter.metacsp.MetaCspFluentStore;
import chimp.model.ChimpConstraint;
import chimp.model.ConstraintKind;
import chimp.model.FluentId;
import chimp.model.FluentStore;
import chimp.model.FluentType;
import chimp.model.FluentView;
import chimp.model.TemporalBounds;
import chimp.model.TemporalEndpoint;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

// HPR = Hierarchichal Plan Representation
public class HPRGenerator {

    private CHIMP chimp;
    private Writer writer;
    private FluentStore store;
    private HPRPlan hprPlan;
    private List<FluentView> plannedFluents;
    private ListMultimap<FluentId, ChimpConstraint> fluentsConstraintsMultiMap;
    private Table<FluentId, FluentId, Boolean> beforesTable;

    public HPRGenerator(CHIMP chimp, Writer writer) {
        this.chimp = chimp;
        this.writer = writer;
        FluentNetworkSolver fluentSolver = chimp.getFluentSolver();
        this.store = new MetaCspFluentStore(fluentSolver);
        this.hprPlan = new HPRPlan();
        this.fluentsConstraintsMultiMap = ArrayListMultimap.create();
        this.beforesTable = HashBasedTable.create();
        createFluentsConstraintsMultiMap();
        this.plannedFluents = collectPlannedFluents();
    }

    public void generateHPR() {
        // Create and sort array of timepoints for each action
        List<FluentView> sortedFluents = createSortedPlannedFluents();

        addNodesToPlan(sortedFluents);
        addTemporalEdgesToPlan(sortedFluents);
        addCausalEdgesToPlan();

        // export HPR
        ObjectMapper objectMapper = new ObjectMapper(
                new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        try {
            objectMapper.writeValue(this.writer, this.hprPlan);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNodesToPlan(List<FluentView> fluents) {
        List<PlanReportroryItem> operators = this.chimp.getPlanner().getHTNMetaConstraint().getOperators();
        Map<String, PlanReportroryItem> nameActionMap = new HashMap<>();
        for (PlanReportroryItem op : operators) {
            nameActionMap.put(op.getName(), op);
        }

        List<PlanReportroryItem> methods = this.chimp.getPlanner().getHTNMetaConstraint().getMethods();
        for (PlanReportroryItem me : methods) {
            nameActionMap.put(me.getName(), me);
        }

        for (FluentView fl : fluents) {
            String name = fl.signature().predicate();
            // check if method action or operator action (primitive action)
            int type = isOperator(fl) ? 1 : 0;
            Node node = new Node((int) fl.id().value(), type, name, fl.temporalInterval().earliestStart() / 1000,
                    (fl.temporalInterval().earliestEnd() - fl.temporalInterval().earliestStart()) / 1000);
            String[] opArgNames = nameActionMap.get(name).getStringArgumentNames();
            List<String> flArgs = fl.signature().arguments();
            for (int j = 0; j < flArgs.size(); j++) {
                String key = opArgNames[j];
                // remove leading '?'
                if (key.length() > 0 && key.charAt(0) == '?') {
                    key = key.substring(1);
                }
                node.parameters.add(new KeyValue(key, flArgs.get(j)));
            }

            for (ChimpConstraint con : this.fluentsConstraintsMultiMap.get(fl.id())) {
                if (con.kind().equals(ConstraintKind.PRECONDITION)) {
                    node.preconditions.add(fluentName(requireFluent(con.from())));
                }
                if (con.kind().equals(ConstraintKind.OPENS)) {
                    node.positive_effects.add(fluentName(requireFluent(con.to())));
                }
                if (con.kind().equals(ConstraintKind.CLOSES)) {
                    node.negative_effects.add(fluentName(requireFluent(con.to())));
                }
            }
            this.hprPlan.addNode(node);
        }
    }

    private void addTemporalEdgesToPlan(List<FluentView> fluents) {
        // the index of nodes in the HPR plan is the same as that of the corresponding
        // fluent in the sorted list

        // Add edges: for two nodes check if an edge exist and add it to the plan

        int edgeIdCnt = 0;
        for (int i = 0; i < fluents.size(); i++) {
            for (int j = 0; j < fluents.size(); j++) {
                FluentId sourceId = fluents.get(i).id();
                FluentId sinkId = fluents.get(j).id();
                int source = (int) sourceId.value();
                int sink = (int) sinkId.value();

                edgeIdCnt = addTemporalEdge(edgeIdCnt, source, sink, sourceId, sinkId,
                        TemporalEndpoint.START, TemporalEndpoint.START, 0);
                edgeIdCnt = addTemporalEdge(edgeIdCnt, source, sink, sourceId, sinkId,
                        TemporalEndpoint.START, TemporalEndpoint.END, 1);
                edgeIdCnt = addTemporalEdge(edgeIdCnt, source, sink, sourceId, sinkId,
                        TemporalEndpoint.END, TemporalEndpoint.START, 2);
                edgeIdCnt = addTemporalEdge(edgeIdCnt, source, sink, sourceId, sinkId,
                        TemporalEndpoint.END, TemporalEndpoint.END, 3);
            }
        }
    }

    private int addTemporalEdge(int edgeId, int source, int sink,
                                FluentId sourceId, FluentId sinkId,
                                TemporalEndpoint fromEndpoint, TemporalEndpoint toEndpoint,
                                int edgeType) {
        Optional<TemporalBounds> distance = store.temporalDistance(sourceId, sinkId, fromEndpoint, toEndpoint);
        if (distance.isEmpty()) {
            return edgeId;
        }
        TemporalBounds bounds = distance.get();
        Edge edge = new TemporalEdge(edgeId, source, sink,
                bounds.minimum() / 1000, bounds.maximum() / 1000, edgeType);
        hprPlan.addEdge(edge);
        return edgeId + 1;
    }

    private void addCausalEdgesToPlan() {
        List<FluentView> rootTasks = filterRootTasks();

        for (FluentView root : rootTasks) {
            recursiveAddCausalEdges(root);
        }
    }

    private void recursiveAddCausalEdges(FluentView task) {
        List<FluentId> subtaskIds = new ArrayList<>();
        for (ChimpConstraint con : this.fluentsConstraintsMultiMap.get(task.id())) {
            if (con.kind().equals(ConstraintKind.DECOMPOSITION)) {
                subtaskIds.add(con.to());
            }
        }

        Collections.sort(subtaskIds, new Comparator<FluentId>() {
            @Override
            public int compare(final FluentId obj1, final FluentId obj2) {
                if (beforesTable.contains(obj1, obj2)) {
                    return -1;
                } else if (beforesTable.contains(obj2, obj1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (FluentId subId : subtaskIds) {
            recursiveAddCausalEdges(requireFluent(subId));
            int edgeId = this.hprPlan.edges.size();
            int sourceId = (int) task.id().value();
            int sinkId = (int) subId.value();
            DecompositionEdge causalEdge = new DecompositionEdge(edgeId, sourceId, sinkId);
            this.hprPlan.addEdge(causalEdge);
        }
    }

    private void createFluentsConstraintsMultiMap() {
        for (FluentView fluent : store.fluents()) {
            FluentId id = fluent.id();
            for (ChimpConstraint con : store.outgoing(id)) {
                switch (con.kind()) {
                    case DECOMPOSITION, OPENS, CLOSES -> this.fluentsConstraintsMultiMap.put(id, con);
                    case BEFORE -> this.beforesTable.put(id, con.to(), true);
                    default -> { }
                }
            }
            for (ChimpConstraint con : store.incoming(id, ConstraintKind.PRECONDITION)) {
                this.fluentsConstraintsMultiMap.put(id, con);
            }
        }
    }

    private List<FluentView> collectPlannedFluents() {
        List<FluentView> ret = new ArrayList<>();
        ret.addAll(store.fluents(FluentType.ACTIVITY));
        ret.addAll(store.fluents(FluentType.TASK));
        return ret;
    }

    private List<FluentView> filterRootTasks() {
        List<FluentView> ret = new ArrayList<>();
        for (FluentView t : this.plannedFluents) {
            if (store.incoming(t.id(), ConstraintKind.DECOMPOSITION).isEmpty()) {
                ret.add(t);
            }
        }
        return ret;
    }

    private List<FluentView> createSortedPlannedFluents() {
        List<FluentView> sorted = new ArrayList<>(this.plannedFluents);
        sorted.sort(new Comparator<FluentView>() {
            @Override
            public int compare(FluentView f0, FluentView f1) {
                int byStart = Long.compare(
                        f0.temporalInterval().earliestStart(),
                        f1.temporalInterval().earliestStart());
                if (byStart != 0) return byStart;
                int byEnd = Long.compare(
                        f0.temporalInterval().earliestEnd(),
                        f1.temporalInterval().earliestEnd());
                if (byEnd != 0) return byEnd;
                // Deterministic tiebreak for fluents that share EST and EET.
                // The previous implementation relied on HashMap iteration order,
                // which was not semantically meaningful; fluent id is stable.
                return Long.compare(f0.id().value(), f1.id().value());
            }
        });
        return sorted;
    }

    private static boolean isOperator(FluentView fl) {
        return fl.signature().predicate().startsWith("!");
    }

    private FluentView requireFluent(FluentId id) {
        return store.fluent(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Constraint references unknown fluent ID: " + id.value()));
    }

    private static String fluentName(FluentView fluent) {
        return fluent.signature().predicate() + "("
                + String.join(" ", fluent.signature().arguments()) + ")";
    }

}
