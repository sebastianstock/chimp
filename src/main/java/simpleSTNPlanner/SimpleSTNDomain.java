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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

public class SimpleSTNDomain extends MetaConstraint {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7226230284206806067L;

	private Vector<SimpleSTNOperator> operators;
	private Vector<SimpleSTNMethod> methods;

	private String name;
	
	public enum markings {ACTIVE, CLOSED, EXECUTED, PLANNED, NEW, UNJUSTIFIED, JUSTIFIED};
	

	public SimpleSTNDomain(String domainName) {
		super(null, null);
		this.name = domainName;
		this.operators = new Vector<SimpleSTNOperator>();
		this.methods = new Vector<SimpleSTNMethod>();
	}
	
	public void addOperator(SimpleSTNOperator r) {
		operators.add(r);
	}
	
	public SimpleSTNOperator[] getOperators() {
		return operators.toArray(new SimpleSTNOperator[operators.size()]);
	}		
	
	public SimpleSTNMethod[] getMethods() {
		return methods.toArray(new SimpleSTNMethod[methods.size()]);
	}

	public void addMethod(SimpleSTNMethod m) {
		this.methods.add(m);
	}

	/**
	 * Returns all Activities that have the marking NEW.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that is marked as NEW an ActivityNetwork is built
		// this becomes a task
		for (Variable task : groundSolver.getVariables()) {
			if (task.getMarking() != null && task.getMarking().equals(markings.NEW)) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(task);
				ret.add(nw);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	/**
	 * Filters for Variables with the marking ACTIVE.
	 */
	private Variable[] getActiveVariables(Variable[] vars) {
		ArrayList<Variable> ret = new ArrayList<Variable>();
		for (Variable var: vars) {
			if (var.getMarking() == markings.ACTIVE) {
				ret.add(var);
			}
		}
		return ret.toArray(new Variable[ret.size()]);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable, int initialTime) {
		return getMetaValues(metaVariable);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Activity currentActivity = (Activity)problematicNetwork.getVariables()[0]; 

		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Variable[] activeVariables = getActiveVariables(groundSolver.getVariables());
		
		for (SimpleSTNOperator op : operators) {
			if (op.checkApplicability(currentActivity, activeVariables)) {
				ConstraintNetwork newResolver = op.expand(currentActivity, groundSolver, activeVariables);
				retPossibleConstraintNetworks.add(newResolver);
			}
		}
		for (SimpleSTNMethod m : methods) {
			if (m.checkApplicability(currentActivity, activeVariables)) {
				ConstraintNetwork newResolver = m.expand(currentActivity, groundSolver, activeVariables);
				retPossibleConstraintNetworks.add(newResolver);
			}
		}
		
		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
//		ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
//		return new ConstraintNetwork[] {nullActivityNetwork};  // TODO does this make sense or just return null???
		return null; // no solution found
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		con.getConstraintNetwork().getVariables()[0].setMarking(markings.EXECUTED);
	}

	@Override
	public void draw(ConstraintNetwork network) {

	}

	@Override
	public String toString() {
		return "SimpleDomain " + this.name;
	}

	@Override
	public String getEdgeLabel() {
		return null;
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		return false;
	}

}
