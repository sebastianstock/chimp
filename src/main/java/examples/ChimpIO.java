package examples;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import postprocessing.EsterelGenerator;
import postprocessing.PlanExtractor;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;
import picocli.CommandLine;
import planner.CHIMP;
import unify.CompoundSymbolicVariableConstraintSolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.logging.Level;

@CommandLine.Command(name = "chimp", mixinStandardHelpOptions = true,
		description = "Plan with CHIMP.")
public class ChimpIO implements Callable<Integer> {

	@CommandLine.Parameters(index = "0", description = "The file containing the planning domain.")
	File domainFile;

	@CommandLine.Parameters(index = "1", description = "The file containing the planning problem.")
	File problemFile;

	@CommandLine.Option(names = {"-o", "--output"}, description = "Write the plan to this output file.")
	File outputFile;

	@CommandLine.Option(names = {"-e", "--esterel"}, description = "Write esterel graph to this output file.")
	File esterelOutputFile;

	@CommandLine.Option(names = {"--horizon"}, description = "Horizon for the temporal variables. (Default: ${DEFAULT-VALUE})")
	private int horizon = 600000;

	@CommandLine.Option(names = {"--htn-unification"}, description = "Try to unify tasks with existing ones during HTN-planning. (Default: ${DEFAULT-VALUE})")
	private boolean htnUnification = false;

	@CommandLine.Option(names = {"--guess-ordering"}, description = "Indicates whether the GuessOrderingMetaConstraint shall be used. (Default: ${DEFAULT-VALUE})")
	private boolean guessOrdering = false;

	@CommandLine.Option(names = {"--print-stats"}, description = "Print statistics. (Default: ${DEFAULT-VALUE})")
	private boolean printStats = false;

//	@CommandLine.Option(names = {"--draw"}, description = "Draw the plan and search tree? (Default: ${DEFAULT-VALUE})")
	private boolean draw = false;

	@Override
	public Integer call() throws Exception {
		if (!checkFile(problemFile) || !checkFile(domainFile)) {
			CommandLine.usage(new ChimpIO(), System.out);
			return -1;
		}
		try {
			callCHIMP();
			return 0;
		} catch (DomainParsingException e) {
			System.out.println("Could not parse domain");
			return -1;
		}
	}

	public static void main(String[] args) {
		System.exit(new CommandLine(new ChimpIO()).execute(args));
	}

	private void callCHIMP() throws DomainParsingException {
		CHIMP.CHIMPBuilder builder;
		ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
		try {
			builder = new CHIMP.CHIMPBuilder(domainFile.getAbsolutePath(), problemFile.getAbsolutePath())
					.horizon(horizon)
					.valHeuristic(valOH)
					.guessOrdering(guessOrdering)
					.htnUnification(htnUnification);
		} catch (DomainParsingException e) {
			e.printStackTrace();
			return;
		}
		CHIMP chimp = builder.build();

		MetaCSPLogging.setLevel(Level.OFF);
		
		boolean result = chimp.generatePlan();
		
		if (draw) {
			chimp.getPlanner().draw();
			chimp.drawSearchSpace();
			chimp.drawHierarchyNetwork();
			chimp.drawPlanHierarchy(5);
		}

		if (result) {
			System.out.println("Solution found");
		} else {
			System.out.println("Could not find a plan");
		}

		if (printStats) {
			chimp.printStats(System.out);
		}

		PlanExtractor pex = new PlanExtractor(chimp.getFluentSolver());
//		pex.printPlan();
		if(result) {
			pex.printActions();
		}
		if (outputFile != null) {
			pex.writePlanToFile(result, outputFile);
		}

		if (esterelOutputFile != null && result) {
			try {
				FileWriter fw = new FileWriter(esterelOutputFile);
				EsterelGenerator.generateEsterelGraph(chimp, fw);
			} catch (IOException e) {
				e.printStackTrace();
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
	
	private static boolean checkFile(File f) {
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			System.out.println("File " + f.getAbsolutePath() + " does not exist");
			return false;
		}
	}
}
