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
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.LookUpTableDurationEstimatorICO;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyEarlisttasksValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import postprocessing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestTransTerraProblems {
	

	public static void main(String[] args) {
		
		// Testproblems for TransTerrA
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_move_to.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_sample_regolith.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_transfer_sample.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_transfer_payload.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_pickup_basecamp.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_op_place_basecamp.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_deploy_basecamp_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_deploy_basecamp_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_take_samples_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_take_samples_2.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_take_samples_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_get_basecamp_1.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_get_basecamp_2.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_transfer_filled.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_transfer_empty.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_rendezvous_meet.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_rendezvous_meet2.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_transfer_charged.pdl");
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_transfer_discharged.pdl");
		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_deposit_samples.pdl");
//		
//		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/test_m_transferall.pdl");
		
		ProblemParser pp = new ProblemParser("problems/transterra_problems_v1/scenario1.pdl");

		HybridDomain domain;
		try {
			domain = new HybridDomain("domains/transterra_v1.ddl");
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
//		MetaCSPLogging.setLevel(Level.FINEST);
		MetaCSPLogging.setLevel(Level.OFF);
		
//		testScheduling(planner);
		
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
		
		for (MetaConstraint mcon : planner.getMetaConstraints()) {
			if (mcon instanceof HTNMetaConstraint) {
				System.out.println("#getMetaVariables-Invocations: " + ((HTNMetaConstraint) mcon).getVarsCNT);
			}
		}
		System.out.println("#Ops: " + opCount);
		System.out.println("#Compound Taks: " + mCount);
		System.out.println("#Fluents: " + fluentSolver.getVariables().length);
		System.out.println("FluentConstraints: " + fluentSolver.getConstraints().length);

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
		
		////////////////
		
	}
	
	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

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
		
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
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
		System.out.println("###### All fluents: ########");
		planEx.printAllFluents();
	}
	
	public static HybridDomain initPlanner(HTNPlanner planner, String domainPath) {
		// load domain
		HybridDomain dom;
		try {
			dom = new HybridDomain(domainPath);
			dom.parseDomain(planner.getTypesInstancesMap());
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		// init meta constraints based on domain
//		ValueOrderingH valOH = new NewestFluentsValOH();
		ValueOrderingH valOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
		
		HTNMetaConstraint selectionConstraint = new HTNMetaConstraint(valOH);
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		selectionConstraint.setResourceUsages(dom.getFluentResourceUsages());
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
			System.out.println("Added FluentResourceUsageScheduler: " + rs);
		}
		
		planner.addMetaConstraint(selectionConstraint);
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
		
		return dom;
	}
	
	public static void testScheduling(HTNPlanner planner) {
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		Fluent[] fluents = (Fluent[]) fluentSolver.createVariables(3);
		
		fluents[0].setName("!move_to(rover1 b1)");
		fluents[1].setName("!move_to(rover1 b2)");
		fluents[2].setName("Attached(sampleContainer1 rover1)");
		
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con1.setFrom(fluents[0]);
		con1.setTo(fluents[0]);

		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con2.setFrom(fluents[1]);
		con2.setTo(fluents[1]);

		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10000,20000));
		con3.setFrom(fluents[2]);
		con3.setTo(fluents[2]);
		
		fluentSolver.addConstraints(con1,con2,con3);
		
		ResourceUsageTemplate rt = new ResourceUsageTemplate("resourceName", new int[] {1}, new String[] {"rover1"}, 1);
		FluentConstraint rcon1 = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
		rcon1.setFrom(fluents[0]);
		rcon1.setTo(fluents[0]);
		
		FluentConstraint rcon2 = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
		rcon2.setFrom(fluents[1]);
		rcon2.setTo(fluents[1]);
		
		ResourceUsageTemplate rt1 = new ResourceUsageTemplate("SampleStorageCapacityRover", new int[] {2}, new String[] {"rover1"}, 2);
		FluentConstraint rcon3 = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt1);
		rcon3.setFrom(fluents[2]);
		rcon3.setTo(fluents[2]);
		
//		fluentSolver.addConstraints(rcon1,rcon2, rcon3);
		fluentSolver.addConstraints(rcon3);
		
		FluentResourceUsageScheduler frs = new FluentResourceUsageScheduler(null, null, "resourceName", 1);
//		FluentScheduler fs = new FluentScheduler(null, null, "get_mug", 1, "mug1");
//		fs.setUsage(fluents);		
		planner.addMetaConstraint(frs);
	}
	
	public static void initPlanner(HTNPlanner planner, HybridDomain domain) throws DomainParsingException {
		// load domain
		domain.parseDomain(planner.getTypesInstancesMap());
		
		// init meta constraints based on domain
		ValueOrderingH valOH = new UnifyEarlisttasksValOH();
//		ValueOrderingH valOH = new UnifyFewestsubsNewestbindingsValOH();
		
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
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimatorICO();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
		
		planner.addMetaConstraint(htnConstraint);
	}
	



}
