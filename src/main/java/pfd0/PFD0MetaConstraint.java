package pfd0;

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

public class PFD0MetaConstraint extends MetaConstraint {

	public enum markings {UNPLANNED, DECOMPOSED,PLANNED, OPEN, CLOSED}; 
	
	public PFD0MetaConstraint() {
		super(null, null);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6968387716912116132L;

	
	/** 
	 * @return All {@link MetaVariable}s with the marking UNPLANNED.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every varible that has the marking UNPLANNED a ConstraintNetwork is built.
		// this becomes a task
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking() != null && var.getMarking().equals(markings.UNPLANNED)) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(var);
				ret.add(nw);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
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
		
		// TODO replace this by applying real htn-methods
		ConstraintNetwork cn = applyMethod(fl, groundSolver);
		if (cn != null) {
			ret.add(cn);
		}
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	/**
	 * Only a dummy method that should be replaced when we have an htn-method class.
	 */
	private ConstraintNetwork applyMethod(Fluent fl, FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);

		String newSymbol =  "!drive (counter1)";

		if (fl.getNameVariable().getName().equals(newSymbol)) 
			return new ConstraintNetwork(null);

		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();

		// create prototypes
		String component = "TestComponent";
		VariablePrototype newFluent0 = new VariablePrototype(groundSolver, component, newSymbol);
		newFluent0.setMarking(markings.UNPLANNED);
		newFluents.add(newFluent0);
		// create constraints
		FluentConstraint dc0 = new FluentConstraint(FluentConstraint.Type.DC);
		dc0.setFrom(fl);
		dc0.setTo(newFluent0);
		newConstraints.add(dc0);

		// TODO add fluents for the preconditions
		// TODO add constraints for the preconditions

		// add constraints to the network
		for (FluentConstraint con : newConstraints)
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
		metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.DECOMPOSED);
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
