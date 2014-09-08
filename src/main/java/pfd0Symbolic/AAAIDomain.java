package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.VariablePrototype;

public class AAAIDomain {
	
	private static String N = "n";
	
	
	public static String[][] createSymbols() {
		String[][] symbols = new String[8][];
		// predicates  
		// index: 0
		symbols[0] = new String[] {"On", "RobotAt", "Holding",
				"Connected",
				"!move_base", "!move_base_blind", "!place_object"};	
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
		symbols[7] = new String[] {"armTuckedPosture", "armToSidePosture", 
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
		
		// !tuck_arms ?leftarmgoal ?rightarmgoal
			// hasArmposture ?arm ?armposture
		
		// !move_arms_to_carryposture
			// hasArmposture ?arm ?armposture
		
		// !move_arm_to_side ?whicharm
			// hasArmPosture ?arm ?armposture
		
		// !pick_up_object ?object ?arm
			// On ?object ?placingarea
			// RobotAt trixi ?manipulationarea
			// ismanipulationarea ?manipulationarea
			// hasplacingarea ?manarea ?placingarea
			// Holding ?arm ?object
			// Armposture ?arm ?armtosideposture
			// torsoposture ?torso ?torsoposture
			
		
		//############### !place_object ?object ?arm ?placingArea ###################
			// robotat ?trixi ?manipulationarea
			// Manipulationarea
			// hasplacingarea ?manarea ?placingarea
			// holding
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
