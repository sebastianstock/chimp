package domains;

import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;

public class TestProblemParsing {

	public static void main(String[] args) {
		
//		ProblemParser pp = new ProblemParser("problems/test_fluent_scheduling.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl");
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_torso.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_pick_up_object.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_place_object.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arm_to_side.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arms_to_carryposture.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_observe_objects_on_area.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_torso.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_torso_assume_driving_pose0.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_torso_assume_driving_pose1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_0.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose0.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_0.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_3.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_4.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_5.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_2.pdl"); // PROBLEM uses Connected -> two options
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_3.pdl");  // Much backtracking
//		ProblemParser pp = new ProblemParser("problems/test_m_leave_manipulation_pose_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_grasp_object_w_arm_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_3.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_1a.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_3.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_2.pdl"); // 4 secs  // BUG: DOES NOT WORK
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_3.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_scenario_3_2_3.pdl");
		
		String[][] symbols = RACEProblemsSingle.createSymbols();
		int[] ingredients = RACEProblemsSingle.createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		PFD0Planner planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		initPlanner(planner, "domains/race_domain.ddl");
		
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
		
		plan(planner, fluentSolver);
		
		extractPlan(fluentSolver);
	}
	
	public static boolean plan(PFD0Planner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		return result;
	}
	
	public static void extractPlan(FluentNetworkSolver fluentSolver) {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	public static void initPlanner(PFD0Planner planner, String domainPath) {
		// load domain
		HybridDomain dom;
		try {
			dom = new HybridDomain(planner, domainPath);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// init meta constraints based on domain
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = dom.getFluentResourceUsages();
		selectionConstraint.setResourceUsages(fluentResourceUsages);
		planner.addMetaConstraint(selectionConstraint);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
	}
	



}
