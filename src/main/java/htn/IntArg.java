package htn;

public class IntArg {
    public final String varName;
    public final int constValue;

    public IntArg(String varName) {
        this.varName = varName;
        constValue = -1;
    }

    public IntArg(int constValue) {
        this.constValue = constValue;
        this.varName = null;
    }

    public boolean isVariable() {
        return varName != null;
    }
}
