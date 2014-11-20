package pfd0Symbolic;

import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import unify.CompoundSymbolicValueConstraint;


public class PFD0Planner extends MetaConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8031573555691611305L;

	public PFD0Planner(long origin, long horizon, long animationTime, String[][] symbols, int[] symbolicingredients) {
		// Currently only FluentConstraints. Other constraint should be added later.
		super(new Class[] {FluentConstraint.class}, 
				animationTime, 
				new FluentNetworkSolver(origin, horizon, symbols, symbolicingredients));
	}


	@Override
	public void preBacktrack() {
		// TODO Do I need to do something here?

	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		if (mv.getMetaConstraint() instanceof TaskApplicationMetaConstraint) {
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.SELECTED);
			}
		}
		else if (mv.getMetaConstraint() instanceof PreconditionMetaConstraint) {
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.UNJUSTIFIED);
			}
		} 
		else if (mv.getMetaConstraint() instanceof TaskSelectionMetaConstraint) {
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.UNPLANNED);
			}
		} 
	}

	/**
	 * Defines extra operations that should happen
	 * after retracting a meta-value in the meta-CSP search (e.g., when backtracking).
	 * @param metaVariable The {@link MetaVariable} over which backtracking occurs.
	 * @param metaValue The meta-value that has been retracted.
	 */
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		Logger logger = MetaCSPLogging.getLogger(PFD0Planner.class);
		logger.finest("START RETRACT_RESOLVER_SUB");
		long startTime = System.nanoTime();
		
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
		
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
				((FluentConstraint) c).getTo().setMarking(markings.OPEN);
			}
		}
		long endTime = System.nanoTime();
		logger.finest("END RECTRACT_RESOLVER_SUB Took: " + ((endTime - startTime) / 1000000) + " ms");
		
		TaskSelectionMetaConstraint ts = null;
		TaskApplicationMetaConstraint ta = null;
		for (MetaConstraint mcon : this.metaConstraints) {
			if (mcon instanceof TaskSelectionMetaConstraint) ts = (TaskSelectionMetaConstraint)mcon;
			else if (mcon instanceof TaskApplicationMetaConstraint) ta = (TaskApplicationMetaConstraint)mcon;
		}

		//Set resource usage if necessary
		// TODO
//		for (Variable v : varsToRemove) {
//			for (SimpleReusableResourceFluent rr : ts.getCurrentReusableResourcesUsedByActivity(v)) {
//				rr.removeUsage((Fluent)v);
//			}
//		}

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
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
		
//		System.out.println("addResolverSub Constraints: ");
//		for(Constraint c : metaValue.getConstraints()) {
//			System.out.println(c);
//		}
		
		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component 
				//  and the second is the type, the third are the arguments of the fluent
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Fluent fluent = (Fluent)groundSolver.createVariable(component);
				
				String[] arguments = (String[])((VariablePrototype) v).getParameters()[2];
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
					((FluentConstraint) con).getTo().setMarking(markings.CLOSED);
				}
			}
		}
		
		
//		TaskSelectionMetaConstraint ts = null;
//		TaskApplicationMetaConstraint ta = null;
//		for (MetaConstraint mcon : this.metaConstraints) {
//			if (mcon instanceof TaskSelectionMetaConstraint) ts = (TaskSelectionMetaConstraint)mcon;
//			else if (mcon instanceof TaskApplicationMetaConstraint) ta = (TaskApplicationMetaConstraint)mcon;
//		}

		//Set resource usage if necessary
		// OBSOLETE
//		for (Variable v : metaValue.getVariables()) {
//			for (SimpleReusableResourceFluent rr : ts.getCurrentReusableResourcesUsedByActivity(v)) {
//				//rr.setUsage(metaValue.getSubstitution(v));
//				rr.setUsage((Fluent)v);
//			}
//		}
		
		// Add resource constraints
				
		return true;
	}

	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected double getLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// TODO Auto-generated method stub

	}

}
