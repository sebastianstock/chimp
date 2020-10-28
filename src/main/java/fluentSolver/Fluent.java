package fluentSolver;

import htn.HTNMetaConstraint;
import integers.IntegerVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.allenInterval.AllenInterval;

import unify.CompoundSymbolicVariable;

public class Fluent extends MultiVariable implements Activity{

	public static String ACTIVITY_TYPE_STR = "Activity";
	public static String STATE_TYPE_STR = "State";
	public static String PLANNED_STATE_TYPE_STR = "PlannedState";
	public static String TASK_TYPE_STR = "Task";

	/**
	 * 
	 */
	private static final long serialVersionUID = -3155335762297801220L;
	
	public Fluent(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] 
			internalVars) {
		super(cs, id, internalSolvers, internalVars);
	}
	
	
	/**
	 * @return The {@link CompoundSymbolicVariable} representing the compound symbolic value 
	 * of this {@link Fluent}.
	 */
	public CompoundSymbolicVariable getCompoundSymbolicVariable() {
		return (CompoundSymbolicVariable)this.getInternalVariables()[0];
	}

	/**
	 * @return The {@link AllenInterval} representing the temporal extent of this {@link Fluent}.
	 */
	public AllenInterval getAllenInterval() {
		return (AllenInterval)this.getInternalVariables()[1];
	}

	public IntegerVariable[] getIntegerVariables() {
		int integerVariableStartIndex = ((FluentNetworkSolver) solver).getIntegerVariableStartIndex();
		int integerVariablesCnt = ((FluentNetworkSolver) solver).getIntegerVariablesCnt();
		IntegerVariable[] ret = new IntegerVariable[integerVariablesCnt];
		for (int i = 0; i < integerVariablesCnt; i++) {
			ret[i] = (IntegerVariable) getInternalVariables()[i + integerVariableStartIndex];
		}
		return ret;
	}
	
	public void setName(String name) {
		this.getCompoundSymbolicVariable().setFullName(name);
	}
	
	public void setName(String type, String[] arguments) {
		this.getCompoundSymbolicVariable().setName(type, arguments);
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
//		ret.append(this.getComponent());
		ret.append("(");
		ret.append(this.getID());
		ret.append(")::");
//		ret.append(this.getInternalVariables()[0].toString());
		ret.append(((CompoundSymbolicVariable) this.getInternalVariables()[0]).getShortName());

		IntegerVariable[] integerVariables = getIntegerVariables();
		if (integerVariables.length > 0) {
			ret.append("(");
			ret.append(integerVariables[0].toString());
			for (int i = 1; i < integerVariables.length; i++) {
				ret.append(", ").append(integerVariables[i].toString());
			}
			ret.append(")");
		}

//		ret.append(">U<");
		ret.append(this.getAllenInterval().getDomain());
//		ret.append(this.getInternalVariables()[1].toString());
//		ret.append(">");
		if (this.getMarking() == HTNMetaConstraint.markings.UNIFIED) {
			ret.append("/");
			ret.append(this.getMarking());
		}
//		ret.append("/");
//		ret.append(this.getMarking());
		return ret.toString();
//		return "";
	}
	
	public String getName() {
		return ((CompoundSymbolicVariable) this.getInternalVariables()[0]).getShortName();
	}

	@Override
	public AllenInterval getTemporalVariable() {
		return getAllenInterval();
	}


	@Override
	public Variable getVariable() {
		return this;
	}


	@Override
	public String[] getSymbols() {
		return new String[] {this.getInternalVariables()[0].toString()};
	}

	public String getTypeStr() {
		String component = this.getComponent();
		if (component != null) {
			return component;
		} else {
			return STATE_TYPE_STR;
		}
	}

	/**
	 * @return true if this Fluent represents an activity
	 */
	public boolean isActivity() {
		return getTypeStr().equals(ACTIVITY_TYPE_STR);
	}

}
