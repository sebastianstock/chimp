package domains;

import hybridDomainParsing.ProblemParser;

import org.metacsp.framework.ConstraintNetwork;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestProblemParsing {

	public static void main(String[] args) {
		
		ProblemParser pp = new ProblemParser("domains/race_problem.pdl");
		
		String[][] symbols = RACEProblemsSingle.createSymbols();
		int[] ingredients = RACEProblemsSingle.createIngredients();
		PFD0Planner planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
//		System.out.println("Found a plan? " + planner.backtrack());
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
	}
	



}
