package htn.guessOrdering;

import htn.GroundSolverHolder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import chimp.model.FluentId;
import chimp.search.ChimpFlaw;
import chimp.search.ChimpMetaConstraint;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;


public class GuessOrderingMetaConstraint extends ChimpMetaConstraint implements GroundSolverHolder {


	private static final long serialVersionUID = 5778589636096352891L;
	
	private List<List<Integer>> previousConflicts = new ArrayList<List<Integer>>();
	private FluentNetworkSolver groundSolver;

	public GuessOrderingMetaConstraint(ValueOrderingH valOH) {
		super(null, valOH);
	}

	public void setGroundSolver(FluentNetworkSolver groundSolver) {
		this.groundSolver = groundSolver;
	}

	@Override
	public ChimpFlaw[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		ArrayList<Variable> tasks = new ArrayList<Variable>();
		for (Variable var : groundSolver.getVariables("Activity")) {
			tasks.add(var);
		}
		for (Variable var : groundSolver.getVariables("Task")) {
			tasks.add(var);
		}

		List<Integer> varIds = new ArrayList<Integer>();
		List<FluentId> flawTaskIds = new ArrayList<>();
		for (Variable var : tasks) {
			if (!checkApplied((Fluent)var)) {
				if (!checkIsDecomposition(var, groundSolver)) {
					if (checkPredecessors(var, groundSolver)) {  // only add it if there are no predecessors
						varIds.add(var.getID());
						flawTaskIds.add(new FluentId(var.getID()));
					}
				}
			}
		}
		if (varIds.size() > 1 && ! isRepetition(varIds)) {
			previousConflicts.add(varIds);
			return new ChimpFlaw[] { new ChimpFlaw(flawTaskIds) };
		} else {
			return new ChimpFlaw[0];
		}
	}
	
	private boolean isRepetition(List<Integer> varIds) {
		for (List<Integer> l : previousConflicts) {
			if (varIds.size() == l.size()) {
				if (l.containsAll(varIds)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if a is a decomposition of another task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkIsDecomposition(Variable var, FluentNetworkSolver groundSolver) {
		return groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.DC).size() > 0;
	}
	
	/**
	 * Checks if a Variable has a Before with an UNPLANNED task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkPredecessors(Variable var, FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : 
			groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
			if (!checkApplied((Fluent)flc.getScope()[0])) {
				return false;
			}	 
		}
		return true;
	}
	
	private boolean checkApplied(Fluent task) {
		for (Constraint con : this.getGroundSolver().getConstraintNetwork().getConstraints(task, task)) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint)con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariable}s.
	 */
	@Override
	public ConstraintNetwork[] resolvers(ChimpFlaw flaw) {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver) this.getGroundSolver();
		Variable[] vars = new Variable[flaw.taskIds().size()];
		for (int i = 0; i < flaw.taskIds().size(); i++) {
			vars[i] = groundSolver.getVariable((int) flaw.taskIds().get(i).value());
		}
		Fluent taskFluent = (Fluent) vars[0];

		logger.fine("getMetaValues for: " + taskFluent);

		List<ConstraintNetwork> ret = new LinkedList<ConstraintNetwork>();
		if (vars.length > 1) {
			for (int i = 0; i < vars.length; i++) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				for (int j = 0; j < vars.length; j++) {
					if (j != i) {
						FluentConstraint bc = new FluentConstraint(FluentConstraint.Type.BEFORE);
						bc.setFrom(vars[i]);
						bc.setTo(vars[j]);
						nw.addConstraint(bc);
					}
				}
				ret.add(nw);
			}
		}
		ret.add(new ConstraintNetwork(null));
		logger.info("Meta-Values:" );
		for (ConstraintNetwork cn : ret) {
			logger.info(cn.toString());
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	
	@Override
	public ConstraintSolver getGroundSolver() {
		return groundSolver;
	}

	@Override
	public String toString() {
		return "GuessOrderingMetaConstraint";
	}

}
