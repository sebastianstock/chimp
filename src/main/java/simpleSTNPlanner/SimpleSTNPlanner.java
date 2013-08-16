/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package simpleSTNPlanner;

import java.util.Vector;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;

import simpleSTNPlanner.SimpleSTNDomain.markings;


public class SimpleSTNPlanner extends MetaConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8039042672928514133L;

	public SimpleSTNPlanner(long origin, long horizon, long animationTime) {
		// Through the constructor, the Scheduler is put in the "InternalSolver variable"
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, animationTime, new ActivityNetworkSolver(origin, horizon, 500));
		// Through the following line, the Scheduler is put in the "nextMetaConstraintSolver" belongin
		// to the MetaConstraintSolver class
	}
	
	@Override
	public void preBacktrack() { }

	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.getConstraintSolvers()[0];
		Vector<Variable> activityToRemove = new Vector<Variable>();
		
		for (Variable v : metaValue.getVariables()) {
			if (!metaVariable.containsVariable(v)) {
				if (v instanceof VariablePrototype) {
					Variable vReal = metaValue.getSubstitution((VariablePrototype)v);
					if (vReal != null) {
						activityToRemove.add(vReal);
					}
				}
			}
		}

		SimpleSTNDomain sd = (SimpleSTNDomain)this.metaConstraints.elementAt(0);
		for (Variable v : activityToRemove) {
			// TODO
//			for (SimpleReusableResource rr : sd.getCurrentReusableResourcesUsedByActivity((Activity)v)) {
//				rr.removeUsage((Activity)v);
//			}
		}
		
		groundSolver.removeVariables(activityToRemove.toArray(new Variable[activityToRemove.size()]));
	}

	@Override
	protected boolean addResolverSub(ConstraintNetwork currentProblematicConstraintNetwork, ConstraintNetwork possibleOperatorConstraintNetwork) {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.getConstraintSolvers()[0];

		//Make real variables from variable prototypes
		for (Variable v :  possibleOperatorConstraintNetwork.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component itself, the second is
				//	the symbol of the Activity to be instantiated
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Activity tailActivity = (Activity)groundSolver.createVariable(component);
				tailActivity.setSymbolicDomain(symbol);
				tailActivity.setMarking(v.getMarking());
				possibleOperatorConstraintNetwork.addSubstitution((VariablePrototype)v, tailActivity);
			}
		}

		//Involve real variables in the constraints
		for (Constraint con : possibleOperatorConstraintNetwork.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();  
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			for (int i = 0; i < oldScope.length; i++) {
				if (oldScope[i] instanceof VariablePrototype) newScope[i] = possibleOperatorConstraintNetwork.getSubstitution((VariablePrototype)oldScope[i]);
				else newScope[i] = oldScope[i];
			}
			clonedConstraint.setScope(newScope);
			possibleOperatorConstraintNetwork.removeConstraint(con);
			possibleOperatorConstraintNetwork.addConstraint(clonedConstraint);
		}
		
		//Set resource usage if necessary
		for (Variable v : possibleOperatorConstraintNetwork.getVariables()) {
			SimpleSTNDomain sd = (SimpleSTNDomain)this.metaConstraints.elementAt(0);
			// TODO
//			for (SimpleReusableResource rr : sd.getCurrentReusableResourcesUsedByActivity(v)) {
//				rr.setUsage((Activity)v);
//			}
		}
		
		return true;
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		if (mv.getMetaConstraint() instanceof SimpleSTNDomain)
			for (Variable v : mv.getConstraintNetwork().getVariables()) v.setMarking(markings.NEW);
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
