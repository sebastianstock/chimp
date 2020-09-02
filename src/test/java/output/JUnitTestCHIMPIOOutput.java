package output;

import examples.ChimpIO;
import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;
import picocli.CommandLine;
import planner.CHIMP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class JUnitTestCHIMPIOOutput {

    private String domainFile = "domains/ordered_domain.ddl";
    private String problemFile = "problems/test_m_drive_robot_1.pdl";

    public void testPlanning() {
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        try {
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
                    .htnUnification(true);
            builder.htnUnification(true);
            CHIMP chimp = builder.build();
            assertTrue(chimp.generatePlan());
        } catch (DomainParsingException e) {
            e.printStackTrace();
            assertFalse(true);
            return;
        }
    }

    @Test
    public void testCHIMPIOMain() {
        String planPath = "testing/ChimpIO_testplan.txt";
        String[] args = new String[] {domainFile, problemFile, "-o", planPath};
        new CommandLine(new ChimpIO()).execute(args);

        try {
            BufferedReader br = new BufferedReader(new FileReader(planPath));
            assertEquals("; Solution found", br.readLine());
            assertEquals("; Actions:", br.readLine());
            assertEquals("0.001: (!move_torso TorsoDownPosture) [4.000]", br.readLine());
            assertEquals("0.001: (!tuck_arms ArmTuckedPosture ArmTuckedPosture) [4.000]", br.readLine());
            assertEquals("4.002: (!move_base preManipulationAreaNorthTable1) [0.002]", br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
