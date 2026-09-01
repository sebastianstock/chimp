package chimp.kernel.symbolic;

import java.util.Arrays;

import chimp.kernel.Constraint;

/**
 * CHIMP-native symbolic (name) constraint.
 *
 * <p>Port of {@code unify.NameMatchingConstraint}. Supports four types:
 * <ul>
 *   <li>{@link Type#EQUALS} — two variables must have the same symbol</li>
 *   <li>{@link Type#DIFFERENT} — two variables must have different symbols</li>
 *   <li>{@link Type#UNARYEQUALS} — variable must take one of the given values</li>
 *   <li>{@link Type#UNARYDIFFERENT} — variable must NOT take any of the given values</li>
 * </ul>
 */
public class SymbolicConstraint extends Constraint {

    public enum Type { EQUALS, DIFFERENT, UNARYEQUALS, UNARYDIFFERENT }

    private final Type type;
    private int[] unaryValues;

    public SymbolicConstraint(Type type) {
        this.type = type;
    }

    public Type type() { return type; }

    /** Use {@link #type()} in new code. */
    public Type getType() { return type; }

    public void setUnaryValues(int[] unaryValues) {
        this.unaryValues = unaryValues.clone();
        Arrays.sort(this.unaryValues);
    }

    public int[] getUnaryValues() { return unaryValues; }

    // -- Constraint contract --

    @Override
    public SymbolicConstraint clone() {
        SymbolicConstraint c = new SymbolicConstraint(this.type);
        if (this.unaryValues != null) c.setUnaryValues(this.unaryValues);
        return c;
    }

    @Override
    public String getEdgeLabel() {
        if (type == Type.UNARYEQUALS || type == Type.UNARYDIFFERENT) {
            return type + " " + Arrays.toString(unaryValues);
        }
        return type.toString();
    }

    @Override
    public boolean isEquivalent(Constraint other) {
        if (!(other instanceof SymbolicConstraint sc)) return false;
        if (this.scope().length != sc.scope().length) return false;
        for (int i = 0; i < this.scope().length; i++) {
            if (this.scope()[i] != sc.scope()[i]) return false;
        }
        if (this.type != sc.type) return false;
        if (type == Type.UNARYEQUALS || type == Type.UNARYDIFFERENT) {
            for (int v : unaryValues) {
                boolean found = false;
                for (int cv : sc.unaryValues) {
                    if (v == cv) { found = true; break; }
                }
                if (!found) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + from() + getEdgeLabel() + to() + "]";
    }
}
