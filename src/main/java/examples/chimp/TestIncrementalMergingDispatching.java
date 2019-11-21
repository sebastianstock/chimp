package examples.chimp;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Logger;

import htn.HTNMetaConstraint;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import dispatching.FluentDispatchingFunction;
import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;
import sensing.FluentConstraintNetworkAnimator;

public class TestIncrementalMergingDispatching {
	
	private static Logger logger;
	

	public static void main(String[] args) {
		
		logger = MetaCSPLogging.getLogger(TestIncrementalMergingDispatching.class);
		
		String problemFile = "problems/iros/iros_incremental_merging_initial.pdl";
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
		builder.htnUnification(true);
		CHIMP chimp = builder.build();
		
		System.out.println("Found plan? " + chimp.generatePlan());
		FluentNetworkSolver fluentSolver = chimp.getFluentSolver();	
		
		// Add another task
//		FluentNetworkSolver fluentSolver = chimp.getFluentSolver();
//		System.out.println("##### Adding another task ########");
//		String name = "move_object(milkPot1 placingAreaNorthLeftTable2)";
//		String component = "Task";
//		Variable var = fluentSolver.createVariable(component);
//		((Fluent) var).setName(name);
//		var.setMarking(markings.UNPLANNED);	
	
		dispatch(chimp, fluentSolver);
	}
	
	private static void dispatch(CHIMP chimp, FluentNetworkSolver fns) {

		logger.info("Starting Dispatching");

		final Vector<Fluent> executingActs = new Vector<Fluent>();

		FluentDispatchingFunction df = new FluentDispatchingFunction("Activity") {

			@Override
			public boolean skip(Fluent act) {
                return act.getMarking() == HTNMetaConstraint.markings.UNIFIED;
            }

			@Override
			public void dispatch(Fluent act) {
				executingActs.addElement(act);
				act.getCompoundSymbolicVariable().getPredicateName();
				logger.info("Dispatching: " + act.toString());
			}
		};

		FluentConstraintNetworkAnimator animator = new FluentConstraintNetworkAnimator(fns, 1000) {
			//		@Override
			//		protected long getCurrentTimeInMillis() {
			//			return (long)((double)cnode.getCurrentTime().totalNsecs()/1000000.0);
			//		}
		};
		animator.addDispatchingFunctions(fns, df);

		Fluent future = animator.getFuture();
		future.setColor(Color.WHITE);

		while (true) {
			System.out.println("Executing activities (press <enter> to refresh list):");
			for (int i = 0; i < executingActs.size(); i++) System.out.println(i + ". " + executingActs.elementAt(i));
			System.out.println("--");
			//System.out.print("Please enter activity to finish: ");  
			String input = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			try { input = br.readLine(); }
			catch (IOException e) { e.printStackTrace(); }

			if (input.length() > 0 && Character.isLetter(input.charAt(0))) {
				// Add another task
				String name = "move_object(milkPot1 placingAreaNorthLeftTable2)";
				String component;
				if (name.startsWith("!")) {
					component = "Activity";
				} else {
					component = "Task";
				}
				Variable var = fns.createVariable(component);
				((Fluent) var).setName(name);
				var.setMarking(HTNMetaConstraint.markings.UNPLANNED);

				//			planner.clearResolvers();
				System.out.println("Trying to merge in new task.");
				System.out.println("Found plan? " + chimp.generatePlan());
			}

			for (int i = 0; i < executingActs.size(); i++) {
				Fluent fl = executingActs.get(i);
				if (fl.getTemporalVariable().getEET() > future.getTemporalVariable().getEST()) { 
					df.finish(fl);
					executingActs.remove(i);
				}
			}


		}

	}



	
}
