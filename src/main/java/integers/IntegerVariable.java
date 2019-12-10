package integers;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class IntegerVariable extends Variable {

    private IntegerDomain domain;

    protected IntegerVariable(ConstraintSolver cs, int id) {
        super(cs, id);
        domain = new IntegerDomain(this);
    }

    @Override
    public Domain getDomain() {
        return this.domain;
    }

    @Override
    public void setDomain(Domain d) {
        this.domain = (IntegerDomain) d;
    }

    @Override
    public String toString() {
        return this.domain.toString();
    }

    @Override
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
