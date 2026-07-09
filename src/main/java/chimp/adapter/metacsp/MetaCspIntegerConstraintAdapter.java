package chimp.adapter.metacsp;

import chimp.model.IntegerConstraintKind;
import htn.IntegerConstraintTemplate;
import integers.IntegerConstraint;
import org.metacsp.framework.Variable;

public final class MetaCspIntegerConstraintAdapter {

    public IntegerConstraint toIntegerConstraint(
            IntegerConstraintTemplate template,
            Variable[] scope) {
        return new IntegerConstraint(
                toMetaCspType(template.kind()),
                scope,
                template.operator1(),
                template.operator2(),
                template.constant());
    }

    public IntegerConstraint createAllEqualConstraint(Variable[] scope) {
        return new IntegerConstraint(IntegerConstraint.Type.ALLEQUAL, scope);
    }

    public IntegerConstraint createConstantConstraint(Variable variable, int constant) {
        return new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new Variable[]{variable},
                "=",
                constant);
    }

    private IntegerConstraint.Type toMetaCspType(IntegerConstraintKind kind) {
        return switch (kind) {
            case ARITHMETIC -> IntegerConstraint.Type.ARITHM;
            case ALL_DIFFERENT -> IntegerConstraint.Type.ALLDIFFERENT;
            case ALL_EQUAL -> IntegerConstraint.Type.ALLEQUAL;
        };
    }
}
