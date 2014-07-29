package pfd0;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;

import pfd0.PFD0MetaConstraint.markings;


public class PFD0Planner extends MetaConstraintSolver {

	public PFD0Planner(long origin, long horizon, long animationTime) {
		// Currently only FluentConstraints. Other constraint should be added later.
		super(new Class[] {FluentConstraint.class}, 
				animationTime, 
				new FluentNetworkSolver(origin, horizon));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8763530402696588255L;

	@Override
	public void preBacktrack() {
		// TODO Do I need to do something here?

	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		if (mv.getMetaConstraint() instanceof PFD0MetaConstraint) {
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.UNPLANNED);
			}
		}
		else if (mv.getMetaConstraint() instanceof PreconditionMetaConstraint) {
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.UNJUSTIFIED);
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
		
		System.out.println("addResolverSub Constraints: ");
		for(Constraint c : metaValue.getConstraints()) {
			System.out.println(c);
		}
		
		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component 
				//  and the second is the type, the third are the arguments of the fluent
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Fluent fluent = (Fluent)groundSolver.createVariable(component);
				
				if (((VariablePrototype) v).getParameters().length > 2) {
					String[] arguments = (String[])((VariablePrototype) v).getParameters()[2];
					fluent.setName(symbol, arguments);
				} else {
					fluent.setName(symbol);
				}

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
					newScope[i] = metaValue.getSubstitution((VariablePrototype)oldScope[i]);
				}
				else {
					newScope[i] = oldScope[i];
				}
			}
			clonedConstraint.setScope(newScope);
			metaValue.removeConstraint(con);
			metaValue.addConstraint(clonedConstraint);
		}
		
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
