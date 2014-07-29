package pfd0;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0.PFD0MetaConstraint.markings;

public class PreconditionMetaConstraint extends MetaConstraint {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4155403043486687430L;

	public PreconditionMetaConstraint() {
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
		// for every variable that has the marking UNJUSTIFIEDD and that has no unplanned predecessors 
		// a ConstraintNetwork is built.
		// this becomes a task.
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking() != null && var.getMarking().equals(markings.UNJUSTIFIED)) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(var);
				ret.add(nw);
			}
		}
		System.out.println("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	
	}
	

	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * 
	 * Search for open Variables that match the dummy precondition.
	 * Put a matches constraint between them and set the marking to SATISFIED.
	 * 
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariables}.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		// possible constraint networks
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent fl = (Fluent)problematicNetwork.getVariables()[0];
		logger.info("getMetaValues for: " + fl);
		
		Fluent[] openFluents = ((FluentNetworkSolver)this.getGroundSolver()).getOpenFluents();
		String headName = fl.getCompoundNameVariable().getHeadName();
		for (Fluent openFluent : openFluents)  {
			if(headName.equals(openFluent.getCompoundNameVariable().getHeadName())) {
				// potential match. Add matches constraint.
				// TODO could be optimized by testing the full name.
				ConstraintNetwork newResolver = addMatches(fl, openFluent);
				if (newResolver != null) {
					ret.add(newResolver);
				}
			}
		}
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	/** Adds the MATCHES constraint between the two fluents.
	 * 
	 * @param preFluent
	 * @param matchingFluent
	 * @return
	 */
	private ConstraintNetwork addMatches(Fluent preFluent, Fluent matchingFluent) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		FluentConstraint con = new FluentConstraint(FluentConstraint.Type.MATCHES);
		con.setFrom(preFluent);
		con.setTo(matchingFluent);
		ret.addConstraint(con);
		return ret;
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
		metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.JUSTIFIED);
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
