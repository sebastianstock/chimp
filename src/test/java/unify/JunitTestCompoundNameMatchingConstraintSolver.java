package unify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import unify.CompoundSymbolicValueConstraint.Type;

public class JunitTestCompoundNameMatchingConstraintSolver {
	
	private CompoundSymbolicVariableConstraintSolver solver;
	private CompoundSymbolicVariable[] vars;
	private NameMatchingConstraintSolver nameSolver;
	
	private static String N = "n";

	@Before
	public void setUp() throws Exception {
		String[][] symbols = new String[2][];
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
				"armTuckedPosture", "armUnTuckedPosture", "armToSidePosture", "armUnnamedPosture", "armCarryPosture",
				"torsoUpPosture", "torsoDownPosture", "torsoMiddlePosture", 
				N};

		solver = new CompoundSymbolicVariableConstraintSolver(symbols, new int[] {1,2});
		vars = (CompoundSymbolicVariable[]) solver.createVariables(5);
		
		nameSolver = (NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
	}


	@Test
	public void testMatches() {
		vars[0].setName("On", "mug1", "placingAreaWestRightTable1"); // "On" in world state
		vars[1].setName("On", "?mug", "?area"); // "On" as precondition  (old htn version)
		vars[2].setName("!pick_up_object",  "?mug", N);     
		vars[3].setName("!pick_up_object", "?mug", N);      
		vars[4].setName("Holding", "?mug", "rightArm1"); // "holding" effect
		
		CompoundSymbolicValueConstraint cc01 = new CompoundSymbolicValueConstraint(Type.MATCHES);
		cc01.setFrom(vars[0]);
		cc01.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc01));
		
		NameMatchingConstraint nc12 = new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
		nc12.setFrom(vars[1].getInternalVariables()[1]);
		nc12.setTo(vars[2].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc12));
		
		
		NameMatchingConstraint nc32 = new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
		nc32.setFrom(vars[3].getInternalVariables()[1]);
		nc32.setTo(vars[2].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc32));
		
		NameMatchingConstraint nc342 = new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
		nc342.setFrom(vars[3].getInternalVariables()[1]);
		nc342.setTo(vars[4].getInternalVariables()[1]);
		assertTrue(nameSolver.addConstraint(nc342));
		System.out.println(vars[4].getName());
		assertTrue("'mug1' should be propageted to first parameter of 'holding'.", 
				vars[4].getName().equals("Holding(mug1 rightArm1)"));
	}
	
	@Test
	public void testSubmatches() {
		vars[0].setName("On", "mug1", "placingAreaWestRightTable1"); // "On" in world state
		vars[1].setName("On", "?mug", "?area"); // "On" as precondition  (old htn version)
		vars[2].setName("!pick_up_object",  "?mug", N);     
		vars[3].setName("!pick_up_object", "?mug", N);      
		vars[4].setName("Holding", "?mug", "rightArm1"); // "holding" effect
		
		CompoundSymbolicValueConstraint cc01 = new CompoundSymbolicValueConstraint(Type.SUBMATCHES, 
				new int[] {0, 0, 1, 1});
		cc01.setFrom(vars[0]);
		cc01.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc01));
//		System.out.println("Var1: " + vars[1].getName());
		assertTrue(vars[1].getName().equals("On(mug1 placingAreaWestRightTable1)"));
		
		CompoundSymbolicValueConstraint cc02 = new CompoundSymbolicValueConstraint(Type.SUBMATCHES, 
				new int[] {0, 1});
		cc02.setFrom(vars[0]);
		cc02.setTo(vars[2]);
		assertFalse(solver.addConstraint(cc02));
		
		CompoundSymbolicValueConstraint cc03 = new CompoundSymbolicValueConstraint(Type.SUBMATCHES, 
				new int[] {0, 0});
		cc03.setFrom(vars[0]);
		cc03.setTo(vars[3]);
		assertTrue(solver.addConstraint(cc03));
//		System.out.println("Var3: " + vars[3].getName());
		assertTrue(vars[3].getName().equals("!pick_up_object(mug1)"));

	}
	
	@Test
	public void testSymbolicRestriction() {
		vars[1].setName("On", "?mug", "?area"); // "On" as precondition  (old htn version)
		
		CompoundSymbolicValueConstraint cc11 = new CompoundSymbolicValueConstraint(
				Type.POSITIVEVALUERESTRICTION,
				new int[] {0, 1}, new String[][] {{"mug1", "mug2"}, {"placingAreaWestLeftTable1", "placingAreaWestRightTable1"}});
		cc11.setFrom(vars[1]);
		cc11.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc11));
//		System.out.println("Var1: " + vars[1].getName());
		assertTrue(vars[1].getName().equals("On([mug1, mug2] [placingAreaWestLeftTable1, placingAreaWestRightTable1])"));

	}
	
	
	@Test
	public void testSubDifferent() {
		vars[0].setName("On", "mug1", "placingAreaWestRightTable1");
		vars[1].setName("On", "mug2", "?area");
		System.out.println("Var0: " + vars[0].getName());
		System.out.println("Var1: " + vars[1].getName());
		
		CompoundSymbolicValueConstraint cc01 = new CompoundSymbolicValueConstraint(Type.SUBDIFFERENT, 
				new int[] {1, 1});
		cc01.setFrom(vars[0]);
		cc01.setTo(vars[1]);
		assertTrue(solver.addConstraint(cc01));
		
		System.out.println("Var0: " + vars[0].getName());
		System.out.println("Var1: " + vars[1].getName());

		vars[2].setName("On", "mug1", "?area");
		System.out.println("Var2: " + vars[2].getName());
		
		CompoundSymbolicValueConstraint cc02 = new CompoundSymbolicValueConstraint(Type.SUBDIFFERENT, 
				new int[] {0, 0});
		cc02.setFrom(vars[0]);
		cc02.setTo(vars[2]);
		assertFalse(solver.addConstraint(cc02));
		


	}
	


}
