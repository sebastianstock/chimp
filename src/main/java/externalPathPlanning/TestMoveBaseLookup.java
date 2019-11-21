package externalPathPlanning;

import org.metacsp.framework.ValueOrderingH;

import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestMoveBaseLookup {
	

	public static void main(String[] args) {
		
		String problemFile = "problems/test_op_move_base.pdl";
		String domainFile = "domains/ordered_domain.ddl";
		
		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

		CHIMP.CHIMPBuilder builder;
		try {
			 builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
					.valHeuristic(valOH)
					.mbEstimator(new LookUpTableDurationEstimator())
					.htnUnification(true);
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		CHIMP chimp = builder.build();
		System.out.println("Found plan? " + chimp.generatePlan());
		chimp.printStats(System.out);
	}


}
