package postprocessing;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;
import planner.CHIMP;

import java.io.BufferedWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class JUnitTestEsterelGeneration {

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
            EsterelGenerator.generateEsterelGraph(chimp, sw);
            // TODO check output
        } catch (DomainParsingException e) {
            e.printStackTrace();
            assertFalse(true);
            return;
        }
    }

//    @Test
//    public void testCHIMPIOMain() {
//        String planPath = "testing/ChimpIO_testplan.txt";
//        String[] args = new String[] {domainFile, problemFile, "-o", planPath};
//        new CommandLine(new ChimpIO()).execute(args);
//
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(planPath));
//            assertEquals("; Solution found", br.readLine());
//            assertEquals("; Actions:", br.readLine());
//            assertEquals("0.001: (!move_torso TorsoDownPosture) [4.000]", br.readLine());
//            assertEquals("0.001: (!tuck_arms ArmTuckedPosture ArmTuckedPosture) [4.000]", br.readLine());
//            assertEquals("4.002: (!move_base preManipulationAreaNorthTable1) [0.002]", br.readLine());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}
