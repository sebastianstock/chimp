package examples.chimp;

import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.classic.antlr.ChimpClassicLexer;
import hybridDomainParsing.classic.antlr.ChimpClassicParser;
import hybridDomainParsing.classic.antlr.ChimpClassicReader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import htn.valOrderingHeuristics.CHIMPValOH;
import chimp.logging.ChimpLogging;
import planner.CHIMP;
import planner.CHIMPProblem;

import java.io.IOException;
import java.util.logging.Level;
import chimp.model.PlannedAction;

public class TestPlanningWithIntegerConstraints {

    public static void main(String[] args) {

        String problemFile = "problems/simple_integer_problem.pdl";
        String domainFile = "domains/simple_integer_domain.ddl";
        CHIMPValOH valOH = new UnifyDeepestWeightNewestbindingsValOH();

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

//            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domain, problem)
                    .valHeuristic(valOH)
                    .htnUnification(true);
            CHIMP chimp = builder.build();

            ChimpLogging.setLevel(HTNPlanner.class, Level.FINEST);
            ChimpLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

            System.out.println("Found plan? " + chimp.generatePlan());
            chimp.printStats(System.out);

            PlannedAction[] planVector = chimp.extractActions();
            int c = 0;
            for (PlannedAction act : planVector) {
                if (act.component() != null)
                    System.out.println(c++ + ".\t" + act);
            }

            chimp.printFullPlan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
