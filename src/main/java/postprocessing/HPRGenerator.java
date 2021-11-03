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
    private Map<Fluent, HPRActionDispatch> fluentActionDispatchMap;
    private ListMultimap<Fluent, FluentConstraint> fluentsConstraintsMultiMap;
    private Table<Fluent, Fluent, FluentConstraint> beforesTable;
    private Map<Fluent, Integer> fluentNodeIdMap;

    public HPRGenerator(CHIMP chimp, Writer writer) {
        this.chimp = chimp;
        this.writer = writer;
        this.fluentSolver = chimp.getFluentSolver();
        this.hprPlan = new HPRPlan();
        this.fluentsConstraintsMultiMap = ArrayListMultimap.create();
        this.beforesTable = HashBasedTable.create();
        createFluentsConstraintsMultiMap();
        this.timePointActivityMap = getTimePointActivityMap();
        this.fluentActionDispatchMap = getFluentActionDispatchMap();
        this.fluentNodeIdMap = new HashMap<>();
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
        for (int i = 0; i < tps.length; i++) {
            Duration tp = tps[i];
            Fluent fl = this.timePointActivityMap.get(tp);
            String name = fl.getCompoundSymbolicVariable().getPredicateName();
            // check if method action or operator action (primitive action)
            int type = isOperator(fl) ? 1 : 0;
            Node node = new Node(i, type, name, this.fluentActionDispatchMap.get(fl));
            this.hprPlan.addNode(node);
            this.fluentNodeIdMap.put(fl, i);
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
                if (i == j) {
                    continue;
                }
                SimpleDistanceConstraint sdcSS = apspSolver.getConstraint(tps[i].start, tps[j].start);
                SimpleDistanceConstraint sdcSE = apspSolver.getConstraint(tps[i].start, tps[j].end);
                SimpleDistanceConstraint sdcES = apspSolver.getConstraint(tps[i].end, tps[j].start);
                SimpleDistanceConstraint sdcEE = apspSolver.getConstraint(tps[i].end, tps[j].end);

                // create edges
                if (sdcSS != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, "edge" + edgeId, i, j, sdcSS.getMinimum() / 1000,
                            sdcSS.getMaximum() / 1000, "start_to_start");
                    hprPlan.addEdge(edge);
                }
                if (sdcSE != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, "edge" + edgeId, i, j, sdcSE.getMinimum() / 1000,
                            sdcSE.getMaximum() / 1000, "start_to_end");
                    hprPlan.addEdge(edge);
                }
                if (sdcES != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, "edge" + edgeId, i, j, sdcES.getMinimum() / 1000,
                            sdcES.getMaximum() / 1000, "end_to_start");
                    hprPlan.addEdge(edge);
                }
                if (sdcEE != null) {
                    int edgeId = edgeIdCnt++;
                    Edge edge = new TemporalEdge(edgeId, "edge" + edgeId, i, j, sdcEE.getMinimum() / 1000,
                            sdcEE.getMaximum() / 1000, "end_to_end");
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
            int sourceId = this.fluentNodeIdMap.get(task);
            int sinkId = this.fluentNodeIdMap.get(sub);
            CausalEdge causalEdge = new CausalEdge(edgeId, "edge" + edgeId, sourceId, sinkId);
            this.hprPlan.addEdge(causalEdge);
        }

        for (int i = 0; i < subtasks.size(); i++) {
            Fluent from = subtasks.get(i);
            for (int j = i + 1; j < subtasks.size(); j++) {
                Fluent to = subtasks.get(j);
                if (beforesTable.contains(from, to)) {
                    int edgeId = this.hprPlan.edges.size();
                    int sourceId = this.fluentNodeIdMap.get(from);
                    int sinkId = this.fluentNodeIdMap.get(to);
                    Edge beforeEdge = new Edge(edgeId, "edge" + edgeId, 2, sourceId, sinkId);
                    this.hprPlan.addEdge(beforeEdge);
                }
            }
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

    private Map<Fluent, HPRActionDispatch> getFluentActionDispatchMap() {
        // we need the operators and methods to get the names of the actions' arguments
        List<PlanReportroryItem> operators = this.chimp.getPlanner().getHTNMetaConstraint().getOperators();
        Map<String, PlanReportroryItem> nameActionMap = new HashMap<>();
        for (PlanReportroryItem op : operators) {
            nameActionMap.put(op.getName(), op);
        }

        List<PlanReportroryItem> methods = this.chimp.getPlanner().getHTNMetaConstraint().getMethods();
        for (PlanReportroryItem me : methods) {
            nameActionMap.put(me.getName(), me);
        }

        Map<Fluent, HPRActionDispatch> fluentActionDispatchMap = new HashMap<>();
        for (Fluent fl : this.timePointActivityMap.values()) {
            String actionName = fl.getCompoundSymbolicVariable().getPredicateName();
            HPRActionDispatch actionDispatch = new HPRActionDispatch(fl.getID(), actionName);
            actionDispatch.duration = (fl.getAllenInterval().getEET() - fl.getAllenInterval().getEST()) / 1000;
            String[] opArgNames = nameActionMap.get(actionName).getStringArgumentNames();
            String[] flArgs = fl.getCompoundSymbolicVariable().getArgs();
            for (int i = 0; i < flArgs.length; i++) {
                String key = opArgNames[i];
                // remove leading '?'
                if (key.length() > 0 && key.charAt(0) == '?') {
                    key = key.substring(1);
                }
                actionDispatch.parameters.add(new KeyValue(key, flArgs[i]));
            }
            actionDispatch.dispatch_time = fl.getAllenInterval().getEST() / 1000;

            for (FluentConstraint con : this.fluentsConstraintsMultiMap.get(fl)) {
                FluentConstraint.Type type = con.getType();
                if (type.equals(FluentConstraint.Type.PRE)) {
                    actionDispatch.preconditions.add(((Fluent) con.getFrom()).getCompoundSymbolicVariable().getName());
                }
                if (type.equals(FluentConstraint.Type.OPENS)) {
                    actionDispatch.effects.add("+" + ((Fluent) con.getTo()).getCompoundSymbolicVariable().getName());
                }
                if (type.equals(FluentConstraint.Type.CLOSES)) {
                    actionDispatch.effects.add("-" + ((Fluent) con.getTo()).getCompoundSymbolicVariable().getName());
                }
            }

            fluentActionDispatchMap.put(fl, actionDispatch);
        }
        return fluentActionDispatchMap;
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
