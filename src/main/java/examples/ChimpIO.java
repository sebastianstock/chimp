package examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;

import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import unify.CompoundSymbolicVariableConstraintSolver;

public class ChimpIO {

	private static final boolean DRAW = false;
	
	private static final int HORIZON = 600000;
	
	public static void printUsage() {
		System.out.println("Usage: ");
		System.out.println("chimp domain_file problem_file [output_file]");
	}
	
	public static void initPlanner(HTNPlanner planner, HybridDomain domain) throws DomainParsingException {
		domain.parseDomain(planner);
		
		// init meta constraints based on domain
//		ValueOrderingH valOH = new NewestFluentsValOH();
		//ValueOrderingH valOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
		
		for (FluentScheduler fs : domain.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : domain.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.addOperators(domain.getOperators());
		htnConstraint.addMethods(domain.getMethods());
		htnConstraint.setResourceUsages(domain.getFluentResourceUsages());
		planner.addMetaConstraint(htnConstraint);
		
		// Not needed for transterra at the moment
//		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
//		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
//		planner.addMetaConstraint(mbConstraint);
	}

	public static void main(String[] args) {
		
		if (!checkArgs(args)) {
			printUsage();
			return;
		}
		
		String outputPath = null;
		if (args.length > 2) {
			outputPath = args[2];
		}
		
		try {
			
			callCHIMP(args[0], args[1], outputPath);
		} catch (DomainParsingException e) {
			System.out.println("Could not parse domain");
		}
		
		
	}
	
	private static void callCHIMP(String domainPath, String problemPath, String outputPath) throws DomainParsingException {
		HybridDomain domain  = new HybridDomain(domainPath);
		ProblemParser problemParser = new ProblemParser(problemPath);
		
		int[] ingredients = new int[] {1, domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
		symbols[1] = problemParser.getArgumentSymbols();
		Map<String, String[]> typesInstancesMap = problemParser.getTypesInstancesMap();
		
		HTNPlanner planner = new HTNPlanner(0,  HORIZON,  0, symbols, ingredients);
		planner.setTypesInstancesMap(typesInstancesMap);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];


		try {
			initPlanner(planner, domain);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		problemParser.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);		
//		MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);
		
//		MetaCSPLogging.setLevel(Level.FINE);
		MetaCSPLogging.setLevel(Level.OFF);
		
//		planner.createInitialMeetsFutureConstraints();
		
		plan(planner, fluentSolver);
		
		if (DRAW) {
			planner.draw();
			drawNetworks(fluentSolver);
		}
		
//		System.out.println(planner.getDescription());
		
//		printPlan(fluentSolver);
		
		PlanExtractor pex = new PlanExtractor(fluentSolver);
//		pex.printPlan();
		pex.printActivities();
		
		// write plan to file
		if (outputPath != null) {

			Writer fw = null;
			try
			{
				fw = new FileWriter(outputPath);
				pex.writeActivities(fw);
				fw.flush();
			}
			catch ( IOException e ) {
				System.err.println( "Could not create file" );
			}
			finally {
				if ( fw != null )
					try { fw.close(); } catch ( IOException e ) { e.printStackTrace(); }
			}
		}
		
	}

	private static void printPlan(FluentNetworkSolver fluentSolver) {
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
		System.out.println("#Ops: " + opCount);
		System.out.println("#Compound Taks: " + mCount);
		System.out.println("#Fluents: " + fluentSolver.getVariables().length);
		System.out.println("#FluentConstraints: " + fluentSolver.getConstraints().length);

		Variable[] planVector = plan.toArray(new Variable[plan.size()]);
		Arrays.sort(planVector, new Comparator<Variable>() {
			@Override
			public int compare(Variable o1, Variable o2) {
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
		
	}

	public static boolean plan(HTNPlanner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		System.out.println("Planning took "+((endTime - startTime) / 1000000) + " ms"); 
		return result;
	}
	
	public static void drawNetworks(FluentNetworkSolver fluentSolver) {
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
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());	
	}
	
	private static boolean checkArgs(String[] args) {
		if (args.length < 2) {
			return false;
		}
		else {
			return checkFile(args[0]) && checkFile(args[1]);
		}
	}
	
	private static boolean checkFile(String arg) {
		File f = new File(arg);
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			System.out.println("File " + arg + " does not exist");
			return false;
		}
	}
	
}
