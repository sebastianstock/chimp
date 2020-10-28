package examples;

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

import dwr.DWRNavigationMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.DeepestFewestsubsNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import postprocessing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestDWRDomain {
	
	static final boolean LOGGING = false;
	static final boolean NAVIGATION_PLANNING = true;
	static final boolean GUESS_ORDERING = false;
    static final boolean PRINT_PLAN = true;
    static final boolean DRAW = false;
	
//	static final String ProblemPath = "domains/dwr/test/test_op_leave.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_enter.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_move.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_stack.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_unstack.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_put.pdl";
//	static String ProblemPath = "domains/dwr/test/test_op_take.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_load.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_unload.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_uncover0.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_uncover1.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_navigate0.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_navigate1.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_goto0.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_goto1.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_bring0.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_bring1.pdl";
//	static String ProblemPath = "domains/dwr/test/test_m_bring2.pdl";
	
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_simple.pdl"; // runs into a trap -> 30 seconds runtime
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_0.pdl";
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_1.pdl";
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_2.pdl";
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_3.pdl";
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_3_c11_c23.pdl"; 
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_3_c11_c21.pdl"; 
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_3_c19_c23_c18.pdl"; 
//	static String ProblemPath = "domains/dwr/comp_problems/dwr_problem_3_c19_c23_c18_c22.pdl"; 
	
//	static String ProblemPath = "domains/dwr/dwr_test_examples/bring/dwr_abb63_bring.pdl";
	static String ProblemPath = "domains/dwr/dwr_test_examples/bring/dwr_abb63_bring_c13_c21.pdl";
//	static String ProblemPath = "domains/dwr/dwr_test_examples/bring/dwr_abb63_bring.pdl";
//	static String ProblemPath = "domains/dwr/dwr_test_examples/bring/dwr_abb63_bring2.pdl";
//	static String ProblemPath = "domains/dwr/dwr_test_examples/bring/dwr_abb63_bring2_c11_c21.pdl";
//	static String ProblemPath = "domains/dwr/dwr_test_examples/move/dwr_problem_1_r1-c14-p4_r2-c24-p3.pdl";
	
	
	
	
	public static void main(String[] args) {
		plan_dwr(args);
	}

	public static double plan_dwr(String[] args) {
//		Scanner s = new Scanner(System.in);
//		System.out.println(s.nextInt());
		
		ProblemParser pp = new ProblemParser(ProblemPath);
		
		HybridDomain domain;
		try {
//			domain = new HybridDomain("domains/dwr/dwr.ddl");
//			domain = new HybridDomain("domains/dwr/dwr_resources.ddl");
			domain = new HybridDomain("domains/dwr/dwr_resources2.ddl");
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return 0;
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
			return 0;
		}
		
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		MetaCSPLogging.setLevel(Level.INFO);
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINE);		
		if (! LOGGING) {
			MetaCSPLogging.setLevel(Level.OFF);
		}

		MetaCSPLogging.setLevel(planner.getClass(), Level.FINE);
		
		double planning_time = plan(planner, fluentSolver);
		
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
			
//			if (var.getMarking() == markings.OPEN) {
//				System.out.println(var);
//			}
		}
		
		for (MetaConstraint mcon : planner.getMetaConstraints()) {
			if (mcon instanceof HTNMetaConstraint) {
				System.out.println("#getMetaVariables-Invocations: " + ((HTNMetaConstraint) mcon).getVarsCNT);
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
		System.out.println("---------------------------------------");

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
		return planning_time;
		
	}
	
	public static double plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		long endTime = System.nanoTime();
		System.out.println("Found a plan? " + result);
		
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
		
		if (DRAW) {
			planner.draw();
			ConstraintNetwork.draw(cn);
			ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
			ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());
		}

//		System.out.println(planner.getDescription());
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
		
		return (endTime - startTime) / 1000000;
	}
	
	public static void extractPlan(FluentNetworkSolver fluentSolver) {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	
	public static void initPlanner(HTNPlanner planner, HybridDomain domain) throws DomainParsingException {
		// load domain
		domain.parseDomain(planner.getTypesInstancesMap());
		
		// init meta constraints based on domain
		ValueOrderingH valOH = new DeepestFewestsubsNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestNewestbindingsValOH(); // not working
//		ValueOrderingH valOH = new DeepestWeightNewestbindingsValOH();	
//		ValueOrderingH valOH = new ShallowFewestsubsNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyEarlisttasksValOH(); // not working
//		ValueOrderingH valOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
//		ValueOrderingH valOH = new UnifyWeightNewestbindingsValOH();
//		ValueOrderingH valOH = new DeepestWeightOldestbindingsValOH();	
		
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
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
	
		if (NAVIGATION_PLANNING) {
			DWRNavigationMetaConstraint navConstraint = new DWRNavigationMetaConstraint();
			planner.addMetaConstraint(navConstraint);
		}
		
		planner.addMetaConstraint(htnConstraint);
	}
	



}
