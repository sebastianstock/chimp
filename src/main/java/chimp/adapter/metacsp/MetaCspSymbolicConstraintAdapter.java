package chimp.adapter.metacsp;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import unify.CompoundSymbolicValueConstraint;

/**
 * Adapts CHIMP symbolic constraint construction to the Meta-CSP
 * {@link CompoundSymbolicValueConstraint} representation. Mirrors
 * {@link MetaCspIntegerConstraintAdapter}: factory methods hide the
 * Meta-CSP constraint type and constructor from callers, returning
 * the framework {@link Constraint} supertype.
 */
public final class MetaCspSymbolicConstraintAdapter {

    public Constraint createPositiveValueRestriction(
            Variable var, int[] indices, String[][] restrictions) {
        CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.POSITIVEVALUERESTRICTION,
                indices, restrictions);
        con.setFrom(var);
        con.setTo(var);
        return con;
    }

    public Constraint createNegativeValueRestriction(
            Variable var, int[] indices, String[][] restrictions) {
        CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.NEGATIVEVALUERESTRICTION,
                indices, restrictions);
        con.setFrom(var);
        con.setTo(var);
        return con;
    }

    public Constraint createSubDifferent(Variable from, Variable to, int[] connections) {
        CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.SUBDIFFERENT, connections);
        con.setFrom(from);
        con.setTo(to);
        return con;
    }

    public Constraint createSubMatches(Variable from, Variable to, int[] connections) {
        CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.SUBMATCHES, connections);
        con.setFrom(from);
        con.setTo(to);
        return con;
    }
}
