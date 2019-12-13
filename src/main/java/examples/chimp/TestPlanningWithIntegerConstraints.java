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
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;
import planner.CHIMP;
import planner.CHIMPProblem;

import java.io.IOException;
import java.util.logging.Level;

public class TestPlanningWithIntegerConstraints {

    public static void main(String[] args) {

        String problemFile = "problems/simple_integer_problem.pdl";
        String domainFile = "domains/simple_integer_domain.ddl";
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

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

            MetaCSPLogging.setLevel(HTNPlanner.class, Level.FINEST);
            MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

            System.out.println("Found plan? " + chimp.generatePlan());
            chimp.printStats(System.out);

            Variable[] planVector = chimp.extractActions();
            int c = 0;
            for (Variable act : planVector) {
                if (act.getComponent() != null)
                    System.out.println(c++ + ".\t" + act);
            }

            chimp.printFullPlan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
