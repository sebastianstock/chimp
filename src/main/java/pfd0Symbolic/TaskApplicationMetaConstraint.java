package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class TaskApplicationMetaConstraint extends MetaConstraint {
	

	public enum markings {UNPLANNED, SELECTED, PLANNED, OPEN, CLOSED, UNJUSTIFIED, JUSTIFIED}; 
	
	public TaskApplicationMetaConstraint() {
		super(null, null);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6968387716912116132L;

	
	/** 
	 * @return All {@link MetaVariable}s with the marking SELECTED and which have 
	 * no unplanned or selected predecessors.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that has the marking UNPLANNED and that has no unplanned predecessors 
		// a ConstraintNetwork is built.
		// this becomes a task.
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking() != null && var.getMarking().equals(markings.SELECTED)) {
				if (checkPredecessors(var, groundSolver)) {  // only add it if there are no predecessors
					ConstraintNetwork nw = new ConstraintNetwork(null);
					nw.addVariable(var);
					ret.add(nw);
				}
			}
		}
		System.out.println("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	
	}
	
	
	/**
	 * Checks if a Variable has a Before with an UNPLANNED task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkPredecessors(Variable var, 
			FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
			Object marking = flc.getScope()[0].getMarking();
			if ( (marking == markings.UNPLANNED) || (marking == markings.SELECTED) ){
				return false;
			}	 
		}
		return true;
	}

	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariables}.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		// possible constraint networks
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent fl = (Fluent)problematicNetwork.getVariables()[0];
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
//		((TypedCompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
//		((TypedCompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagatePredicateNames();
		
		logger.info("getMetaValues for TaskApplicationMetaConstraint: " + fl);
		// get the used operator/method
		PlanReportroryItem usedItem = this.getUsedPlanReportroiryItem(fl);
		ConstraintNetwork newResolver = usedItem.expandOnlyTail(fl,  groundSolver);
		if (newResolver != null) {
			ret.add(newResolver);
		}
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	private PlanReportroryItem getUsedPlanReportroiryItem(Fluent fl) {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		for (FluentConstraint flc : groundSolver.getFluentConstraintsOfTypeTo(fl, FluentConstraint.Type.UNARYAPPLIED)) {
			return flc.getPlannedWith();
		}
		return null;
	}

	/**
	 * Sets the marking of the task to DECOMPOSED (compound) or PLANNED (primitive)
	 */
	@Override
	public void markResolvedSub(MetaVariable metaVariable,
			ConstraintNetwork metaValue) {
		// TODO if it is a primitive task, set the marking to PLANNED
		metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.PLANNED);
	}

	@Override
	public void draw(ConstraintNetwork network) {
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return this.metaCS.getConstraintSolvers()[0];
	}

	@Override
	public String toString() {
		return "PFD0MetaConstraint";
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
