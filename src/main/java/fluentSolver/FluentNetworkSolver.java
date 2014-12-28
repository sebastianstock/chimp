package fluentSolver;

import htn.TaskApplicationMetaConstraint;
import htn.TaskApplicationMetaConstraint.markings;

import java.util.ArrayList;
import java.util.List;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;

import unify.CompoundSymbolicVariableConstraintSolver;

public class FluentNetworkSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5831971530237352714L;

	public FluentNetworkSolver(long origin, long horizon, String[][] symbols, int[] symbolicingredients) {
		super(new Class[] {FluentConstraint.class, AllenIntervalConstraint.class}, Fluent.class, 
				createConstraintSolvers(origin, horizon, symbols, symbolicingredients), new int[] {1, 1});
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, 
			String[][] symbols, int[] symbolicingredients) {
		ConstraintSolver[] ret = new ConstraintSolver[] { 
				new CompoundSymbolicVariableConstraintSolver(symbols, symbolicingredients), 
				new AllenIntervalNetworkSolver(origin, horizon)};
		return ret;
	}

	/**
	 * @return All Fluents with the marking OPEN.
	 */
	public Fluent[] getOpenFluents() {
		ArrayList<Fluent> ret = new ArrayList<Fluent>();
		for (Variable var: getVariables()) {
			if (var.getMarking() == TaskApplicationMetaConstraint.markings.OPEN) {
				ret.add((Fluent) var);
			}
		}
		return ret.toArray(new Fluent[ret.size()]);
	}
	
	/**
	 * @param taskLST The latest start time of the task.
	 * @return All Fluents that are active in a given interval.
	 */
	public Fluent[] getOpenFluents(AllenInterval taskInterval) {
		long taskEST = taskInterval.getEST();
		long taskLST = taskInterval.getLST();
		ArrayList<Fluent> ret = new ArrayList<Fluent>();
		for (Variable var: getVariables()) {
			AllenInterval varInterval = ((Fluent) var).getAllenInterval();
			if (varInterval.getEST() < taskLST ) {//&& varInterval.getLET() >= taskEST) {
				ret.add((Fluent) var);
			}
		}
		return ret.toArray(new Fluent[ret.size()]);
	}

	public List<Constraint> getConstraintsTo(Variable to) {
		ArrayList<Constraint> ret = new ArrayList<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (con.getScope().length == 2) {
				if (con.getScope()[1].equals(to)) ret.add(con);
			}
		}
		return ret;
	}
	
	
	public List<FluentConstraint> getFluentConstraintsOfTypeTo(Variable to, FluentConstraint.Type type) {
		ArrayList<FluentConstraint> ret = new ArrayList<FluentConstraint>(8);
		for (Constraint con : this.getConstraints()) {
			if (con.getScope().length == 2 && con.getScope()[1].equals(to)) {
				if (con instanceof FluentConstraint) {
					if(((FluentConstraint) con).getType() == type) {
						ret.add((FluentConstraint) con);
					}
				}
			}
		}
		return ret;
	}
	
	public List<FluentConstraint> getFluentConstraintsOfTypeFrom(Variable from, FluentConstraint.Type type) {
		ArrayList<FluentConstraint> ret = new ArrayList<FluentConstraint>(8);
		for (Constraint con : this.getConstraints()) {
			if (con.getScope().length == 2 && con.getScope()[0].equals(from)) {
				if (con instanceof FluentConstraint) {
					if(((FluentConstraint) con).getType() == type) {
						ret.add((FluentConstraint) con);
					}
				}
			}
		}
		return ret;
	}


}
