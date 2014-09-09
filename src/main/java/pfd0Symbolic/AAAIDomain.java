package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.VariablePrototype;

public class AAAIDomain {
	
	private static String N = "n";
	
	
	public static String[][] createSymbols() {
		String[][] symbols = new String[8][];
		// predicates  
		// index: 0
		symbols[0] = new String[] {"On", "RobotAt", "Holding", "HasArmPosture",
				"Connected",
				"!move_base", "!move_base_blind", "!place_object", "!pick_up_object",
				"!move_arm_to_side", "!move_arms_to_carryposture", "!tuck_arms"};	
		// race:Kitchenware		
		// index: 1, 2
		symbols[1] = new String[] {"mug1", "mug2", "sugarpot1", "milk1", N};
		// race:PlacingArea
		// index: 3
		symbols[2] = new String[] {"placingAreaEastRightCounter1",
				"placingAreaWestLeftTable1", "placingAreaWestRightTable1",
				"placingAreaEastLeftTable1", "placingAreaEastRightTable1",
				"placingAreaNorthLeftTable2", "placingAreaNorthRightTable2",
				"placingAreaSouthLeftTable2", "placingAreaSouthRightTable2",
				"trayArea1", 
				N};
		// race:ManipulationArea, race:PreManipulationArea, race:FloorArea, race:SittingArea
		// index: 4, 5
		symbols[3] = new String[] {"manipulationAreaEastCounter1", "preManipulationAreaEastCounter1",
				"manipulationAreaNorthTable1", "manipulationAreaSouthTable1",
				"preManipulationAreaNorthTable1", "preManipulationAreaSouthTable1",
				"manipulationAreaWestTable2", "manipulationAreaEastTable2",
				"preManipulationConstraintWestTable2", "preManipulationConstraintEastTable2",
				"floorAreaTamsRestaurant1", 
				"sittingAreaWestTable1", "sittingAreaEastTable1",
				"sittingAreaNorthTable2", "sittingConstraintSouthTable2",
				N};
		// race:Furniture
		// index: 6 
		symbols[4] = new String[] {"table1", "table2", "counter1", N};
		// race:Guest
		// index: 7
		symbols[5] = new String[] {"guest1", "guest2", N};
		// race:Arm
		// index: 8, 9
		symbols[6] = new String[] {"leftArm1", "rightArm1", N};
		// race:RobotPosture
		// index: 10, 11 
		symbols[7] = new String[] {
				"armTuckedPosture", "armUnTuckedPosture", "armToSidePosture", "armUnnamedPosture", "armCarryPosture",
				"torsoUpPosture", "torsoDownPosture", "torsoMiddlePosture", 
				N};
		return symbols;
	}
	
	public static int[] createIngredients() {
		return new int[] {1,2,1,2,1,1,2,2};
	}
	
	
	public static Vector<PlanReportroryItem> createMethods(FluentNetworkSolver groundSolver) {
		Vector<PlanReportroryItem> ret = new Vector<PlanReportroryItem>();
		
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?counter", "none", "none"}, new int[] {0, 0, 1, 1});
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", "!drive", new String[] {"none", "?pl", "none", "none"});
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", "!grasp", new String[] {"?mug", "?pl", "none", "none"});
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(drive);
		before.setTo(grasp);
		PFD0Method getMug1Method = new PFD0Method("get_mug", new String[] {"?mug", "?pl", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				new VariablePrototype[] {grasp, drive}, 
				new FluentConstraint[] {before}
		);
//		getMug1Method.setDurationBounds(new Bounds(10, 40));
		ret.add(getMug1Method);
		
		return ret;
	}
	
	
	/**
	 * Create all operators.
	 * In the old domain we have the following operators:
	 * * !tuck_arms ?leftarmgoal ?rightarmgoal
	 * * !move_arms_to_carryposture
	 * * !move_arm_to_side ?whicharm
	 * * !pick_up_object ?object ?arm
	 * * !place_object ?object ?arm ?placingArea
	 * * !move_base ?to
	 * * !move_base_blind ?to
	 * * !move_torso ?torsoPostureType
	 * @param groundSolver 
	 * @return The operators.
	 */
	public static Vector<PlanReportroryItem> createOperators(FluentNetworkSolver groundSolver) {
		Vector<PlanReportroryItem> ret = new Vector<PlanReportroryItem>();
		
		//###################### !tuck_arms ?leftarmgoal ?rightarmgoal ##############################
		PFD0Precondition oldArmPosture3 = new PFD0Precondition("HasArmPosture", 
				new String[] {N, N, N, N, N, N, N, "leftArm1", N, "?oldPosture1", N}, 
				new int[] {7,7});
		oldArmPosture3.setNegativeEffect(true);
		PFD0Precondition oldArmPosture4 = new PFD0Precondition("HasArmPosture", 
				new String[] {N, N, N, N, N, N, N, "rightArm1", N, "?oldPosture2", N}, 
				new int[] {7, 8});
		oldArmPosture4.setNegativeEffect(true);
		
		// TODO make sure that the goal is in {ArmTuckedPosture, ArmUnTuckedPosture
		VariablePrototype newArmPosture3 = new VariablePrototype(groundSolver, "S", 
				"HasArmPosture", new String[] {N, N, N, N, N, N, N, "leftArm1", N, "?leftGoal", N});
		VariablePrototype newArmPosture4 = new VariablePrototype(groundSolver, "S", 
				"HasArmPosture", new String[] {N, N, N, N, N, N, N, "rightArm1", N, "?rightGoal", N});

		ret.add( new PFD0Operator("!tuck_arms", 
				new String[] {N, N, N, N, N, N, N, "leftArm1", "rightArm1", "?leftGoal", "?rightGoal"},
				new PFD0Precondition[]{oldArmPosture3, oldArmPosture4}, 
				new VariablePrototype[] {newArmPosture3, newArmPosture4}));
		
		
		//################ !move_arms_to_carryposture ################################################
		// TODO make sure that torso is down
		PFD0Precondition oldArmPosture1 = new PFD0Precondition("HasArmPosture", 
				new String[] {N, N, N, N, N, N, N, "leftArm1", N, "?oldPosture1", N}, 
				new int[] {7,7});
		oldArmPosture1.setNegativeEffect(true);
		PFD0Precondition oldArmPosture2 = new PFD0Precondition("HasArmPosture", 
				new String[] {N, N, N, N, N, N, N, "rightArm1", N, "?oldPosture2", N}, 
				new int[] {7, 8});
		oldArmPosture2.setNegativeEffect(true);
		
		VariablePrototype newArmPosture1 = new VariablePrototype(groundSolver, "S", 
				"HasArmPosture", new String[] {N, N, N, N, N, N, N, "leftArm1", N, "armCarryPosture", N});
		VariablePrototype newArmPosture2 = new VariablePrototype(groundSolver, "S", 
				"HasArmPosture", new String[] {N, N, N, N, N, N, N, "rightArm1", N, "armCarryPosture", N});

		ret.add( new PFD0Operator("!move_arms_to_carryposture", 
				new String[] {N, N, N, N, N, N, N, "leftArm1", "rightArm1", N, N},
				new PFD0Precondition[]{oldArmPosture1, oldArmPosture2}, 
				new VariablePrototype[] {newArmPosture1, newArmPosture2}));
		
		//################### !move_arm_to_side ?whicharm ###########################################
			// hasArmPosture ?arm ?armposture
		// TODO: here we could restrict the domain of the old posture to be not tucked
		// + same for the other arm
		PFD0Precondition oldArmPosture0 = new PFD0Precondition("HasArmPosture", 
				new String[] {N, N, N, N, N, N, N, "?arm", N, "?oldPosture", N}, 
				new int[] {7,7});
		oldArmPosture0.setNegativeEffect(true);
		
		VariablePrototype newArmPosture0 = new VariablePrototype(groundSolver, "S", 
				"HasArmPosture", new String[] {N, N, N, N, N, N, N, "?arm", N, "armToSidePosture", N});

		ret.add( new PFD0Operator("!move_arm_to_side", 
				new String[] {N, N, N, N, N, N, N, "?arm", N, N, N},
				new PFD0Precondition[]{oldArmPosture0}, 
				new VariablePrototype[] {newArmPosture0}));


		//################ !pick_up_object ?object ?arm #############################################
			// On ?object ?placingarea
			// RobotAt trixi ?manipulationarea
			// ismanipulationarea ?manipulationarea
			// hasplacingarea ?manarea ?placingarea
			// Holding ?arm ?object
			// Armposture ?arm ?armtosideposture
			// torsoposture ?torso ?torsoposture
		// TODO: NOT HOLDING
		PFD0Precondition onPrePickObj = new PFD0Precondition("On", 
				new String[] {"?obj", N, "?fromArea", N, N, N, N, N, N, N, N}, 
				new int[] {0,0, 2,2});
		onPrePickObj.setNegativeEffect(true);
		PFD0Precondition atPrePickObj = new PFD0Precondition("RobotAt", 
				new String[] {N, N, N, "?mArea", N, N, N, N, N, N, N}, 
				new int[] {3, 3});
		PFD0Precondition connectedPickObj = new PFD0Precondition("Connected", 
				new String[] {N, N, "?fromArea", "?mArea", N, N, N, N, N, N, N}, 
				new int[] {2,2, 3, 3});
		
		VariablePrototype holdingEffPickObj = new VariablePrototype(groundSolver, "S", 
				"Holding", new String[] {"?obj", N, N, N, N, N, N, "?arm", N, N, N});

		// TODO delete old armposture and create new
		ret.add( new PFD0Operator("!pick_up_object", 
				new String[] {"?obj", N, "?fromArea", "?mArea?", N, N, N, "?arm", N, N, N}, // head
				new PFD0Precondition[]{onPrePickObj, atPrePickObj, connectedPickObj}, 
				new VariablePrototype[] {holdingEffPickObj}));
			
		
		//############### !place_object ?object ?arm ?placingArea ###################
		PFD0Precondition holdingPrePlaceObj = new PFD0Precondition("Holding", 
				new String[] {"?obj", N, N, N, N, N, N, "?arm", N, N, N}, 
				new int[] {0,0, 7,7});
		holdingPrePlaceObj.setNegativeEffect(true);
		PFD0Precondition atPrePlaceObj = new PFD0Precondition("RobotAt", 
				new String[] {N, N, N, "?mArea", N, N, N, N, N, N, N}, 
				new int[] {3, 3});
		PFD0Precondition connectedPlaceObj = new PFD0Precondition("Connected", 
				new String[] {N, N, "?toArea", "?mArea", N, N, N, N, N, N, N}, 
				new int[] {2,2, 3, 3});
		
		VariablePrototype onEffPlaceObj = new VariablePrototype(groundSolver, "S", 
				"On", new String[] {"?obj", N, "?toArea", N, N, N, N, N, N, N, N});

		// TODO delete old armposture and create new
		ret.add( new PFD0Operator("!place_object", 
				new String[] {"?obj", N, "?toArea", "?mArea?", N, N, N, "?arm", N, N, N}, // head
				new PFD0Precondition[]{holdingPrePlaceObj, atPrePlaceObj, connectedPlaceObj}, 
				new VariablePrototype[] {onEffPlaceObj}));
		
		//#################### !move_base ?to #########################################
		PFD0Precondition atPreMB = new PFD0Precondition("RobotAt", 
				new String[] {N, N, N, "?formArea", N, N, N, N, N, N, N}, 
				new int[] {});
		atPreMB.setNegativeEffect(true);
		
		VariablePrototype robotatEffMB = new VariablePrototype(groundSolver, "S", 
				"RobotAt", new String[] {N, N, N, "?toArea", N, N, N, N, N, N, N});
		
		ret.add( new PFD0Operator("!move_base", 
				new String[] {N, N, N, "?toArea", N, N, N, N, N, N, N}, // head
				new PFD0Precondition[]{atPreMB}, 
				new VariablePrototype[] {robotatEffMB}));
		
		//#################### !move_base_blind ?to ###################################
		PFD0Precondition atPreMBB = new PFD0Precondition("RobotAt", 
				new String[] {N, N, N, "?formArea", N, N, N, N, N, N, N}, 
				new int[] {});
		atPreMBB.setNegativeEffect(true);
		
		VariablePrototype robotatEffMBB = new VariablePrototype(groundSolver, "S", 
				"RobotAt", new String[] {N, N, N, "?toArea", N, N, N, N, N, N, N});
		
		ret.add( new PFD0Operator("!move_base_blind", 
				new String[] {N, N, N, "?toArea", N, N, N, N, N, N, N}, // head
				new PFD0Precondition[]{atPreMBB}, 
				new VariablePrototype[] {robotatEffMBB}));
		
		
		// !move_torso ?torsoPostureType
			// Torsoposture ?oldposture
		
		return ret;
		
		
	}
	
	

}