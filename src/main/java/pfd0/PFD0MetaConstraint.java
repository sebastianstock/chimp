package pfd0;

import java.util.ArrayList;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import simpleSTNPlanner.SimpleSTNOperator;
import simpleSTNPlanner.SimpleSTNDomain.markings;

public class PFD0MetaConstraint extends MetaConstraint {
	
	private Vector<PFD0Operator> operators;
	private Vector<PFD0Method> methods;

	public enum markings {UNPLANNED, PLANNED, OPEN, CLOSED, UNSATISFIED, SATISFIED}; 
	
	public PFD0MetaConstraint() {
		super(null, null);
		operators = new Vector<PFD0Operator>();
		methods = new Vector<PFD0Method>();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6968387716912116132L;
	
	public void addOperator(PFD0Operator o) {
		operators.add(o);
	}
	
	public void addMethod(PFD0Method m) {
		methods.add(m);
	}

	
	/** 
	 * @return All {@link MetaVariable}s with the marking UNPLANNED and which have no unplanned predecessors.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that has the marking UNPLANNED and that has no unplanned predecessors 
		// a ConstraintNetwork is built.
		// this becomes a task.
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking() != null && var.getMarking().equals(markings.UNPLANNED)) {
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
		Constraint[] cons = groundSolver.getConstraintsTo(var);
		if (cons != null) {
			for (Constraint c : cons) {
				if ( (c instanceof FluentConstraint) ) {
					FluentConstraint flc = (FluentConstraint) c;
					if (flc.getType() == FluentConstraint.Type.BEFORE 
							&& flc.getScope()[0].getMarking() == markings.UNPLANNED) {
						return false;
					}	 
				}
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
		Fluent[] openFluents = groundSolver.getOpenFluents();
		
		logger.info("getMetaValues for: " + fl);
		if (fl.getCompoundNameVariable().getHeadName().charAt(0) == '!') {
			for (PFD0Operator o : operators) {
				if (o.checkApplicability(fl) && o.checkPreconditions(openFluents)) {
					logger.info("Applying operator " + o);
					ConstraintNetwork newResolver = o.expand(fl,  groundSolver);
					if (newResolver != null) {
						ret.add(newResolver);
					}
				}
			}
		} else {
			for (PFD0Method m : methods) {
				if (m.checkApplicability(fl) && m.checkPreconditions(openFluents)) {
					logger.info("Applying method " + m);
					ConstraintNetwork newResolver = m.expand(fl,  groundSolver);
					if (newResolver != null) {
						ret.add(newResolver);
					}
				}
			}
		}
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		return getMetaValues(metaVariable);
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
