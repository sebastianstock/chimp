package planner;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;

import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.TaskApplicationMetaConstraint.markings;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import unify.CompoundSymbolicVariableConstraintSolver;

/**
 * A wrapper for the HTN-Planner that bundles the features of the planner CHIMP.
 * 
 * @author Sebastian Stock
 *
 */
public class CHIMP {
	
	private ProblemParser problemParser;
	private HybridDomain domain;
	private HTNPlanner planner;
	private FluentNetworkSolver fluentSolver;
	private boolean foundPlan = false;
	private long planningTime = -1;
	
	private static long origin = 0; // TODO: parse from problem file
	private static long horizon = 600000; // TODO: parse from problem file

	public static class CHIMPBuilder {
		
		private final String domainPath;
		private final String problemPath;
		private ValueOrderingH htnValOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
		private MoveBaseDurationEstimator mbEstimator;
		private boolean guessOrdering = false;
		public boolean htnUnification = false;
		
		public CHIMPBuilder(String domainPath, String problemPath) {
			this.domainPath = domainPath;
			this.problemPath = problemPath;	
		}
		
		/**
		 * 
		 * @param valOH The heuristic that shall be used by the HTNPlanner. Default: UnifyFewestsubsEarliesttasksNewestbindingsValOH
		 * @return
		 */
		public CHIMPBuilder valHeuristic(ValueOrderingH valOH) {
			this.htnValOH = valOH;
			return this;
		}
		
		/**
		 * If a MoveBaseDurationEstimator is set, the MoveBaseDurationMetaConstraint will be used by the planner.
		 * @param mbe
		 * @return
		 */
		public CHIMPBuilder mbEstimator(MoveBaseDurationEstimator mbe) {
			this.mbEstimator = mbe;
			return this;
		}
		
		public CHIMP build() throws DomainParsingException {
			return new CHIMP(this);
		}

		/**
		 * 
		 * @param guess Indicates wheter the GuessOrderinMetaConstraint shall be used. Default: false 
		 * @return
		 */
		public CHIMPBuilder guessOrdering(boolean guess) {
			this.guessOrdering  = guess;
			return this;
		}
		
		/**
		 * 
		 * @param unification With this option set it tries to unify tasks to already planned tasks. Default: false
		 * @return
		 */
		public CHIMPBuilder htnUnification(boolean unification) {
			this.htnUnification = unification;
			return this;
		}
		
	}
	
	private CHIMP(CHIMPBuilder builder) throws DomainParsingException {
		
		problemParser = new ProblemParser(builder.problemPath);
		domain = new HybridDomain(builder.domainPath);
		
		int[] ingredients = new int[] {1, domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
		symbols[1] = problemParser.getArgumentSymbols();
		
		planner = new HTNPlanner(origin,  horizon,  0, symbols, ingredients);
		planner.setTypesInstancesMap(problemParser.getTypesInstancesMap());
		domain.parseDomain(planner);  // loads the domain into the planner
		
		initMetaConstraints(builder.htnValOH, builder.mbEstimator, builder.guessOrdering, builder.htnUnification);
		
		// create initial state:
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		problemParser.createState(fluentSolver, domain);
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
	}
	
	public boolean generatePlan() {
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		long endTime = System.nanoTime();
		planningTime = ((endTime - startTime) / 1000000);
		
		foundPlan = result;
		return result;
	}
	
	/**
	 * Draws the search space of the meta-CSP.
	 */
	public void drawSearchSpace() {
		planner.draw();
	}
	
	/**
	 * Gets a description of the involved constraint solvers.
	 */
	public String printDescription() {
		return planner.getDescription();
	}
	
	/**
	 * Draws the plan hierarchy as a constraint network.
	 */
	public void drawHierarchy() {
		ConstraintNetwork cn = new ConstraintNetwork(null);
		for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
			if (con instanceof FluentConstraint) {
				FluentConstraint fc = (FluentConstraint) con;
				if (fc.getType() == FluentConstraint.Type.MATCHES) {
					fc.getFrom().setMarking(markings.UNIFIED);
					cn.addConstraint(fc);
				} else if (fc.getType() == FluentConstraint.Type.DC) {
					cn.addConstraint(fc);
				}
			}
		}
		ConstraintNetwork.draw(cn);
	}
	
	public Variable[] extractActions() {
		if (foundPlan) {
			ArrayList<Variable> plan = new ArrayList<Variable>();
			for (Variable var : fluentSolver.getVariables()) {
				String component = var.getComponent();
				if (component == null) {
					plan.add(var);
				}
				else if (component.equals("Activity")) {
					plan.add(var);
				}
			}
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
			return planVector;
		} else {
			return new Variable[0];
		}
	}
	
	/**
	 * Prints the plan's full hierarchy.
	 */
	public void printFullPlan() {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	/**
	 * Prints statistics about the found plan on printstream.
	 * @param ps where the statistics will be printed on.
	 */
	public void printStats(PrintStream ps) {
		if (foundPlan) {
			int opCount = 0;
			int mCount = 0;
			for (Variable var : fluentSolver.getVariables()) {
				String component = var.getComponent();
				if (component != null) {
					if (component.equals("Activity")) {
						opCount++;
					} else if (component.equals("Task")) {
						mCount++;
					}
				}
			}
			ps.println("Planning time: " + this.getPlanningTime() + " ms");
			ps.println("#Ops: " + opCount);
			ps.println("#Compound Tasks: " + mCount);
			ps.println("#Fluents: " + fluentSolver.getVariables().length);
			ps.println("#FluentConstraints: " + fluentSolver.getConstraints().length);
		} else {
			ps.println("Could not find a plan.");
		}
	}
	
	private void initMetaConstraints(ValueOrderingH valOH, MoveBaseDurationEstimator mbEstimator, boolean guessOrdering, boolean htnUnification) {
		
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.addOperators(domain.getOperators());
		htnConstraint.addMethods(domain.getMethods());
		htnConstraint.setResourceUsages(domain.getFluentResourceUsages());
		if (htnUnification) {
			htnConstraint.enableUnification();
		}
		
		for (FluentScheduler fs : domain.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : domain.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		if (guessOrdering) {
			ValueOrderingH guessOH = new GuessOrderingValOH();
			GuessOrderingMetaConstraint ordConstraint = new GuessOrderingMetaConstraint(guessOH);
			planner.addMetaConstraint(ordConstraint);
		}
		
		if (mbEstimator != null) {
			MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
			planner.addMetaConstraint(mbConstraint);
		}
		
		planner.addMetaConstraint(htnConstraint);
	}

	public HTNPlanner getPlanner() {
		return planner;
	}

	public FluentNetworkSolver getFluentSolver() {
		return fluentSolver;
	}

	/**
	 * @return time in ms that it took to generate the recent plan.
	 */
	public long getPlanningTime() {
		return planningTime;
	}
}
