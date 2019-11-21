package examples.chimp;

import java.util.logging.Level;

import htn.HTNMetaConstraint;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestIncrementalMerging {
	

	public static void main(String[] args) {
		
		// IROS
		String problemFile = "problems/iros/iros_incremental_merging_initial.pdl";
		String domainFile = "domains/ordered_domain.ddl";
		
		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

		CHIMP.CHIMPBuilder builder = null;
		try {
			builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
					.valHeuristic(valOH)
					.mbEstimator(new LookUpTableDurationEstimator())
					.htnUnification(true);
		} catch (DomainParsingException e) {
			e.printStackTrace();
		}
		builder.htnUnification(true);
		CHIMP chimp = builder.build();
		
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);		
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
		MetaCSPLogging.setLevel(Level.OFF);
		
		System.out.println("Found plan? " + chimp.generatePlan());
		chimp.printStats(System.out);
		
		// Add another task
		FluentNetworkSolver fluentSolver = chimp.getFluentSolver();
		System.out.println("##### Adding another task ########");
		String name = "move_object(milkPot1 placingAreaNorthLeftTable2)";
		String component = "Task";
		Variable var = fluentSolver.createVariable(component);
		((Fluent) var).setName(name);
		var.setMarking(HTNMetaConstraint.markings.UNPLANNED);
		
		chimp.drawSearchSpace();
		
		System.out.println("Found plan? " + chimp.generatePlan());
		chimp.printStats(System.out);
		
		Variable[] planVector = chimp.extractActions();
		int c = 0;
		for (Variable act : planVector) {
			if (act.getComponent() != null)
				System.out.println(c++ +".\t" + act);	
		}

		chimp.printFullPlan();
		chimp.drawSearchSpace();
	}
}
