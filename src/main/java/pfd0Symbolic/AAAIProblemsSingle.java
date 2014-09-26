package pfd0Symbolic;

import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class AAAIProblemsSingle {
	
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
	
	public static void createProblemMoveBase(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
		stateVars[0].setMarking(markings.OPEN);
		
		// task
		Fluent mbFluent = (Fluent) fluentSolver.createVariable("Task1");
		mbFluent.setName("!move_base(preManipulationAreaSouthTable1)");
		mbFluent.setMarking(markings.UNPLANNED);
		
		// task
		Fluent mbbFluent = (Fluent) fluentSolver.createVariable("Task2");
		mbbFluent.setName("!move_base_blind(manipulationAreaSouthTable1)");
		mbbFluent.setMarking(markings.UNPLANNED);
		
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1,1));
		release.setFrom(mbFluent);
		release.setTo(mbFluent);
		fluentSolver.addConstraint(release);
		AllenIntervalConstraint mbDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,20));
		mbDuration.setFrom(mbFluent);
		mbDuration.setTo(mbFluent);
		fluentSolver.addConstraint(mbDuration);
//		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(100,100));
//		deadline.setFrom(mbFluent);
//		deadline.setTo(mbFluent);
//		fluentSolver.addConstraint(deadline);
		AllenIntervalConstraint mbbDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,10));
		mbbDuration.setFrom(mbbFluent);
		mbbDuration.setTo(mbbFluent);
		fluentSolver.addConstraint(mbbDuration);
		
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(mbFluent);
		before.setTo(mbbFluent);
		fluentSolver.addConstraint(before);
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
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!pick_up_object(mug1 ?area ?manArea leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemMoveArmToSide(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 armUnnamedPosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 armCarryPosture)");
		
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
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 armToSidePosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 armToSidePosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!move_arms_to_carryposture(leftArm1 rightArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemTuckArms(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("HasArmPosture(leftArm1 armToSidePosture)");
		((Fluent) stateVars[1]).setName("HasArmPosture(rightArm1 armToSidePosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!tuck_arms(leftArm1 rightArm1 armTuckedPosture armUnTuckedPosture)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemMoveTorso(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("HasTorsoPosture(torsoUpPosture)");
		
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) fluentSolver.createVariable("Task1");
		taskFluent.setName("!move_torso(torsoDownPosture)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemDriveM(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(4);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
		((Fluent) stateVars[1]).setName("HasTorsoPosture(torsoUpPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 armToSidePosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 armToSidePosture)");
		
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
		((Fluent) stateVars[1]).setName("HasTorsoPosture(torsoUpPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 armToSidePosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 armToSidePosture)");
		
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
		((Fluent) stateVars[1]).setName("HasTorsoPosture(torsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 armTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 armTuckedPosture)");
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
		((Fluent) stateVars[1]).setName("HasTorsoPosture(torsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 armTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 armTuckedPosture)");
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
		((Fluent) stateVars[1]).setName("HasTorsoPosture(torsoDownPosture)");
		((Fluent) stateVars[2]).setName("HasArmPosture(leftArm1 armTuckedPosture)");
		((Fluent) stateVars[3]).setName("HasArmPosture(rightArm1 armTuckedPosture)");
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
