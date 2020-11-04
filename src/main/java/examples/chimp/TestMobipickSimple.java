package examples.chimp;

import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;
import planner.CHIMP;
import postprocessing.EsterelGenerator;

import java.io.StringWriter;
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

            chimp.printFullPlan();
//            chimp.drawPlanHierarchy(100);
//            chimp.drawHierarchyNetwork();
//            chimp.drawSearchSpace();

//            ConstraintNetwork.draw(chimp.getFluentSolver().getConstraintNetwork());

//            System.out.println(chimp.printDescription());

            StringWriter sw = new StringWriter();
            EsterelGenerator.generateEsterelGraph(chimp, sw);
            System.out.println("Generated Esterel Graph:");
            System.out.println(sw.toString());

        }



    }

}
