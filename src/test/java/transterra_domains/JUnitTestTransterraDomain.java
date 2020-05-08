package transterra_domains;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestTransterraDomain {

    private String domainFile = "domains/transterra_v1.ddl";

    private void testPlanning(String problemFile) {
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
            return;
        }
    }


    @Test
    public void testOpMoveTo() {
        testPlanning("problems/transterra_problems_v1/test_op_move_to.pdl");
    }

    @Test
    public void testOpSample() {
        testPlanning("problems/transterra_problems_v1/test_op_sample_regolith.pdl");
    }

    @Test
    public void testOpTransferSample() {
        testPlanning("problems/transterra_problems_v1/test_op_transfer_sample.pdl");
    }

    @Test
    public void testOpTransferPayload() {
        testPlanning("problems/transterra_problems_v1/test_op_transfer_payload.pdl");
    }

    @Test
    public void testOpPickupBasecamp() {
        testPlanning("problems/transterra_problems_v1/test_op_pickup_basecamp.pdl");
    }

    @Test
    public void testOpPickupBasecamp2() {
        testPlanning("problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl");
    }

    @Test
    public void testOpPlaceBasecamp() {
        testPlanning("problems/transterra_problems_v1/test_op_place_basecamp.pdl");
    }

    @Test
    public void testMDeployBasecamp1() {
        testPlanning("problems/transterra_problems_v1/test_m_deploy_basecamp_1.pdl");
    }

    @Test
    public void testMDeployBasecamp2() {
        testPlanning("problems/transterra_problems_v1/test_m_deploy_basecamp_2.pdl");
    }

    @Test
    public void testMTakeSamples1() {
        testPlanning("problems/transterra_problems_v1/test_m_take_samples_1.pdl");
    }

    @Test
    public void testMTakeSamples2() {
        testPlanning("problems/transterra_problems_v1/test_m_take_samples_2.pdl");
    }

    @Test
    public void testMGetBasecamp1() {
        testPlanning("problems/transterra_problems_v1/test_m_get_basecamp_1.pdl");
    }

    @Test
    public void testMGetBasecamp2() {
        testPlanning("problems/transterra_problems_v1/test_m_get_basecamp_2.pdl");
    }

    @Test
    public void testMTransferFilled() {
        testPlanning("problems/transterra_problems_v1/test_m_transfer_filled.pdl");
    }
}
