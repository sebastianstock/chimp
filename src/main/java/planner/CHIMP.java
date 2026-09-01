package planner;

import java.awt.Color;
import java.io.PrintStream;
import java.util.*;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.Variable;
import chimp.kernel.VariablePrototype;
import chimp.search.SearchTreeEdge;
import chimp.search.SearchTreeNode;
import edu.uci.ics.jung.graph.DelegateForest;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.fluent.ProblemCompiler;
import chimp.kernel.fluent.ResourceCapacityAdapter;
import chimp.kernel.fluent.StateVariableAdapter;
import chimp.kernel.integer.IntegerVariable;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.FluentMarking;
import chimp.model.PlannedAction;
import chimp.model.ResourceCapacity;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import externalPathPlanning.MoveBaseDurationEstimator;
import externalPathPlanning.MoveBaseMetaConstraint;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.PlanReportroryItem;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.ProblemParser;
import hybridDomainParsing.classic.antlr.ChimpClassicReader;
import postprocessing.PlanExtractor;
import ui.PlanHierarchyFrame;
import ui.SearchTreeFrame;

/**
 * A wrapper for the HTN-Planner that bundles the features of the planner CHIMP.
 * 
 * @author Sebastian Stock
 *
 */
public class CHIMP {
	private static final ResourceCapacityAdapter RESOURCE_CAPACITY_ADAPTER =
			new ResourceCapacityAdapter();
	private static final StateVariableAdapter STATE_VARIABLE_ADAPTER =
			new StateVariableAdapter();
	private static final ProblemCompiler PROBLEM_COMPILER =
			new ProblemCompiler();

	private HTNPlanner planner;
	private FluentNetworkSolver fluentSolver;
	private boolean foundPlan = false;
	private long planningTime = -1;
	private PlanHierarchyFrame planHierarchyFrame = null;
	private SearchTreeFrame searchTreeFrame = null;

	public static class CHIMPBuilder {
		
		private final CHIMPProblem problem;
		private final ClassicHybridDomain domain;
		private Comparator<ConstraintNetwork> htnValOH = new UnifyFewestsubsEarliesttasksNewestbindingsValOH();
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
		public CHIMPBuilder valHeuristic(Comparator<ConstraintNetwork> valOH) {
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
		fluentSolver = planner.getFluentNetworkSolver();
		PROBLEM_COMPILER.compile(builder.problem.getProblemDefinition(), fluentSolver, builder.domain);
		fluentSolver.getSymbolicSolver().propagateAllSub();
	}
	
	public boolean generatePlan() {
		VariablePrototype.resetIds();
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
		DelegateForest<SearchTreeNode, SearchTreeEdge> tree = planner.getSearchTree();
		if (tree == null) {
			System.err.println("No search tree to draw (run generatePlan() first)");
			return;
		}
		if (this.searchTreeFrame != null) { // close the old frame
			this.disposeSearchTreeFrame();
		}
		try {
			this.searchTreeFrame = SearchTreeFrame.draw(tree);
		} catch (Exception e) {
			System.err.println("Caught exception while drawing search tree: " + e.toString());
		}
	}

	public void disposeSearchTreeFrame() {
		if (this.searchTreeFrame != null) { // close the old frame
			this.searchTreeFrame.setVisible(false);
			this.searchTreeFrame.dispose();
		} else {
			System.out.println("no active searchTreeFrame");
		}
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
		ConstraintNetwork cn = new ConstraintNetwork();
		for (Constraint con : fluentSolver.getConstraintNetwork().getConstraints()) {
			if (con instanceof FluentConstraint) {
				FluentConstraint fc = (FluentConstraint) con;
				if (fc.getType() == FluentConstraint.Type.MATCHES) {
					fc.getFrom().setMarking(FluentMarking.UNIFIED.ordinal());
					cn.addConstraint(fc);
				} else if (fc.getType() == FluentConstraint.Type.DC) {
					cn.addConstraint(fc);
				}
			}
		}
		// Text-only dump. Pre-removal master opened Meta-CSP's ConstraintNetworkFrame
		// here; CHIMP has no graphical constraint-network view yet (the graphical
		// hierarchy view is drawPlanHierarchy()/PlanHierarchyFrame).
		System.out.println("Hierarchy network: " + cn);
	}
	
	public PlannedAction[] extractActions() {
		if (foundPlan) {
			ArrayList<Fluent> plan = new ArrayList<Fluent>();
			for (Variable var : fluentSolver.getVariables()) {
				if (var instanceof Fluent) {
					String fluentType = ((Fluent) var).getTypeStr();
					if (fluentType.equals(Fluent.ACTIVITY_TYPE_STR)) {
						plan.add((Fluent) var);
					}
				}
			}
			plan.sort(Comparator.comparingLong(f -> f.getTemporalVariable().getEST()));

			PlannedAction[] result = new PlannedAction[plan.size()];
			for (int i = 0; i < plan.size(); i++) {
				result[i] = toPlannedAction(plan.get(i));
			}
			return result;
		} else {
			return new PlannedAction[0];
		}
	}

	private static PlannedAction toPlannedAction(Fluent fluent) {
		CompoundSymbolicVariable csv = fluent.getCompoundSymbolicVariable();
		String name = csv.getSymbolsAt(0)[0];
		int argCount = csv.getInternalVariables().length - 1;
		List<String> arguments = new ArrayList<>(argCount);
		for (int i = 1; i <= argCount; i++) {
			String[] symbols = csv.getSymbolsAt(i);
			String symbol = symbols.length > 0 ? symbols[0] : "";
			// Match CompoundSymbolicVariable.getShortName(): stop at NONESYMBOL ("n")
			if (symbol.isEmpty() || CompoundSymbolicVariable.NONESYMBOL.equals(symbol)) {
				break;
			}
			arguments.add(symbol);
		}

		TemporalVariable interval = fluent.getTemporalVariable();
		long est = interval.getEST();
		long lst = interval.getLST();
		long eet = interval.getEET();
		long let = interval.getLET();

		IntegerVariable[] intVars = fluent.getIntegerVariables();
		List<String> intValues = new ArrayList<>(intVars.length);
		for (IntegerVariable iv : intVars) {
			intValues.add(iv.toString());
		}

		return new PlannedAction(
				fluent.getID(),
				fluent.getComponent(),
				name,
				arguments,
				est, lst, eet, let,
				intValues);
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
	
	private void initMetaConstraints(ClassicHybridDomain domain, Comparator<ConstraintNetwork> valOH,
									 MoveBaseDurationEstimator mbEstimator, boolean guessOrdering,
									 boolean htnUnification) {
		HTNMetaConstraint htnConstraint = new HTNMetaConstraint(valOH);
		htnConstraint.addOperators( domain.getOperators());
		htnConstraint.addMethods(domain.getMethods());
		htnConstraint.setResourceUsages(domain.getFluentResourceUsages());
		if (htnUnification) {
			htnConstraint.enableUnification();
		}
		
		for (var definition : domain.getStateVariableDefinitions()) {
			for (var scheduler : STATE_VARIABLE_ADAPTER.toFluentSchedulers(definition)) {
				planner.addMetaConstraint(scheduler);
			}
		}
		
		for (ResourceCapacity resourceCapacity : domain.getResourceCapacities()) {
			planner.addMetaConstraint(RESOURCE_CAPACITY_ADAPTER.toResourceScheduler(resourceCapacity));
		}
		
		if (guessOrdering) {
			Comparator<ConstraintNetwork> guessOH = new GuessOrderingValOH();
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
