package postprocessing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;

import org.metacsp.framework.Variable;
import org.metacsp.framework.Constraint;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import fluentSolver.FluentConstraint;
import htn.PlanReportroryItem;

import planner.CHIMP;
import postprocessing.estereltypes.KeyValue;
import postprocessing.HPRtypes.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

// HPR = Hierarchichal Plan Representation
public class HPRGenerator {

    private CHIMP chimp;
    private Writer writer;
    private FluentNetworkSolver fluentSolver;
    private HPRPlan hprPlan;
    private Map<Duration, Fluent> timePointActivityMap;
    private ListMultimap<Fluent, FluentConstraint> fluentsConstraintsMultiMap;
    private Table<Fluent, Fluent, FluentConstraint> beforesTable;

    public HPRGenerator(CHIMP chimp, Writer writer) {
        this.chimp = chimp;
        this.writer = writer;
        this.fluentSolver = chimp.getFluentSolver();
        this.hprPlan = new HPRPlan();
        this.fluentsConstraintsMultiMap = ArrayListMultimap.create();
        this.beforesTable = HashBasedTable.create();
        createFluentsConstraintsMultiMap();
        this.timePointActivityMap = getTimePointActivityMap();
    }

    public void generateHPR() {
        // Create and sort array of timepoints for each action
        Duration[] tps = createSortedActivityTimepoints();

        addNodesToPlan(tps);
        addTemporalEdgesToPlan(tps);
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

    private void addNodesToPlan(Duration[] tps) {
        List<PlanReportroryItem> operators = this.chimp.getPlanner().getHTNMetaConstraint().getOperators();
        Map<String, PlanReportroryItem> nameActionMap = new HashMap<>();
        for (PlanReportroryItem op : operators) {
            nameActionMap.put(op.getName(), op);
        }

        List<PlanReportroryItem> methods = this.chimp.getPlanner().getHTNMetaConstraint().getMethods();
        for (PlanReportroryItem me : methods) {
            nameActionMap.put(me.getName(), me);
        }

        for (int i = 0; i < tps.length; i++) {
            Duration tp = tps[i];
            Fluent fl = this.timePointActivityMap.get(tp);
            String name = fl.getCompoundSymbolicVariable().getPredicateName();
            // check if method action or operator action (primitive action)
            int type = isOperator(fl) ? 1 : 0;
            Node node = new Node(fl.getID(), type, name, fl.getAllenInterval().getEST() / 1000,
                    (fl.getAllenInterval().getEET() - fl.getAllenInterval().getEST()) / 1000);
            String[] opArgNames = nameActionMap.get(name).getStringArgumentNames();
            String[] flArgs = fl.getCompoundSymbolicVariable().getArgs();
            for (int j = 0; j < flArgs.length; j++) {
                String key = opArgNames[j];
                // remove leading '?'
                if (key.length() > 0 && key.charAt(0) == '?') {
                    key = key.substring(1);
                }
                node.parameters.add(new KeyValue(key, flArgs[j]));
            }

            for (FluentConstraint con : this.fluentsConstraintsMultiMap.get(fl)) {
                FluentConstraint.Type con_type = con.getType();
                if (con_type.equals(FluentConstraint.Type.PRE)) {
                    node.preconditions.add(((Fluent) con.getFrom()).getCompoundSymbolicVariable().getName());
                }
                if (con_type.equals(FluentConstraint.Type.OPENS)) {
                    node.positive_effects.add(((Fluent) con.getTo()).getCompoundSymbolicVariable().getName());
                }
                if (con_type.equals(FluentConstraint.Type.CLOSES)) {
                    node.negative_effects.add(((Fluent) con.getTo()).getCompoundSymbolicVariable().getName());
                }
            }
            this.hprPlan.addNode(node);
        }
    }

    private void addTemporalEdgesToPlan(Duration[] tps) {
        APSPSolver apspSolver = ((APSPSolver) ((AllenIntervalNetworkSolver) fluentSolver.getConstraintSolvers()[1])
                .getConstraintSolvers()[0]);

        // the index of nodes in the HPR plan is the same as that of the corresponding
        // timepoint in tps

        // Add edges: for two nodes check if an edge exist and add it to the plan

        int edgeIdCnt = 0;
        for (int i = 0; i < tps.length; i++) {
            for (int j = 0; j < tps.length; j++) {
                SimpleDistanceConstraint sdcSS = apspSolver.getConstraint(tps[i].start, tps[j].start);
                SimpleDistanceConstraint sdcSE = apspSolver.getConstraint(tps[i].start, tps[j].end);
                SimpleDistanceConstraint sdcES = apspSolver.getConstraint(tps[i].end, tps[j].start);
                SimpleDistanceConstraint sdcEE = apspSolver.getConstraint(tps[i].end, tps[j].end);
                Fluent source_fl = this.timePointActivityMap.get(tps[i]);
                Fluent sink_fl = this.timePointActivityMap.get(tps[j]);

                // create edges
                if (sdcSS != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, source_fl.getID(), sink_fl.getID(), sdcSS.getMinimum() / 1000,
                            sdcSS.getMaximum() / 1000, 0);
                    hprPlan.addEdge(edge);
                }
                if (sdcSE != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, source_fl.getID(), sink_fl.getID(), sdcSE.getMinimum() / 1000,
                            sdcSE.getMaximum() / 1000, 1);
                    hprPlan.addEdge(edge);
                }
                if (sdcES != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, source_fl.getID(), sink_fl.getID(), sdcES.getMinimum() / 1000,
                            sdcES.getMaximum() / 1000, 2);
                    hprPlan.addEdge(edge);
                }
                if (sdcEE != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, source_fl.getID(), sink_fl.getID(), sdcEE.getMinimum() / 1000,
                            sdcEE.getMaximum() / 1000, 3);
                    hprPlan.addEdge(edge);
                }
            }
        }
    }

    private void addCausalEdgesToPlan() {
        List<Fluent> rootTasks = filterRootTasks();

        for (Fluent root : rootTasks) {
            recursiveAddCausalEdges(root);
        }
    }

    private void recursiveAddCausalEdges(Fluent task) {
        List<FluentConstraint> cons = this.fluentsConstraintsMultiMap.get(task);
        List<Fluent> subtasks = new ArrayList<Fluent>();
        for (FluentConstraint con : cons) {
            if (con.getType().equals(FluentConstraint.Type.DC)) {
                subtasks.add((Fluent) con.getTo());
            }
        }

        Collections.sort(subtasks, new Comparator<Fluent>() {
            @Override
            public int compare(final Fluent obj1, final Fluent obj2) {
                if (beforesTable.contains(obj1, obj2)) {
                    return -1;
                } else if (beforesTable.contains(obj2, obj1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (Fluent sub : subtasks) {
            recursiveAddCausalEdges(sub);
            int edgeId = this.hprPlan.edges.size();
            int sourceId = task.getID();
            int sinkId = sub.getID();
            DecompositionEdge causalEdge = new DecompositionEdge(edgeId, sourceId, sinkId);
            this.hprPlan.addEdge(causalEdge);
        }
    }

    private void createFluentsConstraintsMultiMap() {
        for (Constraint con : this.fluentSolver.getConstraints()) {
            if (con instanceof FluentConstraint) {
                Fluent from = (Fluent) ((FluentConstraint) con).getFrom();
                Fluent to = (Fluent) ((FluentConstraint) con).getTo();
                FluentConstraint.Type type = ((FluentConstraint) con).getType();
                if (type.equals(FluentConstraint.Type.DC)) {
                    this.fluentsConstraintsMultiMap.put(from, (FluentConstraint) con);
                }
                if (type.equals(FluentConstraint.Type.PRE)) {
                    this.fluentsConstraintsMultiMap.put(to, (FluentConstraint) con);
                }
                if (type.equals(FluentConstraint.Type.OPENS)) {
                    this.fluentsConstraintsMultiMap.put(from, (FluentConstraint) con);
                }
                if (type.equals(FluentConstraint.Type.CLOSES)) {
                    this.fluentsConstraintsMultiMap.put(from, (FluentConstraint) con);
                }
                if (type.equals(FluentConstraint.Type.BEFORE)) {
                    this.beforesTable.put(from, to, (FluentConstraint) con);
                }
            }
        }
    }

    private List<Fluent> filterPlannedFluents() {
        ArrayList<Fluent> ret = new ArrayList<Fluent>();
        for (Variable var : this.fluentSolver.getVariables("Activity")) {
            ret.add((Fluent) var);
        }
        for (Variable var : this.fluentSolver.getVariables("Task")) {
            ret.add((Fluent) var);
        }

        return ret;
    }

    private List<Fluent> filterRootTasks() {
        ArrayList<Fluent> ret = new ArrayList<Fluent>();
        SetMultimap<Fluent, FluentConstraint> fluentsIncomingDCsMultiMap = HashMultimap.create();

        for (Constraint con : this.fluentSolver.getConstraints()) {
            if (con instanceof FluentConstraint) {
                FluentConstraint.Type type = ((FluentConstraint) con).getType();
                if (type.equals(FluentConstraint.Type.DC)) {
                    fluentsIncomingDCsMultiMap.put((Fluent) ((FluentConstraint) con).getTo(), (FluentConstraint) con);
                }
            }
        }

        for (Fluent t : this.timePointActivityMap.values()) {
            if (!fluentsIncomingDCsMultiMap.containsKey(t)) {
                ret.add(t);
            }
        }

        return ret;
    }

    private Map<Duration, Fluent> getTimePointActivityMap() {
        Map<Duration, Fluent> timePointActivityMap = new HashMap<>();
        List<Fluent> plannedFluents = filterPlannedFluents();
        for (Fluent fl : plannedFluents) {
            Duration duration = new Duration(fl.getAllenInterval().getStart(), fl.getAllenInterval().getEnd());
            timePointActivityMap.put(duration, fl);
        }
        return timePointActivityMap;
    }

    private Duration[] createSortedActivityTimepoints() {
        Duration[] tps = new Duration[this.timePointActivityMap.keySet().size()];
        int pos = 0;
        for (Duration tp : this.timePointActivityMap.keySet()) {
            tps[pos++] = tp;
        }
        Arrays.sort(tps, new Comparator<Duration>() {
            @Override
            public int compare(Duration t0, Duration t1) {
                int comparison = Long.compare(t0.start.getLowerBound(), t1.start.getLowerBound());
                return (comparison != 0) ? comparison : Long.compare(t0.end.getLowerBound(), t1.end.getLowerBound());
            }
        });
        return tps;
    }

    private static boolean isOperator(Fluent fl) {
        return fl.getCompoundSymbolicVariable().getPredicateName().startsWith("!");
    }

}
