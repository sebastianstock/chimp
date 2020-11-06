package postprocessing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNOperator;
import htn.HTNPlanner;
import htn.PlanReportroryItem;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
import planner.CHIMP;
import postprocessing.estereltypes.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class EsterelGenerator {

    public static void generateEsterelGraph(CHIMP chimp, Writer writer) {

        FluentNetworkSolver fluentSolver = chimp.getFluentSolver();
        List<SimpleDistanceConstraint> additionalConsList = calcAdditionalConstraints(fluentSolver);

        // Add new constraints to apspSolver
        APSPSolver apspSolver = getApspSolver(fluentSolver);
        boolean success = apspSolver.addConstraints(
                additionalConsList.toArray(new SimpleDistanceConstraint[additionalConsList.size()]));

        // Create Esterel Graph:

        Map<TimePoint, Fluent> timePointActivityMap = getTimePointActivityMap(fluentSolver);

        // create ActionDispatch messages
        Map<Fluent, ActionDispatch> fluentActionDispatchMap = getFluentActionDispatchMap(timePointActivityMap, chimp);

        // Create and sort array of timepoints
        TimePoint[] tps = createSortedActivityTimepoints(apspSolver, timePointActivityMap);

        EsterelPlan esterelPlan = new EsterelPlan();
        // add nodes to esterel plan
        for (int i = 0; i < tps.length - 1; i++) {
            TimePoint tp = tps[i];
            if (tp.getID() == 0) {
                EsterelPlanNode node = new EsterelPlanNode();
                node.name = "plan_start";
                node.node_id = 0;
                node.node_type = 2;
                node.action = new ActionDispatch(0, "");
                esterelPlan.nodes.add(node);
            } else {
                Fluent fl = timePointActivityMap.get(tp);
                EsterelPlanNode node = new EsterelPlanNode();
                node.name = fl.getCompoundSymbolicVariable().getPredicateName();
                if (isEndpoint(tp)) {
                    node.name += "_end";
                } else {
                    node.name += "_start";
                }
                node.node_type = isEndpoint(tp) ? 1 : 0;
                node.node_id = i;
                node.action = fluentActionDispatchMap.get(fl);
                esterelPlan.nodes.add(node);
            }
        }
        // the index of nodes in the esterel plan is the same as that of the corresponding timepoint in tps

        // Add edges: for two nodes check if an edge exist and add it to the plan
        int edgeIdCnt = 0;
        for (int i = 0; i < tps.length - 1; i++) {
            for (int j = 0; j < tps.length - 1; j++) {  // horizon tp is last in tps and can be ignored
                SimpleDistanceConstraint sdc = apspSolver.getConstraint(tps[i], tps[j]);
                if (sdc != null) {
                    // create edge
                    int edgeId = edgeIdCnt++;
                    EsterelPlanEdge edge = new EsterelPlanEdge(edgeId, "edge" + edgeId, i, j,
                            sdc.getMinimum() / 1000, sdc.getMaximum() / 1000);
                    esterelPlan.addEdge(edge);
                }
            }
        }

        // export esterel
        ObjectMapper objectMapper = new ObjectMapper(
                new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        try {
            objectMapper.writeValue(writer, esterelPlan);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TimePoint[] createSortedActivityTimepoints(APSPSolver apspSolver, Map<TimePoint, Fluent> timePointActivityMap) {
        TimePoint[] tps = new TimePoint[timePointActivityMap.keySet().size() + 2];
        tps[0] = apspSolver.getSource();
        tps[tps.length-1] = apspSolver.getSink();
        int pos = 1;
        for (TimePoint tp : timePointActivityMap.keySet()) {
            tps[pos++] = tp;
        }
        Arrays.sort(tps, new Comparator<TimePoint>() {
            @Override
            public int compare(TimePoint t0, TimePoint t1) {
                return Long.compare(t0.getLowerBound(), t1.getLowerBound());
            }
        });
        return tps;
    }

    public static Map<Fluent, ActionDispatch> getFluentActionDispatchMap(Map<TimePoint, Fluent> timePointActivityMap,
                                                                         CHIMP chimp) {
        // we need the operators to get the names of the actions' arguments
        List<PlanReportroryItem> operators = chimp.getPlanner().getHTNMetaConstraint().getOperators();
        Map<String, PlanReportroryItem> nameOperatorMap = new HashMap<>();
        for (PlanReportroryItem op : operators) {
            nameOperatorMap.put(op.getName(), op);
        }

        Map<Fluent, ActionDispatch> fluentActionDispatchMap = new HashMap<>();
        for (Fluent fl : timePointActivityMap.values()) {
            String actionName= fl.getCompoundSymbolicVariable().getPredicateName();
            ActionDispatch actionDispatch = new ActionDispatch(fl.getID(), actionName);
            actionDispatch.duration = (fl.getAllenInterval().getEET() - fl.getAllenInterval().getEST()) / 1000;
            String[] opArgNames = nameOperatorMap.get(actionName).getStringArgumentNames();
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
            fluentActionDispatchMap.put(fl, actionDispatch);
        }
        return fluentActionDispatchMap;
    }

    public static Map<TimePoint, Fluent> getTimePointActivityMap(FluentNetworkSolver fluentSolver) {
        Map<TimePoint, Fluent> timePointActivityMap = new HashMap<>();
        for (Variable activityVar : fluentSolver.getVariables(Fluent.ACTIVITY_TYPE_STR))  {
            Fluent activity = (Fluent) activityVar;
            timePointActivityMap.put(activity.getAllenInterval().getStart(), activity);
            timePointActivityMap.put(activity.getAllenInterval().getEnd(), activity);
        }
        return timePointActivityMap;
    }

    public static APSPSolver getApspSolver(FluentNetworkSolver fluentSolver) {
        AllenIntervalNetworkSolver aiSolver =
                (AllenIntervalNetworkSolver) fluentSolver.getConstraintSolvers()[1];
        APSPSolver apspSolver = (APSPSolver) aiSolver.getConstraintSolvers()[0];
        return apspSolver;
    }

    /**
     * Compute implicit constraints in between activities for between which a path exists.
     * @param fluentSolver
     * @return List of implicit constraints between activities.
     */
    public static List<SimpleDistanceConstraint> calcAdditionalConstraints(FluentNetworkSolver fluentSolver) {
        APSPSolver apspSolver = getApspSolver(fluentSolver);
        // Copy temporal constraint network to dummy cn
        ConstraintNetwork cn = (ConstraintNetwork) apspSolver.getConstraintNetwork().clone();

        List<SimpleDistanceConstraint> additionalConsList = new ArrayList<>();
        // [0,0]-constraints are relevant in both directions, therefore we also need edges in both directions
        // Add reverse constraints for constraints [0,0]-constraints from end-timepoint to end-timepoint
        for (Constraint con : cn.getConstraints()) {
            SimpleDistanceConstraint dst = (SimpleDistanceConstraint) con;
            if (isEndpoint(dst.getFrom()) && isEndpoint(dst.getTo()) &&
                    dst.getMinimum() == 0 && dst.getMaximum() == 0) {
                additionalConsList.add(createReverseConstraint(dst));
            }
        }
        cn.addConstraints(additionalConsList.toArray(new SimpleDistanceConstraint[additionalConsList.size()]));

        // Replace fluents that are not activities
        for (Variable v : fluentSolver.getVariables()) {
            Fluent fl = (Fluent) v;
            if (!fl.isActivity()) {
                TimePoint start = fl.getAllenInterval().getStart();
                additionalConsList.addAll(replaceNode(cn, start));
                TimePoint end = fl.getAllenInterval().getEnd();
                additionalConsList.addAll(replaceNode(cn, end));
            }
        }
        return additionalConsList;
    }

    //

    /**
     * Check if the temporal variable represents a start or end point.
     *
     * Assumes that ids of endpoints are odd.
     * @return true if the temporal variable is a endpoint, false if it as startpoint
     */
    private static boolean isEndpoint(Variable var) {
        return (var.getID() % 2) == 1;
    }

    private static SimpleDistanceConstraint createReverseConstraint(SimpleDistanceConstraint con) {
        SimpleDistanceConstraint reverseCon = new SimpleDistanceConstraint();
        reverseCon.setMinimum(con.getMinimum());
        reverseCon.setMaximum(con.getMaximum());
        reverseCon.setFrom(con.getTo());
        reverseCon.setTo(con.getFrom());
        return reverseCon;
    }

    public static List<SimpleDistanceConstraint> replaceNode(ConstraintNetwork cn, TimePoint tp) {
        List<SimpleDistanceConstraint> newConstraints = new ArrayList<>();
        for (Constraint inConstraint : cn.getIngoingEdges(tp)) {
            SimpleDistanceConstraint inDistCon = (SimpleDistanceConstraint) inConstraint;
            if (inDistCon.getFrom().getID() < tp.getID())
                continue; // skip node that has already been processed
            for (Constraint outConstraint : cn.getOutgoingEdges(tp)) {
                SimpleDistanceConstraint outDistCon = (SimpleDistanceConstraint) outConstraint;
                if (outDistCon.getTo().getID() < tp.getID())
                    continue; // skip node that has already been processed
                SimpleDistanceConstraint sumDistCon = new SimpleDistanceConstraint();
                if (inDistCon.getMinimum() == APSPSolver.INF || outDistCon.getMinimum() == APSPSolver.INF) {
                    sumDistCon.setMinimum(APSPSolver.INF);
                } else {
                    sumDistCon.setMinimum(inDistCon.getMinimum() + outDistCon.getMinimum());
                }
                if (inDistCon.getMaximum() == APSPSolver.INF || outDistCon.getMaximum() == APSPSolver.INF) {
                    sumDistCon.setMaximum(APSPSolver.INF);
                } else {
                    sumDistCon.setMaximum(inDistCon.getMaximum() + outDistCon.getMaximum());
                }
                sumDistCon.setFrom(inDistCon.getFrom());
                sumDistCon.setTo(outDistCon.getTo());
                if(sumDistCon.getFrom().getID() == sumDistCon.getTo().getID()) {
//                    System.out.println("Found self-constraint: " + sumDistCon.toString());
                } else {
                    newConstraints.add(sumDistCon);
                    cn.addConstraint(sumDistCon);
                }

//                System.out.println("Replacement for " + inDistCon.toString() + " and " + outDistCon.toString() + ":");
//                System.out.println("   -> " + sumDistCon.toString());
            }
        }
        return newConstraints;
    }
}
