package htn;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.VariablePrototype;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.fluent.FluentStoreImpl;
import chimp.logging.ChimpLogging;
import chimp.model.FluentId;
import chimp.model.FluentMarking;
import chimp.model.FluentStore;
import chimp.model.SubVariableKind;
import chimp.search.ChimpFlaw;
import chimp.search.ChimpResolver;
import chimp.search.ChimpResolverAdapter;
import chimp.search.ChimpSearchEngine;
import chimp.search.FlawProvider;
import chimp.search.SearchHooks;
import hybridDomainParsing.HybridDomainPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;


public class HTNPlanner implements HybridDomainPlanner, SearchHooks {

	private Map<String, String[]> typesInstancesMap;
	private final FluentNetworkSolver groundSolver;
	private final FluentStore fluentStore;
	private final List<FlawProvider> flawProviders = new ArrayList<>();
	private final long animationTime;
	private ChimpSearchEngine searchEngine;

	private Logger logger = ChimpLogging.getLogger(HTNPlanner.class);

	public HTNPlanner(long origin, long horizon, long animationTime, String[][] symbols,
			int[] symbolicIngredients) {
		this.animationTime = animationTime;
		this.groundSolver = new FluentNetworkSolver(origin, horizon, symbols, symbolicIngredients);
		this.fluentStore = new FluentStoreImpl(this.groundSolver);
	}

	/**
	 * HTNPlanner with internal IntegerConstraintSolver.
	 * @param origin Origin for AllenIntervalNetworkSolver
	 * @param horizon Horizon for AllenIntervalNetworkSolver
	 * @param symbols Symbols of the CompoundSymbolicVariableConstraintSolver
	 * @param symbolicIngredients Number of internal variables per Fluent created by the CompoundSymbolicVariableConstraintSolver
	 * @param minIntValue Minimum value for the integer variables
	 * @param maxIntValue Maximum value for the integer variables
	 * @param numIntVars Number of IntegerVariables per fluent created by the IntegerConstraintSolver
	 */
	public HTNPlanner(long origin, long horizon, long animationTime, String[][] symbols,
					  int[] symbolicIngredients, int minIntValue, int maxIntValue, int numIntVars ) {
		this.animationTime = animationTime;
		this.groundSolver = new FluentNetworkSolver(origin, horizon, symbols, symbolicIngredients,
				minIntValue, maxIntValue, numIntVars);
		this.fluentStore = new FluentStoreImpl(this.groundSolver);
	}

	/**
	 * Adds a meta-constraint to this planner. Replaces
	 * {@code MetaConstraintSolver.addMetaConstraint}: registers the
	 * meta-constraint and sets its ground solver directly (instead of
	 * via {@code setMetaSolver}).
	 */
	public void addMetaConstraint(FlawProvider provider) {
		flawProviders.add(provider);
		if (provider instanceof GroundSolverHolder holder) {
			holder.setGroundSolver(groundSolver);
		}
	}

	public FlawProvider[] getMetaConstraints() {
		return flawProviders.toArray(new FlawProvider[0]);
	}

	public ConstraintSolver[] getConstraintSolvers() {
		return new ConstraintSolver[] { groundSolver };
	}

	public String getDescription() {
		return "[" + this.getClass().getSimpleName() + " groundSolver: "
				+ groundSolver.getDescription() + "]";
	}

	@Override
	public void preBacktrack() { }

	@Override
	public void postBacktrack(ChimpFlaw flaw) { }

	/**
	 * Initiates backtracking search via the CHIMP-owned {@link ChimpSearchEngine}.
	 */
	public boolean backtrack() {
		List<FlawProvider> providers = new ArrayList<>(flawProviders);
		searchEngine = new ChimpSearchEngine(providers, this, animationTime);
		return searchEngine.backtrack();
	}

	/**
	 * Draws the search tree built by the last {@link #backtrack()} call.
	 */
	public void draw() {
		if (searchEngine != null) {
			searchEngine.draw();
		}
	}

	/**
	 * Defines extra operations that should happen
	 * after retracting a meta-value in the meta-CSP search (e.g., when backtracking).
	 * @param metaVariable The flaw network over which backtracking occurs.
	 * @param metaValue The meta-value that has been retracted.
	 */
	@Override
	public void retractResolverSub(ChimpFlaw flaw,
			ChimpResolver resolver) {

		long startTime = System.nanoTime();
		ConstraintNetwork metaValue = resolver.delegate;

		// remove added fluents
		Vector<Variable> varsToRemove = new Vector<Variable>();
		for (Variable v : metaValue.getVariables()) {
			boolean inFlaw = false;
			for (chimp.model.FluentId flawId : flaw.taskIds()) {
				if (v.getID() == flawId.value()) {
					inFlaw = true;
					break;
				}
			}
			if (!inFlaw) {
				if (v instanceof VariablePrototype vp) {
					Variable vReal = metaValue.getSubstitution(vp);
					if (vReal != null) {
							varsToRemove.add(vReal);
					}
				}
			}
		}
		groundSolver.removeVariables(varsToRemove.toArray(new Variable[varsToRemove.size()]));

		// change CLOSED fluents back to OPEN
		for(Constraint c : metaValue.getConstraints()) {
			if ((c instanceof FluentConstraint) &&
					(((FluentConstraint) c).getType() == FluentConstraint.Type.CLOSES)) {
				((FluentConstraint) c).getTo().setMarking(FluentMarking.OPEN.ordinal());
			}
		}

		long endTime = System.nanoTime();
		logger.finest("RECTRACT_RESOLVER_SUB Took: " + ((endTime - startTime) / 1000000) + " ms");
	}

	/**
	 * Defines additional operations that should happen before
	 * adding a meta-value in the meta-CSP search (e.g., when branching).
	 *
	 * Make real {@link Variable}s from {@link VariablePrototype}s.
	 * Involve real {@link Variable}s in the Constraints.
	 * @param metaVariable The flaw network over which the search is branching.
	 * @param metaValue The meta-value that has been selected (the branch).
	 */
	@Override
	public boolean addResolverSub(ChimpFlaw flaw,
			ChimpResolver resolver) {

		long startTime = System.nanoTime();
		ConstraintNetwork metaValue = resolver.delegate;

		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype vp) {

				Object[] params = vp.getParameters();
				// IntegerConstraints use different kind of variable prototype
				if (params[0] instanceof VariablePrototype) {
					continue;
				}

				// 	Parameters for real instantiation: the first is the component
				//  and the second is the type, the third are the arguments of the fluent
				String component = (String) params[0];
				String symbol = (String) params[1];
				String[] arguments = (String[]) params[2];

				// Create fluent via FluentStore abstraction
				fluentStore.createFluent(component, symbol, arguments,
						FluentMarking.fromIntValue(v.getMarking()));

				// Get the last-created Fluent for substitution tracking
				Fluent fluent = (Fluent) groundSolver.getVariables()[
						groundSolver.getVariables().length - 1];
				fluent.setMarking(v.getMarking());
				metaValue.addSubstitution(vp, fluent);
			}
		}

		// Read sub-variable kind tags (set during compilation)
		Map<Constraint, SubVariableKind> kindMap = null;
		Object annotation = metaValue.getAnnotation();
		if (annotation instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<Constraint, SubVariableKind> cast =
					(Map<Constraint, SubVariableKind>) annotation;
			kindMap = cast;
		}

		//Involve real variables in the constraints
		for (Constraint con : metaValue.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			SubVariableKind kind = kindMap != null
					? kindMap.getOrDefault(con, SubVariableKind.FLUENT)
					: ChimpResolverAdapter.subVariableKindOf(con);
			for (int i = 0; i < oldScope.length; i++) {
				newScope[i] = ChimpResolverAdapter.resolveScopeVariable(
						oldScope[i], kind, metaValue);
			}
			clonedConstraint.setScope(newScope);
			metaValue.removeConstraint(con);
			metaValue.addConstraint(clonedConstraint);
		}

		// set marking of closed fluents to CLOSED
		for (Constraint con : metaValue.getConstraints()) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint) con).getType() == FluentConstraint.Type.CLOSES) {
					((FluentConstraint) con).getTo().setMarking(FluentMarking.CLOSED.ordinal()); // TODO These markings are not really used and could be removed
				}
			}
		}

		long endTime = System.nanoTime();
		logger.finest("ADD_RESOLVER_SUB Took: " + ((endTime - startTime) / 1000000) + " ms");

		return true;
	}

	public Map<String, String[]> getTypesInstancesMap() {
		return typesInstancesMap;
	}

	public void setTypesInstancesMap(Map<String, String[]> typesInstancesMap) {
		this.typesInstancesMap = typesInstancesMap;
	}

	@Override
	public FluentNetworkSolver getFluentNetworkSolver() {
		return groundSolver;
	}

	public HTNMetaConstraint getHTNMetaConstraint() {
		for (FlawProvider provider : flawProviders) {
			if (provider instanceof HTNMetaConstraint)
				return (HTNMetaConstraint) provider;
		}
		return null;
	}

}
