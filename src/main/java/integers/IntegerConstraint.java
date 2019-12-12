package integers;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

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
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        return sb.toString();
    }

    @Override
    public String getEdgeLabel() {
        return toString();
    }

    @Override
    public Object clone() {
        IntegerConstraint ic = new IntegerConstraint(this.type, this.getScope(),
                this.op1, this.op2, this.cste);
        ic.autoRemovable = this.autoRemovable;
        return ic;
    }

    @Override
    public boolean isEquivalent(Constraint c) {
        if (!(c instanceof IntegerConstraint))  return false;
        if (this.getScope().length != c.getScope().length) return false;
        if (this.type != ((IntegerConstraint) c).type) return false;

        for (int i = 0; i < scope.length; i++) {
            if (this.scope[i] != c.getScope()[i]) return false;
        }
        if (this.op1 != ((IntegerConstraint) c).op1) return false;
        if (this.op2 != ((IntegerConstraint) c).op2) return false;
        if (this.cste != ((IntegerConstraint) c).cste) return false;
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
