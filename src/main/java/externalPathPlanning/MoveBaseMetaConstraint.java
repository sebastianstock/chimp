package externalPathPlanning;

import java.util.ArrayList;
import java.util.List;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.model.FluentId;
import chimp.model.TemporalBounds;
import chimp.search.ChimpFlaw;
import chimp.search.ChimpMetaConstraint;
import chimp.search.ChimpResolver;
import htn.GroundSolverHolder;



public class MoveBaseMetaConstraint extends ChimpMetaConstraint implements GroundSolverHolder {

	private static final long serialVersionUID = 2789371087819357936L;
	private static final String MOVE_BASE_NAME = "!move_base";
	private static final String ROBOTAT_NAME = "RobotAt";
	private static final int FROM_AREA_INDEX = 1;
	private static final int TO_AREA_INDEX = 1;
	
	private MoveBaseDurationEstimator durationEstimator;
	private FluentNetworkSolver groundSolver;


	public MoveBaseMetaConstraint(MoveBaseDurationEstimator durationEstimator) {
		super(null, null);
		this.durationEstimator = durationEstimator;
	}

	public void setGroundSolver(FluentNetworkSolver groundSolver) {
		this.groundSolver = groundSolver;
	}

	
	@Override
	public ChimpFlaw[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		List<ChimpFlaw> ret = new ArrayList<>();
		for (Variable var : groundSolver.getVariables()) {
			if (((Fluent) var).getCompoundSymbolicVariable().getPredicateName().equals(MOVE_BASE_NAME)) {
				if (checkApplied((Fluent) var) && ! checkDuration(var, groundSolver)) {
					ret.add(new ChimpFlaw(List.of(new FluentId(var.getID()))));
				}
			}
		}
		logger.finest("MetaVariables: " + ret);
		return ret.toArray(new ChimpFlaw[ret.size()]);

	}
	
	private boolean checkApplied(Fluent task) {
		for (Constraint con : this.getGroundSolver().getConstraintNetwork().getConstraints(task, task)) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint)con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) return true;
			}
		}
		return false;
	}
	

	// Checks if a duration has been set to a variable
	private boolean checkDuration(Variable var, FluentNetworkSolver groundSolver) {
		List<FluentConstraint> durationCons = 
				groundSolver.getFluentConstraintsOfTypeFrom(var, FluentConstraint.Type.MOVEDURATION);
		return !durationCons.isEmpty();
	}

	
	@Override
	public ChimpResolver[] resolvers(ChimpFlaw flaw) {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver) this.getGroundSolver();
		Fluent taskFluent = (Fluent) groundSolver.getVariable((int) flaw.taskIds().get(0).value());

		logger.fine("getMetaValues for: " + taskFluent);

		ConstraintNetwork[] nets = estimateDuration(taskFluent);
		ChimpResolver[] result = new ChimpResolver[nets.length];
		for (int i = 0; i < nets.length; i++) {
			result[i] = new ChimpResolver(nets[i]);
		}
		return result;
	}
	
	private ConstraintNetwork[] estimateDuration(Fluent taskFluent) {
		TemporalBounds bounds = null;
		String fromArea = getFromArea(taskFluent);
		if (fromArea != null && fromArea.length() > 0) {
			String toArea = getToArea(taskFluent);
			if (toArea != null && toArea.length() > 0) {
				bounds = durationEstimator.estimateDuration(fromArea, toArea);
			} else {
				logger.severe("No unique toArea found!");
			}
		} else {
			logger.severe("No fromArea found!");
		}
		
		if (bounds != null) {
			FluentConstraint duration = new FluentConstraint(FluentConstraint.Type.MOVEDURATION, bounds);
			duration.setFrom(taskFluent);
			duration.setTo(taskFluent);
			ConstraintNetwork ret = new ConstraintNetwork();
			ret.addConstraint(duration);
			return new ConstraintNetwork[] {ret};
		} else {
			return new ConstraintNetwork[] {};
		}
	}
	
	private String getToArea(Fluent taskFluent) {
		String[] possibleToAreas = taskFluent.getCompoundSymbolicVariable().getSymbolsAt(TO_AREA_INDEX);
		if (possibleToAreas.length != 1) {
			throw new IllegalStateException("FromArea is not ground");
		} else {
			return possibleToAreas[0];
		}
	}
	
	private String getFromArea(Fluent taskFluent) {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		List<FluentConstraint> preCons = 
				groundSolver.getFluentConstraintsOfTypeTo(taskFluent, FluentConstraint.Type.PRE);
		for (FluentConstraint con : preCons) {
			CompoundSymbolicVariable from = ((Fluent) con.getFrom()).getCompoundSymbolicVariable();
			if (from.getPredicateName().equals(ROBOTAT_NAME)) {
				String[] possibleFromAreas = from.getSymbolsAt(FROM_AREA_INDEX);
				
				if (possibleFromAreas.length != 1) {
					throw new IllegalStateException("FromArea is not ground");
				} else {
					return possibleFromAreas[0];
				}
			}
		}
		throw new IllegalStateException("No RobotAt precondition found");
	}
	
	
	@Override
	public ConstraintSolver getGroundSolver() {
		return groundSolver;
	}

	@Override
	public String toString() {
		return "MoveBaseMetaConstraint";
	}

}
