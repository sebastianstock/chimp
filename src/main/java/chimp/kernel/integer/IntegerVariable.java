package chimp.kernel.integer;

import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;

public class IntegerVariable extends Variable {

    private IntegerDomain domain;

    public IntegerVariable(ConstraintSolver cs, int id) {
        super(cs, id);
        domain = new IntegerDomain();
    }

    public IntegerDomain getDomain() {
        return this.domain;
    }

    public void setDomain(IntegerDomain d) {
        this.domain = d;
    }

    @Override
    public String toString() {
        return this.domain.toString();
    }

    public int compareTo(Variable variable) {
        return this.domain.compareTo(variable);
    }

    public void setValue(int value) {
        this.domain.setValue(value);
    }

    public void setConstantValue(int value) {this.domain.setConstantValue(value);}

    public boolean isInstantiated() {
        return this.domain.isInstantiated();
    }

    public boolean isConstant() {
        return this.domain.isConstant();
    }
}
