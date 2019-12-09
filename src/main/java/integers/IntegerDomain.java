package integers;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import java.util.logging.Logger;

public class IntegerDomain extends Domain {

    int value;
    boolean isInstantiated;
    boolean isConstant;

    protected transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

    protected IntegerDomain(Variable v) {
        super(v);
        isInstantiated = false;
        isConstant = false;
    }

    @Override
    public String toString() {
        if (isInstantiated) {
            return Integer.toString(value);
        } else {
            return "[...]";
        }
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof IntegerDomain)) return 0;
        boolean otherIsInstantiated = ((IntegerDomain) o).isInstantiated;
        if (!this.isInstantiated) {
            if (otherIsInstantiated) {
                return -1;
            } else {
                return 0;
            }
        } else {
            if (otherIsInstantiated) {
                return value - ((IntegerDomain) o).value;
            } else {
                return 1;
            }
        }
    }

    public void setValue(int value) {
        if (!isConstant) {
            this.value = value;
            this.isInstantiated = true;
        } else {
            logger.warning("Tried to change value of constant IntegerVariable");
        }
    }

    public int getValue() {
        return this.value;
    }

    public boolean isInstantiated() {
        return isInstantiated;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void uninstantiate() {
        this.isInstantiated = false;
    }

    public void setConstantValue(int value) {
        setValue(value);
        this.isConstant = true;
    }

}
