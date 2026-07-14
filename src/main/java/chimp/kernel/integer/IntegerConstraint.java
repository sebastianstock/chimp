package chimp.kernel.integer;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;

public class IntegerConstraint extends Constraint {

    public enum Type {ARITHM, ALLDIFFERENT, ALLEQUAL}

    private Type type;
    private String op1;
    private String op2;
    private int cste;

    public IntegerConstraint(Type type, Variable[] scope, String op1, String op2, int cste) {
        this.type = type;
        this.setScope(scope);
        this.op1 = op1;
        this.op2 = op2;
        this.cste = cste;
    }

    public IntegerConstraint(Type type, Variable[] scope) {
        this(type, scope, null, null, 0);
    }

    public IntegerConstraint(Type type, Variable[] scope, String op1, int cste) {
        this(type, scope, op1, null, cste);
    }

    public IntegerConstraint(Type type, Variable[] scope, String op1, String op2) {
        this(type, scope, op1, op2, 0);
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String getEdgeLabel() {
        return toString();
    }

    @Override
    public IntegerConstraint clone() {
        return new IntegerConstraint(this.type, this.getScope(),
                this.op1, this.op2, this.cste);
    }

    @Override
    public boolean isEquivalent(Constraint c) {
        if (!(c instanceof IntegerConstraint ic)) return false;
        if (this.getScope().length != c.getScope().length) return false;
        if (this.type != ic.type) return false;

        for (int i = 0; i < getScope().length; i++) {
            if (this.getScope()[i] != c.getScope()[i]) return false;
        }
        if (this.op1 != ic.op1) return false;
        if (this.op2 != ic.op2) return false;
        if (this.cste != ic.cste) return false;
        return true;
    }

    public Type getType() {
        return type;
    }

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
    }

    public int getCste() {
        return cste;
    }
}
