package examples.chimp;

import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import htn.valOrderingHeuristics.CHIMPValOH;
import org.metacsp.framework.Variable;
import chimp.logging.ChimpLogging;
import planner.CHIMP;

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

        CHIMPValOH valOH = new UnifyDeepestWeightNewestbindingsValOH();

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

//		ChimpLogging.setLevel(planner.getClass(), Level.FINEST);
//		ChimpLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

//		ChimpLogging.setLevel(Level.FINE);
        if (!LOGGING) {
            ChimpLogging.setLevel(Level.OFF);
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

            chimp.printFullPlan();
//            chimp.drawPlanHierarchy(100);
//            chimp.drawHierarchyNetwork();
//            chimp.drawSearchSpace();

//            ConstraintNetwork.draw(chimp.getFluentSolver().getConstraintNetwork());

//            System.out.println(chimp.printDescription());

        }



    }

}
