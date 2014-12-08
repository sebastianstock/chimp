package domains;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import pfd0Symbolic.Fluent;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class RACEProblemsSingle {
	
	private static String N = "n";
	
	public static String[][] createSymbols() {
		String[][] symbols = new String[2][];
		// predicates  
		// index: 0
		symbols[0] = new String[] {"On", "RobotAt", "Holding", "HasArmPosture", "HasTorsoPosture",
				"Connected",
				// operators
				"!move_base", "!move_base_blind", "!place_object", "!pick_up_object",
				"!move_arm_to_side", "!move_arms_to_carryposture", "!tuck_arms", "!move_torso",
				// methods
				"drive", "assume_default_driving_pose", "assume_manipulation_pose",
				"move_both_arms_to_side", "grasp_object_w_arm", "get_object_w_arm"
				};	
		// race:Kitchenware		
		// index: 1, 2
		symbols[1] = new String[] {"mug1", "mug2", "sugarpot1", "milk1", "placingAreaEastRightCounter1",
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
				N};
		return symbols;
	}
	
	public static int[] createIngredients() {
		return new int[] {1,5};
	}
	
	// MOVE_BASE
	public static void createProblemMoveBase(FluentNetworkSolver fluentSolver) {

		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
//		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaSouthTable1)");
		stateVars[0].setMarking(markings.OPEN);
		
		// task
		Fluent mbFluent = (Fluent) fluentSolver.createVariable("Task1");
		mbFluent.setName("!move_base(preManipulationAreaSouthTable1)");
		mbFluent.setMarking(markings.UNPLANNED);
		
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1,1));
		release.setFrom(mbFluent);
		release.setTo(mbFluent);
		fluentSolver.addConstraint(release);
//		AllenIntervalConstraint mbDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,20));
//		mbDuration.setFrom(mbFluent);
//		mbDuration.setTo(mbFluent);
//		fluentSolver.addConstraint(mbDuration);
	}
	
		// MOVE_BASE
	public static void createProblemMoveBaseBlind(FluentNetworkSolver fluentSolver) {

		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
		stateVars[0].setMarking(markings.OPEN);
		
		// task
		Fluent mbFluent = (Fluent) fluentSolver.createVariable("Task1");
		mbFluent.setName("!move_base_blind(manipulationAreaEastCounter1)");
		mbFluent.setMarking(markings.UNPLANNED);
		
		// task
		Fluent mbFluentBack = (Fluent) fluentSolver.createVariable("Task2");
		mbFluentBack.setName("!move_base_blind(preManipulationAreaEastCounter1)");
		mbFluentBack.setMarking(markings.UNPLANNED);
		
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1,1));
		release.setFrom(mbFluent);
		release.setTo(mbFluent);
		fluentSolver.addConstraint(release);
		createConnectedAreas(fluentSolver);
	}
	
	// TUCK_ARMS
	public static void createProblemTuckArms(FluentNetworkSolver fluentSolver) {
		Variable[] stateVars = fluentSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 ArmToSidePosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 ArmToSidePosture)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		setRelease(stateVars, fluentSolver, 0, 0);
		setDuration(stateVars, fluentSolver, 1, 100000);

		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!tuck_arms(ArmTuckedPosture ArmUnTuckedPosture)");
		taskFluent.setMarking(markings.UNPLANNED);
		setRelease(new Variable[] {taskFluent}, fluentSolver, 1, 1);
		
//		Fluent taskFluent2 = (Fluent) fluentSolver.createVariable("Task1");
//		taskFluent2.setName("!tuck_arms(armUnTuckedPosture armTuckedPosture)");
//		taskFluent2.setMarking(markings.UNPLANNED);
	}

	
	private static void createConnectedAreas(FluentNetworkSolver fluentSolver) {
		Variable[] stateVars = fluentSolver.createVariables(9);
		((Fluent) stateVars[0]).setName("Connected(placingAreaEastRightCounter1 manipulationAreaEastCounter1 preManipulationAreaEastCounter1)");
		((Fluent) stateVars[1]).setName("Connected(placingAreaWestLeftTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[2]).setName("Connected(placingAreaEastLeftTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[3]).setName("Connected(placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[4]).setName("Connected(placingAreaEastRightTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[5]).setName("Connected(placingAreaNorthLeftTable2 manipulationAreaEastTable2  preManipulationAreaEastTable2)");
		((Fluent) stateVars[6]).setName("Connected(placingAreaNorthRightTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[7]).setName("Connected(placingAreaSouthLeftTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[8]).setName("Connected(placingAreaSouthRightTable2 manipulationAreaEastTable2 preManipulationAreaEastTable2)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
	}
	
	///////////////////////////////////////
	
	public static void setDuration(Variable[] vars, FluentNetworkSolver fluentSolver, long min, long max) {
		for (Variable var : vars) {
			setDuration(var, fluentSolver, min, max);
		}
	}
	
	public static void setDuration(Variable var, FluentNetworkSolver fluentSolver, long min, long max) {
		AllenIntervalConstraint duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(min,max));
		duration.setFrom(var);
		duration.setTo(var);
		fluentSolver.addConstraint(duration);
	}
	
	public static void setRelease(Variable[] vars, FluentNetworkSolver fluentSolver, long min, long max) {
		for (Variable var : vars) {
			setRelease(var, fluentSolver, min, max);
		}
	}
	
	public static void setRelease(Variable var, FluentNetworkSolver fluentSolver, long min, long max) {
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(min, max));
		release.setFrom(var);
		release.setTo(var);
		fluentSolver.addConstraint(release);
	}
	
	public static void setDeadline(Variable[] vars, FluentNetworkSolver solver, long min, long max) {
		for (Variable var : vars) {
			setDeadline(var, solver, min, max);
		}
	}
	
	public static void setDeadline(Variable var, FluentNetworkSolver solver, long min, long max) {
		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(min, max));
		deadline.setFrom(var);
		deadline.setTo(var);
		solver.addConstraint(deadline);
	}
	
	public static void createProblem(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("On(mug1 pl1 counter1)");
		((Fluent) stateVars[1]).setName("On(mug2 pl1 counter1)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("put_mugs_on_tray(mug1 mug2 trayArea1 none none)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	
	
	public static void createProblemPlaceObject(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(11);
		((Fluent) stateVars[0]).setName("Holding(mug1 leftArm1)");
		
		((Fluent) stateVars[1]).setName("RobotAt(manipulationAreaSouthTable1)");
		
		((Fluent) stateVars[2]).setName("Connected(placingAreaEastRightCounter1 manipulationAreaEastCounter1 preManipulationAreaEastCounter1)");
		((Fluent) stateVars[3]).setName("Connected(placingAreaWestLeftTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[4]).setName("Connected(placingAreaEastLeftTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[5]).setName("Connected(placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[6]).setName("Connected(placingAreaEastRightTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[7]).setName("Connected(placingAreaNorthLeftTable2 manipulationAreaEastTable2  preManipulationAreaEastTable2)");
		((Fluent) stateVars[8]).setName("Connected(placingAreaNorthRightTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[9]).setName("Connected(placingAreaSouthLeftTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[10]).setName("Connected(placingAreaSouthRightTable2 manipulationAreaEastTable2 preManipulationAreaEastTable2)");

		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!place_object(mug1 placingAreaWestRightTable1 manipulationAreaSouthTable1 leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemPickUpObject(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(11);
		((Fluent) stateVars[0]).setName("On(mug1 placingAreaWestRightTable1)");
		
		((Fluent) stateVars[1]).setName("RobotAt(manipulationAreaSouthTable1)");
		
		((Fluent) stateVars[2]).setName("Connected(placingAreaEastRightCounter1 manipulationAreaEastCounter1 preManipulationAreaEastCounter1)");
		((Fluent) stateVars[3]).setName("Connected(placingAreaWestLeftTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[4]).setName("Connected(placingAreaEastLeftTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[5]).setName("Connected(placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[6]).setName("Connected(placingAreaEastRightTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[7]).setName("Connected(placingAreaNorthLeftTable2 manipulationAreaEastTable2  preManipulationAreaEastTable2)");
		((Fluent) stateVars[8]).setName("Connected(placingAreaNorthRightTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[9]).setName("Connected(placingAreaSouthLeftTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[10]).setName("Connected(placingAreaSouthRightTable2 manipulationAreaEastTable2 preManipulationAreaEastTable2)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		setRelease(stateVars, groundSolver, 0, 0);
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!pick_up_object(mug1 ?area ?manArea leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemMoveArmToSide(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 ArmUnnamedPosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 ArmCarryPosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!move_arm_to_side(leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemMoveArmsToCarryposture(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 ArmToSidePosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 ArmToSidePosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!move_arms_to_carryposture(leftArm1 rightArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	

	
	public static void createProblemMoveTorso(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("HasTorsoPosture(TorsoUpPosture)");
		AllenIntervalConstraint torsoRelCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(0, 0));
		torsoRelCon.setFrom(stateVars[0]);
		torsoRelCon.setTo(stateVars[0]);
		fluentSolver.addConstraint(torsoRelCon);
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!move_torso(TorsoDownPosture)");
		taskFluent.setMarking(markings.UNPLANNED);
		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(40, 50));
		deadline.setFrom(taskFluent);
		deadline.setTo(taskFluent);
		fluentSolver.addConstraint(deadline);
	}
	
	public static void createProblemDriveM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(4);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(TorsoUpPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 ArmToSidePosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 ArmToSidePosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("drive(preManipulationAreaSouthTable1)");
//		taskFluent.setName("assume_default_driving_pose()");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemAssumeDefaultDrivingPoseM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(4);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(TorsoUpPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 ArmToSidePosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 ArmToSidePosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("assume_default_driving_pose()");
		//		taskFluent.setName("assume_default_driving_pose()");
		//Says that start time of from (and to) is at least 10 and at most 12
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(0,0));
		release.setFrom(taskFluent);
		release.setTo(taskFluent);
		fluentSolver.addConstraint(release);
		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(100,100));
		deadline.setFrom(taskFluent);
		deadline.setTo(taskFluent);
		fluentSolver.addConstraint(deadline);
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemAssumeManipulationPoseM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		createStaticKnowledge(fluentSolver);
		
		Variable[] stateVars = fluentSolver.createVariables(5);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaSouthTable1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(TorsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 ArmTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 ArmTuckedPosture)");
		((Fluent) stateVars[4]).setName("On(mug1 placingAreaWestRightTable1)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("assume_manipulation_pose(manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		// the following alternative takes forever
//		taskFluent.setName("assume_manipulation_pose(manipulationAreaSouthTable1 ?preManipulationAreaSouthTable1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemGraspM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		createStaticKnowledge(fluentSolver);
		
		Variable[] stateVars = fluentSolver.createVariables(5);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaSouthTable1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(TorsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 ArmTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 ArmTuckedPosture)");
		((Fluent) stateVars[4]).setName("On(mug1 placingAreaWestRightTable1)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("grasp_object_w_arm(mug1 placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1 leftArm1)");
		// the following alternative takes forever
//		taskFluent.setName("assume_manipulation_pose(manipulationAreaSouthTable1 ?preManipulationAreaSouthTable1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemGetObjectWithArmM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		createStaticKnowledge(fluentSolver);
		
		Variable[] stateVars = fluentSolver.createVariables(5);
		((Fluent) stateVars[0]).setName("RobotAt(floorAreaTamsRestaurant1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(TorsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 ArmTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 ArmTuckedPosture)");
		((Fluent) stateVars[4]).setName("On(mug1 placingAreaWestRightTable1)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("get_object_w_arm(mug1 placingAreaWestRightTable1 preManipulationAreaSouthTable1 leftArm1)");
		// the following alternative takes forever
//		taskFluent.setName("assume_manipulation_pose(manipulationAreaSouthTable1 ?preManipulationAreaSouthTable1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createStaticKnowledge(FluentNetworkSolver fluentSolver) {
		Variable[] stateVars = fluentSolver.createVariables(9);
		((Fluent) stateVars[0]).setName("Connected(placingAreaEastRightCounter1 manipulationAreaEastCounter1 preManipulationAreaEastCounter1)");
		((Fluent) stateVars[1]).setName("Connected(placingAreaWestLeftTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[2]).setName("Connected(placingAreaEastLeftTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[3]).setName("Connected(placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[4]).setName("Connected(placingAreaEastRightTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[5]).setName("Connected(placingAreaNorthLeftTable2 manipulationAreaEastTable2  preManipulationAreaEastTable2)");
		((Fluent) stateVars[6]).setName("Connected(placingAreaNorthRightTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[7]).setName("Connected(placingAreaSouthLeftTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[8]).setName("Connected(placingAreaSouthRightTable2 manipulationAreaEastTable2 preManipulationAreaEastTable2)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
	}

	
}
