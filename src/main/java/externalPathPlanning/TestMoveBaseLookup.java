package externalPathPlanning;

import java.util.Map;
import java.util.logging.Level;

import org.metacsp.utility.logging.MetaCSPLogging;

import examples.TestRACEDomain;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestMoveBaseLookup {
	

	public static void main(String[] args) {
		
		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
		
		HybridDomain domain;
		try {
			domain = new HybridDomain("domains/ordered_domain.ddl");
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		int[] ingredients = new int[] {1, domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
		symbols[1] = pp.getArgumentSymbols();
		Map<String, String[]> typesInstancesMap = pp.getTypesInstancesMap();
		//+++
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		try {
			TestRACEDomain.initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
//		MetaCSPLogging.setLevel(Level.INFO);
		
		planner.createInitialMeetsFutureConstraints();
		TestRACEDomain.plan(planner, fluentSolver);
		TestRACEDomain.extractPlan(fluentSolver);
	}


}
