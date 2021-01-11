package planner;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.PlanReportroryItem;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.DomainParsingException;
import postprocessing.PlanExtractor;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.classic.antlr.ChimpClassicReader;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import ui.PlanHierarchyFrame;
import unify.CompoundSymbolicVariableConstraintSolver;

import java.awt.*;
import java.io.PrintStream;
import java.util.*;

/**
 * A wrapper for the HTN-Planner that bundles the features of the planner CHIMP.
 * 
 * @author Sebastian Stock
 *
 */
public class CHIMP {

	private HTNPlanner planner;
	private FluentNetworkSolver fluentSolver;
	private boolean foundPlan = false;
	private long planningTime = -1;
	private PlanHierarchyFrame planHierarchyFrame = null;

	public static class CHIMPBuilder {
		
		private final CHIMPProblem problem;
		private final ClassicHybridDomain domain;
		private ValueOrderingH htnValOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
		private MoveBaseDurationEstimator mbEstimator;
		private boolean guessOrdering = false;
		boolean htnUnification = false;
		private long origin = 0;
		private long horizon = 600000;

		/**
		 * 
		 * @param domain Planning domain.
		 * @param problem Problem to plan.
		 *
		 */
		public CHIMPBuilder(ClassicHybridDomain domain, CHIMPProblem problem) {
			this.domain = domain;
			this.problem = problem;
		}

		private Set<String> extractSymbolicConstants(ClassicHybridDomain domain) {
			Set<String> constants = new HashSet<>();
			for (PlanReportroryItem pri : domain.getOperators()) {
				constants.addAll(pri.getSymbolicConstants());
			}
			for (PlanReportroryItem pri : domain.getMethods()) {
				constants.addAll(pri.getSymbolicConstants());
			}
			return constants;
		}

		/**
		 * @param domainPath Path to the domain file.
		 * @param problemPath Path to the problem file.
		 */
		public CHIMPBuilder(String domainPath, String problemPath) throws DomainParsingException {
			this.problem = new ProblemParser(problemPath); // TODO Create problem parser with antlr
			this.domain = ChimpClassicReader.parseDomainFromFile(domainPath, problem.getTypesInstancesMap());
			this.problem.addArgumentSymbols(extractSymbolicConstants(this.domain));
		}
		
		/**
		 * 
		 * @param valOH The heuristic that shall be used by the HTNPlanner. Default: UnifyFewestsubsEarliesttasksNewestbindingsValOH
		 * @return This {@link CHIMPBuilder}.
		 */
		public CHIMPBuilder valHeuristic(ValueOrderingH valOH) {
			this.htnValOH = valOH;
			return this;
		}
		
		/**
		 * If a MoveBaseDurationEstimator is set, the MoveBaseDurationMetaConstraint will be used by the planner.
		 * @param mbe The {@link MoveBaseDurationEstimator} to estimate the duration of navigation.
		 * @return This {@link CHIMPBuilder}.
		 */
		public CHIMPBuilder mbEstimator(MoveBaseDurationEstimator mbe) {
			this.mbEstimator = mbe;
			return this;
		}

		public CHIMPBuilder origin(long origin) {
			this.origin = origin;
			return this;
		}

		public CHIMPBuilder horizon(long horizon) {
			this.horizon = horizon;
			return this;
		}
		
		public CHIMP build() {
			return new CHIMP(this);
		}

		/**
		 * 
		 * @param guess Indicates whether the GuessOrderinMetaConstraint shall be used. Default: false
		 * @return This {@link CHIMPBuilder}.
		 */
		public CHIMPBuilder guessOrdering(boolean guess) {
			this.guessOrdering  = guess;
			return this;
		}
		
		/**
		 * 
		 * @param unification With this option set it tries to unify tasks to already planned tasks. Default: false
		 * @return This {@link CHIMPBuilder}.
		 */
		public CHIMPBuilder htnUnification(boolean unification) {
			this.htnUnification = unification;
			return this;
		}
		
	}
	
	private CHIMP(CHIMPBuilder builder) {
		
		int[] ingredients = new int[] {1, builder.domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  builder.domain.getPredicateSymbols();
		symbols[1] = builder.problem.getArgumentSymbols();

		if (builder.domain.getMaxIntArgs() > 0) {
			planner = new HTNPlanner(builder.origin,  builder.horizon,  0, symbols, ingredients,
					builder.domain.getMinIntValue(), builder.domain.getMaxIntValue(), builder.domain.getMaxIntArgs());
		} else {
			planner = new HTNPlanner(builder.origin,  builder.horizon,  0, symbols, ingredients);
		}

		planner.setTypesInstancesMap(builder.problem.getTypesInstancesMap());
		
		initMetaConstraints(builder.domain, builder.htnValOH, builder.mbEstimator, builder.guessOrdering, builder.htnUnification);
		
		// create initial state:
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		builder.problem.createState(fluentSolver, builder.domain);
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
	 * 
	 * @return The description of the involved constraint solvers.
	 */
	public String printDescription() {
		return planner.getDescription();
	}
	
	/**
	 * Draws the hierarchy of the plan.
	 */
	public void drawPlanHierarchy(int distx) {
		if (this.planHierarchyFrame != null) { // close the old frame
			this.disposePlanHierarchyFrame();
		}
		Graph<Fluent, FluentConstraint> g = new DirectedSparseMultigraph<Fluent, FluentConstraint>();
		for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
			if (con instanceof FluentConstraint) {
				FluentConstraint fc = (FluentConstraint) con;
				if (fc.getType() == FluentConstraint.Type.DC) {
					g.addEdge(fc, (Fluent) fc.getFrom(), (Fluent) fc.getTo());
				}
			}
		}
		for (Fluent f : g.getVertices()) {
			if (f.getTypeStr().equals(Fluent.ACTIVITY_TYPE_STR))
				f.setColor(Color.BLUE);
			else
				f.setColor(Color.RED);
		}
		try {
			this.planHierarchyFrame = PlanHierarchyFrame.draw(g, distx);
		} catch (Exception e) {
			System.err.println("Caught exception while drawing plan-hierarchy: " + e.toString());
		}
	}
	
	public void disposePlanHierarchyFrame() {
		if (this.planHierarchyFrame != null) { // close the old frame
			this.planHierarchyFrame.setVisible(false);
			this.planHierarchyFrame.dispose();
			System.out.println("Disposed planHierarchyFrame");
		} else {
			System.out.println("no active planHierarchyFrame");
		}
	}
	
	/**
	 * Draws the plan hierarchy as a constraint network.
	 */
	public void drawHierarchyNetwork() {
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
	}
	
	public Variable[] extractActions() {
		if (foundPlan) {
			ArrayList<Variable> plan = new ArrayList<Variable>();
			for (Variable var : fluentSolver.getVariables()) {
				if (var instanceof Fluent) {
					String fluentType = ((Fluent) var).getTypeStr();
					if (fluentType.equals(Fluent.ACTIVITY_TYPE_STR)) {
						plan.add(var);
					}
				}
			}
			Variable[] planVector = plan.toArray(new Variable[plan.size()]);
			Arrays.sort(planVector, new Comparator<Variable>() {
				@Override
				public int compare(Variable o1, Variable o2) {
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
		}
	}
	
	private void initMetaConstraints(ClassicHybridDomain domain, ValueOrderingH valOH,
									 MoveBaseDurationEstimator mbEstimator, boolean guessOrdering,
									 boolean htnUnification) {
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.addOperators( domain.getOperators());
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
