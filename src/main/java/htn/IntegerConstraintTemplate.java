package htn;

import java.util.Arrays;
import java.util.Objects;

import chimp.model.IntegerConstraintKind;

public final class IntegerConstraintTemplate {

    private final IntegerConstraintKind kind;
    private final String[] variableKeys;
    private final String operator1;
    private final String operator2;
    private final int constant;

    public IntegerConstraintTemplate(
            IntegerConstraintKind kind,
            String[] variableKeys,
            String operator1,
            String operator2,
            int constant) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.variableKeys = Arrays.copyOf(
                Objects.requireNonNull(variableKeys, "variableKeys"), variableKeys.length);
        for (String variableKey : this.variableKeys) {
            Objects.requireNonNull(variableKey, "variableKey");
        }
        this.operator1 = operator1;
        this.operator2 = operator2;
        this.constant = constant;
    }

    public IntegerConstraintKind kind() {
        return kind;
    }

    public String[] variableKeys() {
        return Arrays.copyOf(variableKeys, variableKeys.length);
    }

    public String operator1() {
        return operator1;
    }

    public String operator2() {
        return operator2;
    }

    public int constant() {
        return constant;
    }
}
