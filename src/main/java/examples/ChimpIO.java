package examples;

import chimp.logging.ChimpLogging;
import htn.valOrderingHeuristics.CHIMPValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import picocli.CommandLine;
import planner.CHIMP;
import postprocessing.HPRGenerator;
import postprocessing.PlanExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	@CommandLine.Option(names = {"--hpr"}, description = "Write HPR graph to this output file.")
	File hprOutputFile;

	@CommandLine.Option(names = {"--horizon"}, description = "Horizon for the temporal variables. (Default: ${DEFAULT-VALUE})")
	private int horizon = 3600000;

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
		CHIMPValOH valOH = new UnifyDeepestWeightNewestbindingsValOH();
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

		ChimpLogging.setLevel(Level.OFF);
		
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

		if (hprOutputFile != null && result) {
			try {
				FileWriter fw = new FileWriter(hprOutputFile);
				HPRGenerator hprGenerator = new HPRGenerator(chimp, fw);
				hprGenerator.generateHPR();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


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
