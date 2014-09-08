package pfd0Symbolic;

import org.metacsp.framework.Variable;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class AAAIProblems {
	
	public static void createProblem(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(2);
		((Fluent) stateVars[0]).setName("On(mug1 n pl1 n n counter1 n n n n n)");
		((Fluent) stateVars[1]).setName("On(mug2 n pl1 n n counter1 n n n n n)");
		
		for(Variable v : stateVars) {
			v.setMarking(markings.OPEN);
		}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("put_mugs_on_tray(mug1 mug2 trayArea1 none none)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemMoveBase(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("RobotAt(n n n preManipulationAreaEastCounter1 n n n n n n n)");
		stateVars[0].setMarking(markings.OPEN);
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!move_base(n n n preManipulationAreaSouthTable1 n n n n n n n)");
		taskFluent.setMarking(markings.UNPLANNED);
		
		// task
		Fluent mbbFluent = (Fluent) groundSolver.createVariable("Task2");
		mbbFluent.setName("!move_base_blind(n n n manipulationAreaSouthTable1 n n n n n n n)");
		mbbFluent.setMarking(markings.UNPLANNED);
	}
	
	public static void createProblemPlaceObject(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(11);
		((Fluent) stateVars[0]).setName("Holding(mug1 n n n n n n leftArm1 n n n)");
		
		((Fluent) stateVars[1]).setName("RobotAt(n n n manipulationAreaSouthTable1 n n n n n n n)");
		
		((Fluent) stateVars[2]).setName("Connected(n n placingAreaEastRightCounter1 manipulationAreaEastCounter1 n n n n n n n)");
		((Fluent) stateVars[3]).setName("Connected(n n placingAreaWestLeftTable1 manipulationAreaNorthTable1 n n n n n n n)");
		((Fluent) stateVars[4]).setName("Connected(n n placingAreaEastLeftTable1 manipulationAreaSouthTable1 n n n n n n n)");
		((Fluent) stateVars[5]).setName("Connected(n n placingAreaWestRightTable1 manipulationAreaSouthTable1 n n n n n n n)");
		((Fluent) stateVars[6]).setName("Connected(n n placingAreaEastRightTable1 manipulationAreaNorthTable1 n n n n n n n)");
		((Fluent) stateVars[7]).setName("Connected(n n placingAreaNorthLeftTable2 manipulationAreaEastTable2 n n n n n n n)");
		((Fluent) stateVars[8]).setName("Connected(n n placingAreaNorthRightTable2 manipulationAreaWestTable2 n n n n n n n)");
		((Fluent) stateVars[9]).setName("Connected(n n placingAreaSouthLeftTable2 manipulationAreaWestTable2 n n n n n n n)");
		((Fluent) stateVars[10]).setName("Connected(n n placingAreaSouthRightTable2 manipulationAreaEastTable2 n n n n n n n)");

		
		for(Variable v : stateVars) {
			v.setMarking(markings.OPEN);
		}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!place_object(mug1 n placingAreaWestRightTable1 manipulationAreaSouthTable1 n n n leftArm1 n n n)");
		taskFluent.setMarking(markings.UNPLANNED);
	}
	
	
}
