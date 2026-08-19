package chimp.kernel.fluent;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.kernel.integer.IntegerVariable;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.temporal.TemporalVariable;

/**
 * CHIMP-native fluent — a composite variable holding a symbolic component,
 * a temporal interval, and optionally integer variables.
 *
 * <p>Replaces {@code fluentSolver.Fluent} (which extended
 * {@code MultiVariable} from Meta-CSP).
 */
public class Fluent extends Variable {

    public static final String ACTIVITY_TYPE_STR = "Activity";
    public static final String STATE_TYPE_STR = "State";
    public static final String PLANNED_STATE_TYPE_STR = "PlannedState";
    public static final String TASK_TYPE_STR = "Task";

    private final CompoundSymbolicVariable symbolicVar;
    private final TemporalVariable temporalVar;
    private final IntegerVariable[] integerVars;
    private java.awt.Color color;

    public Fluent(FluentNetworkSolver solver, int id,
                  CompoundSymbolicVariable symbolicVar,
                  TemporalVariable temporalVar,
                  IntegerVariable[] integerVars) {
        super(solver, id);
        this.symbolicVar = symbolicVar;
        this.temporalVar = temporalVar;
        this.integerVars = integerVars;
    }

    /**
     * @return The {@link CompoundSymbolicVariable} representing the compound
     *         symbolic value of this fluent.
     */
    public CompoundSymbolicVariable getCompoundSymbolicVariable() {
        return symbolicVar;
    }

    /**
     * @return The {@link TemporalVariable} representing the temporal extent
     *         of this fluent.  (Legacy alias: {@code getAllenInterval()})
     */
    public TemporalVariable getTemporalVariable() {
        return temporalVar;
    }

    /** Legacy alias. */
    public TemporalVariable getAllenInterval() {
        return temporalVar;
    }

    public IntegerVariable[] getIntegerVariables() {
        return integerVars;
    }

    /** Sets the fully qualified name on the symbolic variable. */
    public void setName(String name) {
        symbolicVar.setFullName(name);
    }

    /** Sets the name (type + arguments) on the symbolic variable. */
    public void setName(String type, String[] arguments) {
        symbolicVar.setName(type, arguments);
    }

    /**
     * @return The short name from the symbolic variable (e.g. "on(mug1 pl1)").
     */
    public String getName() {
        return symbolicVar.getShortName();
    }

    /** What kind of fluent this is (Activity, State, etc.). */
    public String getTypeStr() {
        String c = component();
        return c != null ? c : STATE_TYPE_STR;
    }

    /** @return true if this fluent represents an activity. */
    public boolean isActivity() {
        return ACTIVITY_TYPE_STR.equals(getTypeStr());
    }

    /** Sets the visualization color for this fluent. */
    public void setColor(java.awt.Color color) {
        this.color = color;
    }

    /** @return the visualization color, or {@code null} if not set. */
    public java.awt.Color getColor() {
        return color;
    }

    // -- Meta-CSP compatibility methods --

    /**
     * Compatibility method — returns {@code this}.
     * In Meta-CSP, {@code Activity.getVariable()} returns the underlying
     * {@code Variable}.  CHIMP-native Fluent IS a Variable, so this is
     * an identity operation.
     */
    public Variable getVariable() {
        return this;
    }

    /**
     * Compatibility method — returns the predicate name as a single-element
     * string array.
     */
    public String[] getSymbols() {
        return new String[] { symbolicVar.getPredicateName() };
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("(");
        ret.append(id());
        ret.append(")::");
        ret.append(symbolicVar.getShortName());
        if (integerVars.length > 0) {
            ret.append("(");
            ret.append(integerVars[0]);
            for (int i = 1; i < integerVars.length; i++) {
                ret.append(", ").append(integerVars[i]);
            }
            ret.append(")");
        }
        ret.append("[").append(temporalVar.getEST()).append(",")
            .append(temporalVar.getLST()).append(";")
            .append(temporalVar.getEET()).append(",")
            .append(temporalVar.getLET()).append("]");
        if (marking() == 2 /* UNIFIED */) {
            ret.append("/UNIFIED");
        }
        return ret.toString();
    }
}
