package hybridDomainParsing.classic.antlr;

import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.ProblemParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.utility.logging.MetaCSPLogging;
import planner.CHIMP;
import planner.CHIMPProblem;

import java.io.IOException;
import java.util.logging.Level;

public class TestDomainReader {

    public static void main(String[] args) {

        String testDomain = "(HybridHTNDomain testDomain) (MaxArgs 5) (PredicateSymbols table n !pick) " +
                "(StateVariable sv 1 n)" +
                "(Resource resource1 7)" +
                "(:operator 3\n" +
                " (Head !move_base(?toArea))\n" +
                " (Pre p1 RobotAt(?fromArea))\n" +
                " (Constraint OverlappedBy(task,p1))\n" +
                " (Constraint Duration[4000,INF](task))\n" +
                " (Add e1 RobotAt(?toArea))\n" +
                " (Constraint Meets(task,e1))\n" +
                " (Del p1)\n" +
                " (ResourceUsage navigationCapacity 1)\n" +
                " (ResourceUsage\n" +
                "  (Usage leftArm1ManCapacity 1)\n" +
                "  (Param 2 leftArm1))\n" +
                ")";

        String testDomain2 = "(HybridHTNDomain testDomain) (MaxArgs 5) (PredicateSymbols table n !pick) " +
                "(StateVariable sv 1 n)" +
                "(Resource resource1 7)" +
                "(:method\n" +
                " (Head adapt_torso(?newPose))\n" +
                " (Pre p1 HasTorsoPosture(?oldPose))\n" +
                " (VarDifferent ?newPose ?oldPose) \n" +
                " (Sub s1 !move_torso(?newPose))\n" +
                " (Constraint Equals(s1,task))\n" +
                " )";

        String problemFile = "problems/test_m_serve_coffee_problem_1.pdl";
        String domainFile = "domains/ordered_domain.ddl";
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

//        ChimpClassicLexer lexer = new ChimpClassicLexer(CharStreams.fromString(testDomain2));
        ChimpClassicLexer lexer = null;
        try {
            lexer = new ChimpClassicLexer(CharStreams.fromFileName(domainFile));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChimpClassicParser parser = new ChimpClassicParser(tokens);

        try {
            CHIMPProblem problem = new ProblemParser(problemFile);

            ChimpClassicReader visitor = new ChimpClassicReader(problem.getTypesInstancesMap());
            ChimpClassicReader.ParsedDomain domain = visitor.visitDomain(parser.domain());
            System.out.println(domain.toString());


//            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domain, problem)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
                    .htnUnification(true);
            CHIMP chimp = builder.build();

            MetaCSPLogging.setLevel(HTNPlanner.class, Level.FINEST);
            MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

            System.out.println("Found plan? " + chimp.generatePlan());
            chimp.printStats(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FluentNetworkSolver createDummyFluentNetworkSolver() {
        String[] symbolsPredicates = {"on", "robotat", "get_mug"};
        String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
        String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
        String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
        String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
        String[][] symbols = new String[5][];
        symbols[0] = symbolsPredicates;
        symbols[1] = symbolsMugs;
        symbols[2] = symbolsPlAreas;
        symbols[3] = symbolsManAreas;
        symbols[4] = symbolsPreAreas;
        FluentNetworkSolver groundSolver = new FluentNetworkSolver(0, 500, symbols, new int[] {1,1,1,1,1});
        return groundSolver;
    }

}
