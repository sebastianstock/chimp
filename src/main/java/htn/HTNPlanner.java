package htn;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomainPlanner;
import integers.IntegerConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;
import unify.CompoundSymbolicValueConstraint;

import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;


public class HTNPlanner extends MetaConstraintSolver implements HybridDomainPlanner {

	private static final long serialVersionUID = 8031573555691611305L;
	
	private Map<String, String[]> typesInstancesMap;
	private final FluentNetworkSolver groundSolver;
	
	private Logger logger = MetaCSPLogging.getLogger(HTNPlanner.class);

	public HTNPlanner(long origin, long horizon, long animationTime, String[][] symbols, 
			int[] symbolicIngredients) {
		// Currently only FluentConstraints. Other constraint should be added later.
		super(new Class[] {FluentConstraint.class}, 
				animationTime, 
				new FluentNetworkSolver(origin, horizon, symbols, symbolicIngredients));//, 0, 1000, 2));

		groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
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
		// Currently only FluentConstraints. Other constraint should be added later.
		super(new Class[] {FluentConstraint.class},
				animationTime,
				new FluentNetworkSolver(origin, horizon, symbols, symbolicIngredients, minIntValue, maxIntValue, numIntVars));

		groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
	}

	@Override
	public void preBacktrack() { }

	@Override
	public void postBacktrack(MetaVariable mv) { }

	/**
	 * Defines extra operations that should happen
	 * after retracting a meta-value in the meta-CSP search (e.g., when backtracking).
	 * @param metaVariable The {@link MetaVariable} over which backtracking occurs.
	 * @param metaValue The meta-value that has been retracted.
	 */
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable,
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
	 * @param metaVariable The {@link MetaVariable} over which the search is branching.
	 * @param metaValue The meta-value that has been selected (the branch). 
	 */
	@Override
	protected boolean addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		
		long startTime = System.nanoTime();
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
		
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

		//Involve real variables in the constraints
		for (Constraint con : metaValue.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();  
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			for (int i = 0; i < oldScope.length; i++) {
				if (oldScope[i] instanceof VariablePrototype) {
					if (con instanceof CompoundSymbolicValueConstraint){
						newScope[i] = ((Fluent) metaValue.getSubstitution((VariablePrototype)oldScope[i])).getCompoundSymbolicVariable();
					} else if (con instanceof AllenIntervalConstraint){ 
						newScope[i] = ((Fluent) metaValue.getSubstitution((VariablePrototype)oldScope[i])).getAllenInterval();
					} else if (con instanceof IntegerConstraint){
						VariablePrototype oldScopePrototype = (VariablePrototype)oldScope[i];
						VariablePrototype oldFluentPrototype = (VariablePrototype) oldScopePrototype.getParameters()[0];
						int intVarIndex = (int) oldScopePrototype.getParameters()[1];
						Fluent fl = (Fluent) metaValue.getSubstitution(oldFluentPrototype);
						newScope[i] = fl.getIntegerVariables()[intVarIndex];
					} else {
						newScope[i] = metaValue.getSubstitution((VariablePrototype)oldScope[i]);
					}
				}
				else {
					newScope[i] = oldScope[i];
				}
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

	@Override
	protected double getUpperBound() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// Auto-generated method stub

	}

	@Override
	protected double getLowerBound() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// Auto-generated method stub

	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// Auto-generated method stub

	}

	public Map<String, String[]> getTypesInstancesMap() {
		return typesInstancesMap;
	}

	public void setTypesInstancesMap(Map<String, String[]> typesInstancesMap) {
		this.typesInstancesMap = typesInstancesMap;
	}

	@Override
	public FluentNetworkSolver getFluentNetworkSolver() {
		return (FluentNetworkSolver) getConstraintSolvers()[0];
	}

	public HTNMetaConstraint getHTNMetaConstraint() {
		for (MetaConstraint mc : getMetaConstraints()) {
			if (mc instanceof HTNMetaConstraint)
				return (HTNMetaConstraint) mc;
		}
		return null;
	}

}
