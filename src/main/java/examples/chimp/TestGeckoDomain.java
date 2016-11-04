package examples.chimp;

import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestGeckoDomain {
	

	public static void main(String[] args) {
		
		String problem = "problems/gecko/gecko1.pdl";
		String domain = "domains/gecko.ddl";
		
		CHIMP chimp;
		try {
			chimp = new CHIMP.CHIMPBuilder(domain, problem)
					.valHeuristic(new UnifyFewestsubsEarliesttasksNewestbindingsValOH())
					.build();
		} catch (DomainParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		System.out.println("Found plan? " + chimp.generatePlan());
		chimp.printStats(System.out);
		
	}
	



}
