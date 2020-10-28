package postprocessing;

import fluentSolver.Fluent;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
import planner.CHIMP;

import java.util.ArrayList;
import java.util.List;

public class EsterelGenerator {

    public static void generateEsterelGraph(CHIMP chimp) {
        AllenIntervalNetworkSolver aiSolver =
                (AllenIntervalNetworkSolver) chimp.getFluentSolver().getConstraintSolvers()[1];
        APSPSolver apspSolver = (APSPSolver) aiSolver.getConstraintSolvers()[0];

        // Copy temporal constraint network to dummy cn
        ConstraintNetwork cn = (ConstraintNetwork) apspSolver.getConstraintNetwork().clone();

        // [0,0]-constraints are relevant in both directions, therefore we also need edges in both directions
        // add reverse constraints for constraints [0,0]-constraints from end-timepoint to end-timepoint
        List<SimpleDistanceConstraint> additionalConsList = new ArrayList<>();
        for (Constraint con : cn.getConstraints()) {
            SimpleDistanceConstraint dst = (SimpleDistanceConstraint) con;
            if (isEndpoint(dst.getFrom()) && isEndpoint(dst.getTo()) &&
                    dst.getMinimum() == 0 && dst.getMaximum() == 0) {
                additionalConsList.add(createReverseConstraint(dst));
            }
        }
        cn.addConstraints(additionalConsList.toArray(new SimpleDistanceConstraint[additionalConsList.size()]));

        // replace fluents that are not activities
        for (Variable v : chimp.getFluentSolver().getVariables()) {
            Fluent fl = (Fluent) v;
            if (!fl.isActivity()) {
                TimePoint start = fl.getAllenInterval().getStart();
                additionalConsList.addAll(replaceNode(cn, start));
                TimePoint end = fl.getAllenInterval().getEnd();
                additionalConsList.addAll(replaceNode(cn, end));
            }
        }

        System.out.println("Adding " + additionalConsList.size() + " constraints");
        boolean success = apspSolver.addConstraints(
                additionalConsList.toArray(new SimpleDistanceConstraint[additionalConsList.size()]));
        System.out.println("Success? " + success);

        // search for constraints between activities
        TimePoint tp12 = (TimePoint) apspSolver.getVariable(12);
        TimePoint tp13 = (TimePoint) apspSolver.getVariable(13);
        TimePoint tp14 = (TimePoint) apspSolver.getVariable(14);
        TimePoint tp15 = (TimePoint) apspSolver.getVariable(15);
        SimpleDistanceConstraint dst14To15 = apspSolver.getConstraint(tp14, tp15);
        System.out.println(dst14To15.toString());

        System.out.println("Searching for 12-13: " + apspSolver.getConstraint(tp12, tp13).toString());
        System.out.println("Searching for 14-12: " + apspSolver.getConstraint(tp14, tp12).toString());
        System.out.println("Searching for 15-12: " + apspSolver.getConstraint(tp15, tp12).toString());
//            System.out.println(apspSolver.getConstraint(tp12, tp14).toString());
//            System.out.println("Searching for 13-12: " + apspSolver.getConstraint(tp13, tp12).toString());

        for (Constraint con : apspSolver.getConstraintNetwork().getConstraints()) {
            SimpleDistanceConstraint dstCon = (SimpleDistanceConstraint) con;
            int fromID = dstCon.getFrom().getID();
            int toID = dstCon.getTo().getID();
            if (fromID >= 12 && fromID <=15 && toID >=12 && toID <=15) { // test if both are activities
                System.out.println(dstCon.toString());
            }
        }
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
        System.out.println("## Replacing " + tp.toString());
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
                    System.out.println("Found self-constraint: " + sumDistCon.toString());
                } else {
                    newConstraints.add(sumDistCon);
                    cn.addConstraint(sumDistCon);
                }

//                System.out.println("Replacement for " + inDistCon.toString() + " and " + outDistCon.toString() + ":");
//                System.out.println("   -> " + sumDistCon.toString());
            }
        }
        System.out.println();
        return newConstraints;
    }
}
