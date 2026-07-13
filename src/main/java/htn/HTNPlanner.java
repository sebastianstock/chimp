package htn;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomainPlanner;
import chimp.adapter.metacsp.MetaCspFlawProviderAdapter;
import chimp.adapter.metacsp.MetaCspResolverAdapter;
import chimp.model.SubVariableKind;
import chimp.search.ChimpSearchEngine;
import chimp.search.FlawProvider;
import chimp.search.SearchHooks;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import chimp.logging.ChimpLogging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;


public class HTNPlanner implements HybridDomainPlanner, SearchHooks {

	private Map<String, String[]> typesInstancesMap;
	private final FluentNetworkSolver groundSolver;
	private final List<MetaConstraint> metaConstraints = new ArrayList<>();
	private final long animationTime;
	private ChimpSearchEngine searchEngine;

	private Logger logger = ChimpLogging.getLogger(HTNPlanner.class);

	public HTNPlanner(long origin, long horizon, long animationTime, String[][] symbols,
			int[] symbolicIngredients) {
		this.animationTime = animationTime;
		this.groundSolver = new FluentNetworkSolver(origin, horizon, symbols, symbolicIngredients);
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
	}

	/**
	 * Adds a meta-constraint to this planner. Replaces
	 * {@code MetaConstraintSolver.addMetaConstraint}: registers the
	 * meta-constraint and sets its ground solver directly (instead of
	 * via {@code setMetaSolver}).
	 */
	public void addMetaConstraint(MetaConstraint metaConstraint) {
		metaConstraints.add(metaConstraint);
		if (metaConstraint instanceof GroundSolverHolder holder) {
			holder.setGroundSolver(groundSolver);
		}
	}

	public MetaConstraint[] getMetaConstraints() {
		return metaConstraints.toArray(new MetaConstraint[0]);
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
	public void postBacktrack(ConstraintNetwork flawNetwork) { }

	/**
	 * Initiates backtracking search via the CHIMP-owned {@link ChimpSearchEngine}.
	 */
	public boolean backtrack() {
		List<FlawProvider> providers = new ArrayList<>(metaConstraints.size());
		for (MetaConstraint mc : metaConstraints) {
			providers.add(new MetaCspFlawProviderAdapter(mc));
		}
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
	public void retractResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {

		long startTime = System.nanoTime();

		// remove added fluents
		Vector<Variable> varsToRemove = new Vector<Variable>();
		for (Variable v : metaValue.getVariables()) {
			if (!metaVariable.containsVariable(v)) {
				if (v instanceof VariablePrototype) {
					Variable vReal = metaValue.getSubstitution((VariablePrototype)v);
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
				((FluentConstraint) c).getTo().setMarking(HTNMetaConstraint.markings.OPEN);
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
	public boolean addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {

		long startTime = System.nanoTime();

		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype) {
				VariablePrototype vp = (VariablePrototype) v;

				// IntegerConstraints use different kind of variable prototype
				if (vp.getParameters()[0] instanceof VariablePrototype) {
					continue;
				}

				// 	Parameters for real instantiation: the first is the component
				//  and the second is the type, the third are the arguments of the fluent
				String component = (String) vp.getParameters()[0];
				String symbol = (String) vp.getParameters()[1];
				Fluent fluent = (Fluent)groundSolver.createVariable(component);

				String[] arguments = (String[]) vp.getParameters()[2];
				fluent.setName(symbol, arguments);

				fluent.setMarking(v.getMarking());
				metaValue.addSubstitution((VariablePrototype)v, fluent);
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
					: MetaCspResolverAdapter.subVariableKindOf(con);
			for (int i = 0; i < oldScope.length; i++) {
				newScope[i] = MetaCspResolverAdapter.resolveScopeVariable(
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
					((FluentConstraint) con).getTo().setMarking(HTNMetaConstraint.markings.CLOSED); // TODO These markings are not really used and could be removed
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
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof HTNMetaConstraint)
				return (HTNMetaConstraint) mc;
		}
		return null;
	}

}
