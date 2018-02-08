package old_dev.axioms;

import java.util.logging.Level;

import htn.HTNMetaConstraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestAxiomMetaConstraint {
	
	private static HTNPlanner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
//		String[][] symbols = TestRACEDomain.createSymbols();
//		int[] ingredients = TestRACEDomain.createIngredients();
//		
//		planner = new HTNPlanner(0,  600,  0, symbols, ingredients);
//		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
//		
////		initMetaConstraints();
//		TestRACEDomain.initPlanner(planner, "domains/race_domain.ddl");
//		createProblemPickUpObject(fluentSolver);
//
//		test();
	}
	
	
	private static void test() {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

		
		MetaCSPLogging.setLevel(Level.FINEST);
		
		long startTime = System.nanoTime();
		System.out.println("Found a plan? " + planner.backtrack());
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		
//		Callback cb = new Callback() {
//			@Override
//			public void performOperation() {
//				long startTime = System.nanoTime();
//				System.out.println("Found a plan? " + planner.backtrack());
//				
//				((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSubFull();
//				long endTime = System.nanoTime();
//				System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//				planner.draw();
//			}
//		};
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
		System.out.println(planner.getDescription());
//		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1].getConstraintNetwork());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
	}
	
//	private static void initMetaConstraints() {
//		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
//		Vector<PlanReportroryItem> operators = AAAIDomainSingle.createOperators(fluentSolver);
//		selectionConstraint.addOperators(operators);
//		Vector<PlanReportroryItem> methods = AAAIDomainSingle.createMethods(fluentSolver);
//		selectionConstraint.addMethods(methods);
//		planner.addMetaConstraint(selectionConstraint);
//		
//		AxiomMetaConstraint axiomConstraint = new AxiomMetaConstraint();
//		planner.addMetaConstraint(axiomConstraint);
//	}
	
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
		for(Variable v : stateVars) {v.setMarking(HTNMetaConstraint.markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!pick_up_object(mug1 ?area ?manArea leftArm1)");
		taskFluent.setMarking(HTNMetaConstraint.markings.UNPLANNED);
		
		// Axiom constraint:
		FluentConstraint axiomConstraint = new FluentConstraint(FluentConstraint.Type.AXIOM, "TODO");
		axiomConstraint.setFrom(taskFluent);
		axiomConstraint.setTo(taskFluent);
		groundSolver.addConstraint(axiomConstraint);
	}

}
