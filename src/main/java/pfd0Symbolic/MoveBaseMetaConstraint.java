package pfd0Symbolic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import resourceFluent.SchedulableFluent;
import resourceFluent.SimpleReusableResourceFluent;
import unify.CompoundSymbolicVariableConstraintSolver;


public class MoveBaseMetaConstraint extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2789371087819357936L;
	private static final String MOVE_BASE_NAME = "!move_base";


	public MoveBaseMetaConstraint() {
		super(null, null);
	}

	
	/** 
	 * @return All {@link MetaVariable}s with the marking UNPLANNED and which have no unplanned 
	 * predecessors.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that has the marking UNPLANNED and that has no unplanned predecessors 
		// a ConstraintNetwork is built.
		// this becomes a task.
		for (Variable var : groundSolver.getVariables()) {
			if (((Fluent) var).getCompoundSymbolicVariable().getPredicateName().equals(MOVE_BASE_NAME)) {
				//			if (var.getMarking() != null && var.getMarking().equals(markings.UNPLANNED)) {
				if (! checkDuration(var, groundSolver)) {
					ConstraintNetwork nw = new ConstraintNetwork(null);
					nw.addVariable(var);
					ret.add(nw);
				}
			}
			//			}
		}
		logger.finest("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	
	}
	

	// Checks if a duration has been set to a variable
	private boolean checkDuration(Variable var, FluentNetworkSolver groundSolver) {
		List<FluentConstraint> durationCons = 
				groundSolver.getFluentConstraintsOfTypeFrom((Fluent) var, FluentConstraint.Type.MOVEDURATION);
		return !durationCons.isEmpty();
	}

	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariables}.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> ret;
		
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent taskFluent = (Fluent)problematicNetwork.getVariables()[0];
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		
		logger.fine("getMetaValues for: " + taskFluent);
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
//		if (taskFluent.getCompoundSymbolicVariable().getPossiblePredicateNames()[0].charAt(0) == '!') {
//			ret = applyPlanrepoirtroryItems(taskFluent, operators, groundSolver);
//		} else {
//			ret = applyPlanrepoirtroryItems(taskFluent, methods, groundSolver);
//		}
		
		ret = new Vector<ConstraintNetwork>();
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	
	// TODO OBSOLETE
//	private Vector<ConstraintNetwork> applyPlanrepoirtroryItems(Fluent fl, Vector<PlanReportroryItem> items, 
//			FluentNetworkSolver groundSolver) {
//		Fluent[] openFluents = groundSolver.getOpenFluents();
//		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
//		for (PlanReportroryItem item : items) {
//			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
//				
//				
////				HashMap<String, Integer> usages = ((PFD0Operator)item).getResourceUsage();
////				for (String resname : usages.keySet()) {
////					HashMap<Variable, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(resname));
////					utilizers.put(fl, usages.get(resname));
////				}
//				
//				logger.fine("Applying preconditions of PlanReportroryItem " + item);
//				if (this.oneShot) {
//					List<ConstraintNetwork> newResolvers = item.expandOneShot(fl,  groundSolver);
//					for (ConstraintNetwork newResolver : newResolvers) {
//						ret.add(newResolver);
//					}
//				} else {
//					ret.add(item.expandPreconditions(fl, groundSolver));
//				}
//				
//			}
//		}
//		return ret;
//	}
	

	/**
	 * Sets the marking of the task to SELECTED
	 */
	@Override
	public void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue) {
//		if(this.oneShot) {
//			metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.PLANNED);
//		} else {
//			metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.SELECTED);
//		}
		// NOTHING TO DO HERE?
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
		return "MoveBaseMetaConstraint";
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
