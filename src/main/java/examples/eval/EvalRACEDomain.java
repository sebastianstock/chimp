package examples.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import postprocessing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;

public class EvalRACEDomain {
	
	static final boolean LOGGING = false;
	static final boolean GUESS_ORDERING = true;
    static final boolean PRINT_PLAN = false;
    static final boolean DRAW = false;
	private static double planningtime = -1;
	

	public static void main(String[] args) {
		
//		ProblemParser pp = new ProblemParser("../eval/race/test_m_serve_coffee_problem_1.pdl"); // #0
		
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_a2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_a3.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_a4.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_b1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_b2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_b3.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_b4.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_2_c1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_3_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_3_a2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_3_d1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_4_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_4_a2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_4_c1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_5_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_5_a2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_a2.pdl"); 
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_a3.pdl"); // > 5 min.
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_a4.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_b2.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_6_c2.pdl"); // 51.0 sec.
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_a1.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_a2.pdl"); // > 5 min.
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_a3.pdl"); // 74.5 sec.
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_b4.pdl"); // 67.0 sec. 
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_b5.pdl"); // > 5 min.
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_b6.pdl"); // 7.7 sec. 
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_b7.pdl"); // 9.1 sec.
//		ProblemParser pp = new ProblemParser("../eval/race/move_8_c1.pdl"); // 9.9 sec.
//		ProblemParser pp = new ProblemParser("../eval/race/move_10_b1.pdl"); // 14.2 sec. 
//		ProblemParser pp = new ProblemParser("../eval/race/move_10_b2.pdl"); // 21.0 sec.
		ProblemParser pp = new ProblemParser("../eval/race/move_10_b3.pdl"); // 190.8 sec.

	
	// IROS
//		ProblemParser pp = new ProblemParser("../eval/race/iros_incremental_merging_initial.pdl");
//		ProblemParser pp = new ProblemParser("../eval/race/iros_coffee_sugar_merging.pdl");
		
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
		
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		
		try {
			initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);		
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

//		MetaCSPLogging.setLevel(Level.FINE);
		if (! LOGGING) {
			MetaCSPLogging.setLevel(Level.OFF);
		}
		
//		planner.createInitialMeetsFutureConstraints();
		plan(planner, fluentSolver);
		
		Variable[] allFluents = fluentSolver.getVariables();
		ArrayList<Variable> plan = new ArrayList<Variable>();
		int opCount = 0;
		int mCount = 0;
		for (Variable var : allFluents) {
			String component = var.getComponent();
			if (component == null) {
				plan.add(var);
			}
			else if (component.equals("Activity")) {
				plan.add(var);
				opCount++;
			} else if (component.equals("Task")) {
				mCount++;
			}
		}
		
		int mvarinvocs = -1;
		for (MetaConstraint mcon : planner.getMetaConstraints()) {
			if (mcon instanceof HTNMetaConstraint) {
				System.out.println("#getMetaVariables-Invocations: " + ((HTNMetaConstraint) mcon).getVarsCNT);
				mvarinvocs = ((HTNMetaConstraint) mcon).getVarsCNT;
			}
		}
		System.out.println("#Ops: " + opCount);
		System.out.println("#Compound Tasks: " + mCount);
		System.out.println("#Fluents: " + fluentSolver.getVariables().length);
		System.out.println("FluentConstraints: " + fluentSolver.getConstraints().length);
		
		System.out.println("---------------------------------------");
		// print number of applied meta values per metaconstraint:
//		System.out.println("Tried MetaValues: ");
//		int sum = 0;
//		for (Entry<MetaConstraint, Integer> entry: planner.getValCounters().entrySet()) {
//			System.out.println(entry);
//			sum += entry.getValue();
//		}
//		System.out.println("Sum: " + sum);
//		System.out.println("---------------------------------------");
		
		System.out.print(planningtime);
		System.out.print("\t" + mvarinvocs);
		System.out.print("\t" + opCount);
		System.out.print("\t" + mCount);
		System.out.print("\t" + fluentSolver.getVariables().length);
		System.out.print("\t" + fluentSolver.getConstraints().length);
//		System.out.println("\t" + sum);
		
		if (PRINT_PLAN) {

			Variable[] planVector = plan.toArray(new Variable[plan.size()]);
			Arrays.sort(planVector, new Comparator<Variable>() {
				@Override
				public int compare(Variable o1, Variable o2) {
					// TODO Auto-generated method stub
					Fluent f1 = (Fluent)o1;
					Fluent f2 = (Fluent)o2;
					return ((int)f1.getTemporalVariable().getEST()-(int)f2.getTemporalVariable().getEST());
				}
			});

			int c = 0;
			for (Variable act : planVector) {
				if (act.getComponent() != null)
					System.out.println(c++ +".\t" + act);	
			}

			extractPlan(fluentSolver);
		}

		////////////////

	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		if (DRAW) {
			planner.draw();

			ConstraintNetwork cn = new ConstraintNetwork(null);
			for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
				if (con instanceof FluentConstraint) {
					FluentConstraint fc = (FluentConstraint) con;
					if (fc.getType() == FluentConstraint.Type.MATCHES) {
						fc.getFrom().setMarking(HTNMetaConstraint.markings.UNIFIED);
						cn.addConstraint(fc);
					} else if (fc.getType() == FluentConstraint.Type.DC) {
						cn.addConstraint(fc);
					}
				}
			}
			ConstraintNetwork.draw(cn);
			ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		}
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

//		System.out.println(planner.getDescription());
		planningtime  = (endTime - startTime) / 1000000000.;
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		
//		System.out.println("AGAIN");
//		startTime = System.nanoTime();
//		result = planner.backtrack();
//		System.out.println("Found a plan? " + result);
//		endTime = System.nanoTime();
//
//		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//		System.out.println("Finished");
		
		return result;
	}
	
	public static void extractPlan(FluentNetworkSolver fluentSolver) {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	
	public static void initPlanner(HTNPlanner planner, HybridDomain domain) throws DomainParsingException {
		// load domain
		domain.parseDomain(planner.getTypesInstancesMap());
		
		// init meta constraints based on domain
//		ValueOrderingH valOH = new UnifyEarlisttasksValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestWeightNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestFewestsubsNewestbindingsValOH();
		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyDeepestWeightOldestbindingsValOH();
		
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.enableUnification();
		htnConstraint.addOperators(domain.getOperators());
		htnConstraint.addMethods(domain.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = domain.getFluentResourceUsages();
		htnConstraint.setResourceUsages(fluentResourceUsages);
		
		
		for (FluentScheduler fs : domain.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : domain.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		if (GUESS_ORDERING) {
			ValueOrderingH guessOH = new GuessOrderingValOH();
			GuessOrderingMetaConstraint ordConstraint = new GuessOrderingMetaConstraint(guessOH);
			planner.addMetaConstraint(ordConstraint);
		}
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
		
		planner.addMetaConstraint(htnConstraint);
	}
	



}
