package hybridDomainParsing;

import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.NewestFluentsValOH;
import htn.UnifyFewestsubsNewestbindingsValOH;
import htn.TaskApplicationMetaConstraint.markings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class TestProblemParsing {
	
	private static String N = "n";
	
	public static String[][] createSymbols() {
		String[][] symbols = new String[2][];
		// predicates  
		// index: 0
		symbols[0] = new String[] {"On", "RobotAt", "Holding", "HasArmPosture", "HasTorsoPosture",
				"Connected", "Type",
				// operators
				"!move_base", "!move_base_blind", "!place_object", "!pick_up_object",
				"!move_arm_to_side", "!move_arms_to_carryposture", "!tuck_arms", "!move_torso",
				"!observe_objects_on_area",
				// methods
				"adapt_torso", "torso_assume_driving_pose", "adapt_arms", "arms_assume_driving_pose",
				"drive_robot", "move_both_arms_to_side", "assume_manipulation_pose", 
				"leave_manipulation_pose", "grasp_object", "get_object", "put_object",
				"move_object", "serve_coffee_to_guest", 
				
				"serve_coffee_to_guest_test", "assume_manipulation_pose_wrapper",
				// other
				"Future"
				};	
		// race:Kitchenware		
		// index: 1, 2
		symbols[1] = new String[] {"mug1", "mug2",
				"nothing",
				"placingAreaEastRightCounter1",
				"placingAreaWestLeftTable1", "placingAreaWestRightTable1",
				"placingAreaEastLeftTable1", "placingAreaEastRightTable1",
				"placingAreaNorthLeftTable2", "placingAreaNorthRightTable2",
				"placingAreaSouthLeftTable2", "placingAreaSouthRightTable2",
				"trayArea1", 
				"manipulationAreaEastCounter1", "preManipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"preManipulationAreaNorthTable1", "preManipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",
				"preManipulationAreaWestTable2", "preManipulationAreaEastTable2",
				"floorAreaTamsRestaurant1", 
				"sittingAreaWestTable1", "sittingAreaEastTable1",
				"sittingAreaNorthTable2", "sittingConstraintSouthTable2",
				"table1", "table2", "counter1", 
				"guest1", "guest2",
				"leftArm1", "rightArm1", 
				"ArmTuckedPosture", "ArmUnTuckedPosture", "ArmToSidePosture", "ArmUnnamedPosture", "ArmCarryPosture",
				"TorsoUpPosture", "TorsoDownPosture", "TorsoMiddlePosture", 
				"coffeeJug1", "milkPot1", "sugarPot1", "sugarPot2",
				"Milk", "Coffee", "Sugar", "Mug", "Peppermill",
				N};
		return symbols;
	}
	
	public static int[] createIngredients() {
		return new int[] {1,5};
	}
	

	public static void main(String[] args) {
		
//		ProblemParser pp = new ProblemParser("problems/test_fluent_scheduling.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_torso.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_pick_up_object.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_place_object.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arm_to_side.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arms_to_carryposture.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_observe_objects_on_area.pdl");
		
		// new for debugging:
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose_debug1.pdl");
		
		// new for merge
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_torso_merge.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_m_test_merge.pdl"); 
//		ProblemParser pp = new ProblemParser("problems/test_m_merge_get_object.pdl"); 
		
		
		
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
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_2.pdl"); // 4 secs
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_3.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_scenario_3_2_3.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_m_serve_coffee_problem_1.pdl");
		ProblemParser pp = new ProblemParser("problems/test_m_serve_coffee_problem_2_fromtable.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_debug1.pdl");
		
		String[][] symbols = createSymbols();
		int[] ingredients = createIngredients();
		
		Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
		typesInstancesMap.put("ManipulationArea", new String[] {"manipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",});
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
//		initPlanner(planner, "domains/race_domain.ddl");
//		initPlanner(planner, "domains/s_domain.ddl");
		initPlanner(planner, "domains/ordered_domain.ddl");
//		initPlanner(planner, "domains/short_domain.ddl");
		
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
		
				
			MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
		
//		MetaCSPLogging.setLevel(Level.OFF);
		
		
		plan(planner, fluentSolver);
		
		Variable[] acts = fluentSolver.getVariables();
		ArrayList<Variable> plan = new ArrayList<Variable>();
		for (Variable var : acts) {
			if (var.getComponent() == null) {
				plan.add(var);
			}
			else if (var.getComponent().equals("Activity")) {
				plan.add(var);
			}
		}
		Variable[] planVector = plan.toArray(new Variable[plan.size()]);
		Arrays.sort(planVector, new Comparator<Variable>() {
			@Override
			public int compare(Variable o1, Variable o2) {
				// TODO Auto-generated method stub
				Fluent f1 = (Fluent)o1;
				Fluent f2 = (Fluent)o2;
				return ((int)f1.getTemporalVariable().getEST()-(int)f2.getTemporalVariable().getEST());
			}
		});
		
		int c = 0;
		for (Variable act : planVector) {
			System.out.println(c++ +".\t" + act);
		}
		
		extractPlan(fluentSolver);
	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		
		ConstraintNetwork cn = new ConstraintNetwork(null);
		for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
			if (con instanceof FluentConstraint) {
				FluentConstraint fc = (FluentConstraint) con;
				if (fc.getType() == FluentConstraint.Type.MATCHES) {
					fc.getFrom().setMarking(markings.SELECTED);
					cn.addConstraint(fc);
				} else if (fc.getType() == FluentConstraint.Type.DC) {
					cn.addConstraint(fc);
				}
			}
		}
		ConstraintNetwork.draw(cn);
		
		
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

//		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		
//		System.out.println("AGAIN");
//		startTime = System.nanoTime();
//		result = planner.backtrack();
//		System.out.println("Found a plan? " + result);
//		endTime = System.nanoTime();
//
//		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//		System.out.println("Finished");
		
		return result;
	}
	
	public static void extractPlan(FluentNetworkSolver fluentSolver) {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	public static void initPlanner(HTNPlanner planner, String domainPath) {
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
		ValueOrderingH valOH = new NewestFluentsValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
		
		HTNMetaConstraint selectionConstraint = new HTNMetaConstraint(valOH);
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = dom.getFluentResourceUsages();
		selectionConstraint.setResourceUsages(fluentResourceUsages);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		planner.addMetaConstraint(selectionConstraint);
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);

	}
	



}
