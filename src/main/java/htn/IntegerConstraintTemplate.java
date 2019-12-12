package htn;

import integers.IntegerConstraint;

public class IntegerConstraintTemplate {

    public final IntegerConstraint.Type type;
    public final String op1;
    public final String op2;
    public final int cste;
    public final String[] varKeys;

    public IntegerConstraintTemplate(IntegerConstraint.Type type, String[] varKeys, String op1, String op2, int cste) {
        this.type = type;
        this.varKeys = varKeys;
        this.op1 = op1;
        this.op2 = op2;
        this.cste = cste;
    }


}
