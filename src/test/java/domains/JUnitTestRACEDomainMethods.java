package domains;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestRACEDomainMethods {

    private String domainFile = "domains/ordered_domain.ddl";


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
            assertFalse(true);
            return;
        }
    }

    @Test
    public void testMAdaptTorso() {
        testPlanning("problems/test_m_adapt_torso.pdl");
    }

    @Test
    public void testMTorsoAssumeDrivingPose0() {
        testPlanning("problems/test_m_torso_assume_driving_pose0.pdl");
    }

    @Test
    public void testMTorsoAssumeDrivingPose1() {
        testPlanning("problems/test_m_torso_assume_driving_pose1.pdl");
    }

    @Test
    public void testMAdaptArms0() {
        testPlanning("problems/test_m_adapt_arms_0.pdl");
    }

    @Test
    public void testMAdaptArms1() {
        testPlanning("problems/test_m_adapt_arms_1.pdl");
    }

    @Test
    public void testMAdaptArms2() {
        testPlanning("problems/test_m_adapt_arms_2.pdl");
    }

    @Test
    public void testMArmsAssumeDrivingPose0() {
        testPlanning("problems/test_m_arms_assume_driving_pose0.pdl");
    }

    @Test
    public void testMArmsAssumeDrivingPose1() {
        testPlanning("problems/test_m_arms_assume_driving_pose1.pdl");
    }

    @Test
    public void testMDriveRobot0() {
        testPlanning("problems/test_m_drive_robot_0.pdl");
    }

    @Test
    public void testMDriveRobot1() {
        testPlanning("problems/test_m_drive_robot_1.pdl");
    }

    @Test
    public void testMDriveRobot2() {
        testPlanning("problems/test_m_drive_robot_2.pdl");
    }

    @Test
    public void testMMoveBothArmsToSide1() {
        testPlanning("problems/test_m_move_both_arms_to_side_1.pdl");
    }

    @Test
    public void testMMoveBothArmsToSide2() {
        testPlanning("problems/test_m_move_both_arms_to_side_2.pdl");
    }

    @Test
    public void testMMoveBothArmsToSide3() {
        testPlanning("problems/test_m_move_both_arms_to_side_3.pdl");
    }

    @Test
    public void testMMoveBothArmsToSide4() {
        testPlanning("problems/test_m_move_both_arms_to_side_4.pdl");
    }

    @Test
    public void testMMoveBothArmsToSide5() {
        testPlanning("problems/test_m_move_both_arms_to_side_5.pdl");
    }

    @Test
    public void testMAssumeManipulationPose1() {
        testPlanning("problems/test_m_assume_manipulation_pose_1.pdl");
    }

    @Test
    public void testMAssumeManipulationPose2() {
        testPlanning("problems/test_m_assume_manipulation_pose_2.pdl");
    }

    @Test
    public void testMAssumeManipulationPose3() {
        testPlanning("problems/test_m_assume_manipulation_pose_3.pdl");
    }

    @Test
    public void testMLeaveManipulationPose1() {
        testPlanning("problems/test_m_leave_manipulation_pose_1.pdl");
    }

    @Test
    public void testMGraspObjectWArm1() {
        testPlanning("problems/test_m_grasp_object_w_arm_1.pdl");
    }

    @Test
    public void testMGetObjectWArm2() {
        testPlanning("problems/test_m_get_object_w_arm_2.pdl");
    }

    @Test
    public void testMGetObjectWArm3() {
        testPlanning("problems/test_m_get_object_w_arm_3.pdl");
    }

    @Test
    public void testMPutObject1() {
        testPlanning("problems/test_m_put_object_1.pdl");
    }

    @Test
    public void testMPutObject1a() {
        testPlanning("problems/test_m_put_object_1a.pdl");
    }

    @Test
    public void testMPutObject2() {
        testPlanning("problems/test_m_put_object_2.pdl");
    }

    @Test
    public void testMPutObject3() {
        testPlanning("problems/test_m_put_object_3.pdl");
    }

    @Test
    public void testMMoveObject1() {
        testPlanning("problems/test_m_move_object_1.pdl");
    }

    @Test
    public void testMMoveObject2() {
        testPlanning("problems/test_m_move_object_2.pdl");
    }

    @Test
    public void testMMoveObject3() {
        testPlanning("problems/test_m_move_object_3.pdl");
    }

    @Test
    public void testScenario_3_2_3() {
        testPlanning("problems/test_scenario_3_2_3.pdl");
    }

}
