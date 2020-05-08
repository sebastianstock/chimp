package domains;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestRACEDomain {

    private String domainFile = "domains/ordered_domain.ddl";

    public void testPlanning(String problemFile) {
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
    public void testOpTuckArms() {
        testPlanning("problems/test_op_tuck_arms.pdl");
    }

    @Test
    public void testOpMoveBase() {
        testPlanning("problems/test_op_move_base.pdl");
    }

    @Test
    public void testOpMoveTorso() {
        testPlanning("problems/test_op_move_torso.pdl");
    }

    @Test
    public void testOpMoveBaseBlind() {
        testPlanning("problems/test_op_move_base_blind.pdl");
    }

    @Test
    public void testOpPickUpObject() {
        testPlanning("problems/test_op_pick_up_object.pdl");
    }

    @Test
    public void testOpPlaceObject() {
        testPlanning("problems/test_op_place_object.pdl");
    }

    @Test
    public void testOpMoveArmToSide() {
        testPlanning("problems/test_op_move_arm_to_side.pdl");
    }

    @Test
    public void testMoveArmsToCarryposture() {
        testPlanning("problems/test_op_move_arms_to_carryposture.pdl");
    }

    @Test
    public void testOpObserveObjectsOnArea() {
        testPlanning("problems/test_op_observe_objects_on_area.pdl");
    }

}
