package examples.chimp;

import java.util.logging.Level;

import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestRACEDomain {

    static final boolean LOGGING = false;
    static final boolean GUESS_ORDERING = false;
    static final boolean PRINT_PLAN = true;
    static final boolean DRAW = false;


    public static void main(String[] args) {

//		String problemFile = "problems/test_op_tuck_arms.pdl"); // #1
//		String problemFile = "problems/test_op_move_base.pdl"); // #2
//		String problemFile = "problems/test_op_move_base_blind.pdl"); // #3
//		String problemFile = "problems/test_op_move_torso.pdl"); // #4
//		String problemFile = "problems/test_op_pick_up_object.pdl"); // #5
//		String problemFile = "problems/test_op_place_object.pdl"); // #6
//		String problemFile = "problems/test_op_move_arm_to_side.pdl"); // #7
//		String problemFile = "problems/test_op_move_arms_to_carryposture.pdl"); //#8
//		String problemFile = "problems/test_op_observe_objects_on_area.pdl"); //#9

//		String problemFile = "problems/test_m_adapt_torso_merge.pdl"; //#11
//		String problemFile = "problems/test_m_merge_get_object.pdl"; //#12

//		String problemFile = "problems/test_m_adapt_torso.pdl"; //#13
//		String problemFile = "problems/test_m_torso_assume_driving_pose0.pdl"; //#14
//		String problemFile = "problems/test_m_torso_assume_driving_pose1.pdl"; //#15
//		String problemFile = "problems/test_m_adapt_arms_0.pdl"; //#16
//		String problemFile = "problems/test_m_adapt_arms_1.pdl"; //#17
//		String problemFile = "problems/test_m_adapt_arms_2.pdl"; //#18
//		String problemFile = "problems/test_m_arms_assume_driving_pose0.pdl"; //#19
//		String problemFile = "problems/test_m_arms_assume_driving_pose1.pdl"; //#20
//		String problemFile = "problems/test_m_drive_robot_0.pdl"; //#21
//		String problemFile = "problems/test_m_drive_robot_1.pdl"); //#22
//		String problemFile = "problems/test_m_drive_robot_2.pdl"; //#23
//		String problemFile = "problems/test_m_move_both_arms_to_side_1.pdl"; //#24
//		String problemFile = "problems/test_m_move_both_arms_to_side_2.pdl"; //#25
//		String problemFile = "problems/test_m_move_both_arms_to_side_3.pdl"; //#26
//		String problemFile = "problems/test_m_move_both_arms_to_side_4.pdl"; //#27
//		String problemFile = "problems/test_m_move_both_arms_to_side_5.pdl"; //#28
//		String problemFile = "problems/test_m_assume_manipulation_pose_1.pdl"; //#29
//		String problemFile = "problems/test_m_assume_manipulation_pose_2.pdl"; //#30
//		String problemFile = "problems/test_m_assume_manipulation_pose_3.pdl";  //#31
//		String problemFile = "problems/test_m_leave_manipulation_pose_1.pdl"; //#32
//		String problemFile = "problems/test_m_grasp_object_w_arm_1.pdl"; //#33
//		String problemFile = "problems/test_m_get_object_w_arm_2.pdl"; //#34
//		String problemFile = "problems/test_m_get_object_w_arm_3.pdl"; //#35
//		String problemFile = "problems/test_m_put_object_1.pdl"; //#36
//		String problemFile = "problems/test_m_put_object_1a.pdl"; //#37
//		String problemFile = "problems/test_m_put_object_2.pdl"; //#38
//		String problemFile = "problems/test_m_put_object_3.pdl"; //#39
//		String problemFile = "problems/test_m_move_object_1.pdl"; //#40
//		String problemFile = "problems/test_m_move_object_2.pdl"; //#41
//		String problemFile = "problems/test_m_move_object_3.pdl"; //#42
//		String problemFile = "problems/test_scenario_3_2_3.pdl"; //#43

//		String problemFile = "problems/race_testing/move_multiple.pdl");
//		String problemFile = "problems/test_m_serve_coffee_problem_2_fromtable.pdl"); //#44
//		String problemFile = "problems/test_m_get_object_w_arm_debug1.pdl"); //#45
        // value ordering heuristic debugging:
//		String problemFile = "problems/debug_m_put_object1.pdl"); // very slow with UnifyFewestsubsNewestbindingsValOH

//		String problemFile = "problems/iros/iros_incremental_merging_initial.pdl");

        String problemFile = "problems/test_m_serve_coffee_problem_1.pdl"; // #0
        String domainFile = "domains/ordered_domain.ddl";

//		ValueOrderingH valOH = new UnifyEarlisttasksValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestWeightNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestFewestsubsNewestbindingsValOH();
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

        CHIMP.CHIMPBuilder builder;

        try {
            builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
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
            chimp.drawPlanHierarchy(100);
            chimp.drawHierarchyNetwork();
            chimp.drawSearchSpace();
        }

    }

}
