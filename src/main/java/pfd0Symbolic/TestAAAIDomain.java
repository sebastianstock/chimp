package pfd0Symbolic;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import symbolicUnifyTyped.TypedCompoundSymbolicVariableConstraintSolver;

public class TestAAAIDomain {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[][] symbols = AAAIDomain.createSymbols();
		int[] ingredients = AAAIDomain.createIngredients();
		
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		initMetaConstraints();
//		AAAIProblems.createProblemMoveBase(fluentSolver);
//		AAAIProblems.createProblemMoveArmToSide(fluentSolver);
//		AAAIProblems.createProblemMoveArmsToCarryposture(fluentSolver);
//		AAAIProblems.createProblemTuckArms(fluentSolver);
//		AAAIProblems.createProblemPickUpObject(fluentSolver);
//		AAAIProblems.createProblemMoveTorso(fluentSolver);
//		AAAIProblems.createProblemPlaceObject(fluentSolver);
		
//		AAAIProblems.createProblemDriveM(fluentSolver);
		AAAIProblems.createProblemGraspM(fluentSolver);
		test();
	}
	
	
	private static void test() {
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		MetaCSPLogging.setLevel(Level.FINE);
		
		
//		System.out.println("Found a plan? " + planner.backtrack());
//		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
//		planner.draw();
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		
		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				long startTime = System.nanoTime();
				System.out.println("Found a plan? " + planner.backtrack());
				long endTime = System.nanoTime();
				System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//				((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
				planner.draw();
			}
		};
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
		
		System.out.println(planner.getDescription());
//		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[2].getConstraintNetwork());
		
		
//		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
//		Variable[] vars = fluentSolver.getVariables();
//		ArrayList<String> results = new ArrayList<String>();
//		for (int i = 0; i < vars.length; i++) {
//			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
//		}
//
//		System.out.println(results);
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), "Constraint Network");
//		ConstraintNetwork.draw(((TypedCompoundSymbolicVariableConstraintSolver)fluentSolver.getConstraintSolvers()[0]).getConstraintNetwork(), "Constraint Network");
		// TODO following line makes sure that symbolicvariables values are set, but may take to long if we do that always.
		
//		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

		//		TypedCompoundSymbolicVariableConstraintSolver compoundS = ((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]);
//		SymbolicVariableConstraintSolver ssolver = (SymbolicVariableConstraintSolver) compoundS.getConstraintSolvers()[2];
//		ConstraintNetwork.draw(ssolver.getConstraintNetwork());
		
		System.out.println("Finished");
	}
	
	private static void initMetaConstraints() {
//		PreconditionMetaConstraint preConstraint = new PreconditionMetaConstraint();
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
//		TaskApplicationMetaConstraint applicationConstraint = new TaskApplicationMetaConstraint();
		Vector<PlanReportroryItem> operators = AAAIDomain.createOperators(fluentSolver);
		selectionConstraint.setOperators(operators);
		Vector<PlanReportroryItem> methods = AAAIDomain.createMethods(fluentSolver);
		selectionConstraint.setMethods(methods);
//		planner.addMetaConstraint(preConstraint);
//		planner.addMetaConstraint(applicationConstraint);
		planner.addMetaConstraint(selectionConstraint);
	}

}
