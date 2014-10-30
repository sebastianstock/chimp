package externalPahtPlanning;

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.AAAIDomain;
import pfd0Symbolic.AAAIProblems;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.PlanReportroryItem;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestMoveBaseLookup {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[][] symbols = AAAIDomain.createSymbols();
		int[] ingredients = AAAIDomain.createIngredients();
		
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		initMetaConstraints();
		AAAIProblems.createProblemMoveBase(fluentSolver);
//		AAAIProblems.createProblemMoveArmToSide(fluentSolver);
//		AAAIProblems.createProblemMoveArmsToCarryposture(fluentSolver);
//		AAAIProblems.createProblemTuckArms(fluentSolver);
//		AAAIProblems.createProblemPickUpObject(fluentSolver);
//		AAAIProblems.createProblemMoveTorso(fluentSolver);
//		AAAIProblems.createProblemPlaceObject(fluentSolver);
		
//		AAAIProblems.createProblemDriveM(fluentSolver);
//		AAAIProblems.createProblemGraspM(fluentSolver);
//		AAAIProblems.createProblemGetObjectWithArmM(fluentSolver);
		test();
	}
	
	
	private static void test() {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

		
		MetaCSPLogging.setLevel(Level.FINE);
		
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
		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1].getConstraintNetwork());
		
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		
		
		System.out.println("Finished");
	}
	
	private static void initMetaConstraints() {
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		Vector<PlanReportroryItem> operators = AAAIDomain.createOperators(fluentSolver);
		selectionConstraint.setOperators(operators);
		Vector<PlanReportroryItem> methods = AAAIDomain.createMethods(fluentSolver);
		selectionConstraint.setMethods(methods);
		planner.addMetaConstraint(selectionConstraint);
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
	}

}
