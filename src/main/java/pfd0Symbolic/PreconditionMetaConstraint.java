package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import symbolicUnifyTyped.CompoundSymbolicVariableConstraintSolver;

public class PreconditionMetaConstraint extends MetaConstraint {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3724438654585362157L;

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
		
	// TODO this may take too long, added this only for debugging!!!
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
		
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
		Fluent preFluent = (Fluent)problematicNetwork.getVariables()[0];
		logger.info("getMetaValues for: " + preFluent);
		
		// creates MATCHES Fluents between dummy precondition and open state fluents which have the same predicate name
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagatePredicateNames();
		Fluent[] openFluents = groundSolver.getOpenFluents();
		String[] headNames = preFluent.getCompoundSymbolicVariable().getPossiblePredicateNames();
		for (String headName : headNames) {
			for (Fluent openFluent : openFluents)  {
				String[] onames = openFluent.getCompoundSymbolicVariable().getPossiblePredicateNames();
				for (String name : onames) {
					if(headName.equals(name)) {
						// potential match. Add matches constraint.
						// TODO could be optimized by testing the full name.
						ConstraintNetwork newResolver = addMatches(preFluent, openFluent);
						if (newResolver != null) {
							ret.add(newResolver);
						}
						continue;
					}
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
		con.setFrom(matchingFluent);
		con.setTo(preFluent);
		ret.addConstraint(con);
		return ret;
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		return getMetaValues(metaVariable);
	}

	/**
	 * Sets the marking of the variable to justified.
	 */
	@Override
	public void markResolvedSub(MetaVariable metaVariable,
			ConstraintNetwork metaValue) {
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
