package examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.TaskApplicationMetaConstraint.markings;
import htn.UnifyFewestsubsNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestRACEDomain {
	

	public static void main(String[] args) {
		
//		ProblemParser pp = new ProblemParser("problems/test_fluent_scheduling.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl"); // #1
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl"); // #2
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl"); // #3
//		ProblemParser pp = new ProblemParser("problems/test_op_move_torso.pdl"); // #4
//		ProblemParser pp = new ProblemParser("problems/test_op_pick_up_object.pdl"); // #5
//		ProblemParser pp = new ProblemParser("problems/test_op_place_object.pdl"); // #6
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arm_to_side.pdl"); // #7
//		ProblemParser pp = new ProblemParser("problems/test_op_move_arms_to_carryposture.pdl"); //#8
//		ProblemParser pp = new ProblemParser("problems/test_op_observe_objects_on_area.pdl"); //#9
		
		// new for debugging:
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose_debug1.pdl"); //#10
		
		// new for merge
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_torso_merge.pdl"); //#11
//		ProblemParser pp = new ProblemParser("problems/test_m_merge_get_object.pdl"); //#12 
		
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_torso.pdl"); //#13
//		ProblemParser pp = new ProblemParser("problems/test_m_torso_assume_driving_pose0.pdl"); //#14
//		ProblemParser pp = new ProblemParser("problems/test_m_torso_assume_driving_pose1.pdl"); //#15
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_0.pdl"); //#16
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_1.pdl"); //#17
//		ProblemParser pp = new ProblemParser("problems/test_m_adapt_arms_2.pdl"); //#18
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose0.pdl"); //#19
//		ProblemParser pp = new ProblemParser("problems/test_m_arms_assume_driving_pose1.pdl"); //#20
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_0.pdl"); //#21
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_1.pdl"); //#22
//		ProblemParser pp = new ProblemParser("problems/test_m_drive_robot_2.pdl"); //#23
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_1.pdl"); //#24
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_2.pdl"); //#25
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_3.pdl"); //#26
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_4.pdl"); //#27
//		ProblemParser pp = new ProblemParser("problems/test_m_move_both_arms_to_side_5.pdl"); //#28
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_1.pdl"); //#29
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_2.pdl"); //#30 // PROBLEM uses Connected -> two options
//		ProblemParser pp = new ProblemParser("problems/test_m_assume_manipulation_pose_3.pdl");  //#31 // Much backtracking
//		ProblemParser pp = new ProblemParser("problems/test_m_leave_manipulation_pose_1.pdl"); //#32
//		ProblemParser pp = new ProblemParser("problems/test_m_grasp_object_w_arm_1.pdl"); //#33
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_2.pdl"); //#34
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_3.pdl"); //#35
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_1.pdl"); //#36
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_1a.pdl"); //#37
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_2.pdl"); //#38
//		ProblemParser pp = new ProblemParser("problems/test_m_put_object_3.pdl"); //#39
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_1.pdl"); //#40
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_2.pdl"); //#41
//		ProblemParser pp = new ProblemParser("problems/test_m_move_object_3.pdl"); //#42
//		ProblemParser pp = new ProblemParser("problems/test_scenario_3_2_3.pdl"); //#43
		
		ProblemParser pp = new ProblemParser("problems/test_m_serve_coffee_problem_1.pdl"); // #0
//		ProblemParser pp = new ProblemParser("problems/test_m_serve_coffee_problem_2_fromtable.pdl"); //#44
//		
//		ProblemParser pp = new ProblemParser("problems/test_m_get_object_w_arm_debug1.pdl"); //#45
		
		// value ordering heuristic debugging:
//		ProblemParser pp = new ProblemParser("problems/debug_m_put_object1.pdl"); // very slow with UnifyFewestsubsNewestbindingsValOH
		
	// IROS
//		ProblemParser pp = new ProblemParser("problems/iros/iros_incremental_merging_initial.pdl");
		
//		ProblemParser pp = new ProblemParser("tmp/iros_full.pdl"); // #0
		
		HybridDomain domain;
		try {
			domain = new HybridDomain("domains/ordered_domain.ddl");
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		int[] ingredients = new int[] {1, domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
		symbols[1] = pp.getArgumentSymbols();
		Map<String, String[]> typesInstancesMap = pp.getTypesInstancesMap();
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		
		try {
			initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);		
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
//		planner.createInitialMeetsFutureConstraints();
		plan(planner, fluentSolver);
		
		Variable[] allFluents = fluentSolver.getVariables();
		ArrayList<Variable> plan = new ArrayList<Variable>();
		int opCount = 0;
		int mCount = 0;
		for (Variable var : allFluents) {
			String component = var.getComponent();
			if (component == null) {
				plan.add(var);
			}
			else if (component.equals("Activity")) {
				plan.add(var);
				opCount++;
			} else if (component.equals("Task")) {
				mCount++;
			}
		}
		System.out.println("#Ops: " + opCount);
		System.out.println("#Compound Tasks: " + mCount);
		System.out.println("#Fluents: " + fluentSolver.getVariables().length);
		System.out.println("FluentConstraints: " + fluentSolver.getConstraints().length);

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
			if (act.getComponent() != null)
				System.out.println(c++ +".\t" + act);	
		}
		
		extractPlan(fluentSolver);
		
		////////////////
		
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
					fc.getFrom().setMarking(markings.UNIFIED);
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
	
	
	public static void initPlanner(HTNPlanner planner, HybridDomain domain) throws DomainParsingException {
		// load domain
		domain.parseDomain(planner);
		
		// init meta constraints based on domain
//		ValueOrderingH valOH = new NewestFluentsValOH();
		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
		
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.enableUnification();
		htnConstraint.addOperators(domain.getOperators());
		htnConstraint.addMethods(domain.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = domain.getFluentResourceUsages();
		htnConstraint.setResourceUsages(fluentResourceUsages);
		
		
		for (FluentScheduler fs : domain.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : domain.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
		
		planner.addMetaConstraint(htnConstraint);
	}
	



}
