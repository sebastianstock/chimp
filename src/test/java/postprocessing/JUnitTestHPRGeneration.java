package postprocessing;

import org.junit.Test;
import planner.CHIMP;
import org.metacsp.framework.ValueOrderingH;
import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class JUnitTestHPRGeneration {

    String problemFile = "domains/mobipick/simple_navigation/problem.pdl";
    String domainFile = "domains/mobipick/simple_navigation/domain.ddl";

    @Test
    public void testPlanning() {
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        try {
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator());
            CHIMP chimp = builder.build();
            assertTrue(chimp.generatePlan());
            chimp.printStats(System.out);
            StringWriter sw = new StringWriter();
            HPRGenerator hprGenerator = new HPRGenerator(chimp, sw);
            hprGenerator.generateHPR();
            // TODO check output
        } catch (DomainParsingException e) {
            e.printStackTrace();
            assertFalse(true);
            return;
        }
    }
    
}
