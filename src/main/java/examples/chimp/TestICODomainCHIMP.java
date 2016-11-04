package examples.chimp;

import java.util.logging.Level;

import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimatorICO;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestICODomainCHIMP {
	

	public static void main(String[] args) {
		
		// testing:
//		String problemFile = "problems/ico/testing/test_op_move_base.pdl";
//		String problemFile = "problems/ico/testing/test_op_move_torso.pdl";
//		String problemFile = "problems/ico/testing/test_op_pick_up_object.pdl";
//		String problemFile = "problems/ico/testing/test_op_place_object.pdl";
//		String problemFile = "problems/ico/testing/test_op_move_arm.pdl";
//		String problemFile = "problems/ico/testing/test_op_observe_area.pdl";
//		String problemFile = "problems/ico/testing/test_m_adapt_torso1.pdl";
//		String problemFile = "problems/ico/testing/test_m_adapt_torso2.pdl";
//		String problemFile = "problems/ico/testing/test_m_adapt_arm1.pdl";
//		String problemFile = "problems/ico/testing/test_m_adapt_arm2.pdl";
//		String problemFile = "problems/ico/testing/test_m_drive1.pdl";
//		String problemFile = "problems/ico/testing/test_m_drive2.pdl";
//		String problemFile = "problems/ico/testing/test_m_assume_driving_pose1.pdl";
//		String problemFile = "problems/ico/testing/test_m_assume_driving_pose2.pdl";
//		String problemFile = "problems/ico/testing/test_m_assume_manipulation_pose1.pdl";
//		String problemFile = "problems/ico/testing/test_m_get_object1.pdl";
//		String problemFile = "problems/ico/testing/test_m_put_object1.pdl";
//		String problemFile = "problems/ico/testing/test_m_move_object1.pdl";

//		String problemFile = "problems/ico/test_m_serve_coffee_problem_1.pdl";
		String problemFile = "problems/ico/move_2_objects.pdl";

		String domainFile = "domains/ico.ddl";
		
		CHIMP chimp;
		try {
			chimp = new CHIMP.CHIMPBuilder(domainFile, problemFile)
					.mbEstimator(new LookUpTableDurationEstimatorICO())
					.valHeuristic(new UnifyFewestsubsEarliesttasksNewestbindingsValOH())
					.build();
					
		} catch (DomainParsingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
//		MetaCSPLogging.setLevel(chimp.getClass(), Level.FINEST);		
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
		MetaCSPLogging.setLevel(Level.OFF);
		
		System.out.println("Found plan? " + chimp.generatePlan());
		chimp.printStats(System.out);

		Variable[] planVector = chimp.extractActions();
		int c = 0;
		for (Variable act : planVector) {
			if (act.getComponent() != null)
				System.out.println(c++ +".\t" + act);	
		}
		
		chimp.printFullPlan();
		
	}


}
