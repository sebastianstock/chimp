package pfd0Symbolic;

import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.time.Bounds;


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
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent taskFluent = (Fluent)problematicNetwork.getVariables()[0];
		
		logger.fine("getMetaValues for: " + taskFluent);
		
		
		return new ConstraintNetwork[] {estimateDuration(taskFluent)};
	}
	
	private ConstraintNetwork estimateDuration(Fluent taskFluent) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		FluentConstraint duration = new FluentConstraint(
				FluentConstraint.Type.MOVEDURATION, 
				new Bounds(7, 99));
		duration.setFrom(taskFluent);
		duration.setTo(taskFluent);
		ret.addConstraint(duration);
		
		return ret;
	}
	
	
	@Override
	public void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue) {
		// nothing to do here
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
