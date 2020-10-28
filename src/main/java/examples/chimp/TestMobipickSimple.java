package examples.chimp;

import fluentSolver.Fluent;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.multi.TCSP.DistanceConstraint;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
import org.metacsp.utility.logging.MetaCSPLogging;
import planner.CHIMP;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TestMobipickSimple {

    static final boolean LOGGING = false;
    static final boolean GUESS_ORDERING = false;
    static final boolean PRINT_PLAN = true;
    static final boolean DRAW = false;


    public static void main(String[] args) {

        String problemFile = "domains/mobipick/simple_navigation/problem.pdl";
        String domainFile = "domains/mobipick/simple_navigation/domain.ddl";

//        String problemFile = "problems/test_m_serve_coffee_problem_1.pdl";
//        String domainFile = "domains/ordered_domain.ddl";

        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

        CHIMP.CHIMPBuilder builder;

        try {
            builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .htnUnification(true);
            if (GUESS_ORDERING) {
                builder.guessOrdering(true);
            }

        } catch (DomainParsingException e) {
            e.printStackTrace();
            return;
        }
        CHIMP chimp = builder.build();

//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

//		MetaCSPLogging.setLevel(Level.FINE);
        if (!LOGGING) {
            MetaCSPLogging.setLevel(Level.OFF);
        }

        System.out.println("Found plan? " + chimp.generatePlan());
        chimp.printStats(System.out);

        if (PRINT_PLAN) {
            Variable[] planVector = chimp.extractActions();
            int c = 0;
            for (Variable act : planVector) {
                if (act.getComponent() != null)
                    System.out.println(c++ + ".\t" + act);
            }

//            chimp.printFullPlan();
//            chimp.drawPlanHierarchy(100);
//            chimp.drawHierarchyNetwork();
//            chimp.drawSearchSpace();

//            ConstraintNetwork.draw(chimp.getFluentSolver().getConstraintNetwork());

            System.out.println(chimp.printDescription());

            AllenIntervalNetworkSolver aiSolver =
                    (AllenIntervalNetworkSolver) chimp.getFluentSolver().getConstraintSolvers()[1];
            APSPSolver apspSolver = (APSPSolver) aiSolver.getConstraintSolvers()[0];

//            ConstraintNetwork.draw(aiSolver.getConstraintNetwork());
//            ConstraintNetwork.draw(apspSolver.getConstraintNetwork());

            AllenInterval ai2 = (AllenInterval) aiSolver.getVariable(3);
            System.out.println("ai2: " + ai2.toString());
            TimePoint tp2Start = ai2.getStart();
            TimePoint tp2End = ai2.getEnd();
            System.out.println("tp2Start " + tp2Start.toString());
            System.out.println("tp2End " + tp2End.toString());
//            apspSolver.getConstraintNetwork().getDe

            // Copy temporal constraint network to dummy cn

            ConstraintNetwork cn = new ConstraintNetwork(null);
            for (Constraint con : apspSolver.getConstraints()) {
                cn.addConstraint(con);
            }

            // add reverse constraints for constraints [0,0]-constraints from end-timepoint to end-timepoint
            List<SimpleDistanceConstraint> additionalConsList = new ArrayList<>();
            for (Constraint con : apspSolver.getConstraints()) {
                SimpleDistanceConstraint dst = (SimpleDistanceConstraint) con;
                if ((dst.getFrom().getID() % 2) == 1 && (dst.getTo().getID() % 2) == 1 &&
                        dst.getMinimum() == 0 && dst.getMaximum() == 0) {
                    SimpleDistanceConstraint reverseCon = new SimpleDistanceConstraint();
                    reverseCon.setMinimum(0);
                    reverseCon.setMaximum(0);
                    reverseCon.setFrom(dst.getTo());
                    reverseCon.setTo(dst.getFrom());
                    additionalConsList.add(reverseCon);
                }
            }

            cn.addConstraints(additionalConsList.toArray(new SimpleDistanceConstraint[additionalConsList.size()]));

            for (Variable v : chimp.getFluentSolver().getVariables()) {
                Fluent fl = (Fluent) v;
                if (fl.getTypeStr().equals(Fluent.ACTIVITY_TYPE_STR))
                    continue;

                TimePoint start = fl.getAllenInterval().getStart();
                List<SimpleDistanceConstraint> replacingStart = replaceNode(cn, start);
                for (SimpleDistanceConstraint con : replacingStart) {
                    cn.addConstraint(con);
                    additionalConsList.add(con);
                }

                TimePoint end = fl.getAllenInterval().getEnd();
                List<SimpleDistanceConstraint> replacingEnd = replaceNode(cn, end);
                for (SimpleDistanceConstraint con : replacingEnd) {
                    cn.addConstraint(con);
                    additionalConsList.add(con);
                }
            }

            System.out.println("Before adding " + additionalConsList.size() + " constraints");
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

            for (Constraint con : apspSolver.getConstraintNetwork().getConstraints()) {
                SimpleDistanceConstraint dstCon = (SimpleDistanceConstraint) con;
                int fromID = dstCon.getFrom().getID();
                int toID = dstCon.getTo().getID();
                if (fromID >= 12 && fromID <=15 && toID >=12 && toID <=15) { // test if both are activities
                    System.out.println(dstCon.toString());
                }
            }


//            Fluent torsoassume = (Fluent) chimp.getFluentSolver().getConstraintNetwork().getVariable(17);
//            System.out.println("torsoassume: " + torsoassume.toString());
//            System.out.println("tmporal var id: " + torsoassume.getTemporalVariable().getID());
//            AllenInterval torsoAllen = torsoassume.getTemporalVariable();
//            Constraint[] edges = torsoassume.getTemporalVariable().getConstraintSolver().getConstraintNetwork().getIncidentEdges(torsoassume);
//            System.out.println(edges.length);
        }



    }

//    private static void replaceTimepoint(ConstraintNetwork cn, TimePoint start) {
//        List<SimpleDistanceConstraint> replacingStart = replaceNode(cn, start);
//        System.out.println("Before");
//        boolean added = apspSolver.addConstraintsNoPropagation(
//                replacingStart.toArray(new SimpleDistanceConstraint[replacingStart.size()]));
//        System.out.println("Added all successfully? " + added);
//        if (!added) { // could not add all at once -> add one by one
//            for (SimpleDistanceConstraint dst : replacingStart) {
//                System.out.println("Adding " + dst.toString() + " ? " + apspSolver.addConstraint(dst));
//            }
//        }
//
//    }

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
                }

//                System.out.println("Replacement for " + inDistCon.toString() + " and " + outDistCon.toString() + ":");
//                System.out.println("   -> " + sumDistCon.toString());
            }
        }

        System.out.println();
        return newConstraints;
    }

}
