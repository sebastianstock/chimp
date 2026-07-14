package chimp.kernel.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.logging.ChimpLogging;

/**
 * CHIMP-native compound symbolic constraint.
 *
 * <p>Port of {@code unify.CompoundSymbolicValueConstraint}. A constraint
 * between two {@link CompoundSymbolicVariable}s that generates
 * sub-constraints ({@link SymbolicConstraint}s) on their internal
 * {@link SymbolicVariable}s.
 *
 * <p>Types:
 * <ul>
 *   <li>{@link Type#MATCHES} — all internal variables must be equal</li>
 *   <li>{@link Type#SUBMATCHES} — selected argument pairs must be equal</li>
 *   <li>{@link Type#SUBDIFFERENT} — selected argument pairs must differ</li>
 *   <li>{@link Type#POSITIVEVALUERESTRICTION} — variable must have one of given values</li>
 *   <li>{@link Type#NEGATIVEVALUERESTRICTION} — variable must NOT have any of given values</li>
 * </ul>
 */
public class CompoundSymbolicConstraint extends Constraint {

    public enum Type { MATCHES, SUBMATCHES, SUBDIFFERENT,
                       POSITIVEVALUERESTRICTION, NEGATIVEVALUERESTRICTION }

    private static final Logger logger =
            ChimpLogging.getLogger(CompoundSymbolicConstraint.class);

    private final Type type;
    private final int[] connections;
    private final int[] restrictionIndices;
    private final String[][] restrictions;

    // Generated sub-constraints with their target solver indices
    private List<RoutedConstraint> routedConstraints;

    public CompoundSymbolicConstraint(Type type) {
        this.type = type;
        this.connections = null;
        this.restrictionIndices = null;
        this.restrictions = null;
    }

    public CompoundSymbolicConstraint(Type type, int[] connections) {
        this.type = type;
        this.connections = connections;
        this.restrictionIndices = null;
        this.restrictions = null;
    }

    public CompoundSymbolicConstraint(Type type, int[] indices, String[][] restrictions) {
        this.type = type;
        this.connections = null;
        this.restrictionIndices = indices;
        this.restrictions = restrictions;
    }

    public Type type() { return type; }

    /** Use {@link #type()} in new code. */
    public Type getType() { return type; }

    /**
     * Generates sub-constraints between the internal variables of
     * {@code f} and {@code t} (both must be {@link CompoundSymbolicVariable}s).
     *
     * <p>Returns a list of {@link RoutedConstraint}s, each pairing a
     * {@link SymbolicConstraint} with the index of the sub-solver that
     * should receive it.  The {@link CompoundSymbolicSolver} uses this
     * to route constraints to the correct {@link SymbolicSolver}.
     */
    public List<RoutedConstraint> createInternalConstraints(Variable f, Variable t) {
        if (!(f instanceof CompoundSymbolicVariable cf)
                || !(t instanceof CompoundSymbolicVariable ct)) {
            return List.of();
        }

        SymbolicVariable[] finternals = cf.getInternalVariables();
        SymbolicVariable[] tinternals = ct.getInternalVariables();
        CompoundSymbolicSolver compSolver = (CompoundSymbolicSolver) cf.solver();
        int[] var2solver = compSolver.getVarIndexToSolverIndex();

        List<RoutedConstraint> result = new ArrayList<>();

        switch (type) {
            case MATCHES -> {
                for (int i = 0; i < finternals.length; i++) {
                    SymbolicConstraint con = new SymbolicConstraint(
                            SymbolicConstraint.Type.EQUALS);
                    con.setScope(new Variable[] { finternals[i], tinternals[i] });
                    result.add(new RoutedConstraint(con, var2solver[i]));
                }
            }
            case SUBMATCHES -> {
                logger.fine("CREATING SUBMATCHES INTERNAL CONSTRAINTS");
                if (connections != null && (connections.length % 2) == 0) {
                    for (int i = 0; i < connections.length; i += 2) {
                        int fi = connections[i] + 1;
                        int ti = connections[i + 1] + 1;
                        if (fi < finternals.length && ti < tinternals.length) {
                            SymbolicConstraint con = new SymbolicConstraint(
                                    SymbolicConstraint.Type.EQUALS);
                            con.setScope(new Variable[] { finternals[fi], tinternals[ti] });
                            result.add(new RoutedConstraint(con, var2solver[fi]));
                        }
                    }
                }
            }
            case SUBDIFFERENT -> {
                logger.fine("CREATING SUBDIFFERENT INTERNAL CONSTRAINTS");
                if (connections != null && (connections.length % 2) == 0) {
                    for (int i = 0; i < connections.length; i += 2) {
                        int fi = connections[i] + 1;
                        int ti = connections[i + 1] + 1;
                        if (fi < finternals.length && ti < tinternals.length) {
                            SymbolicConstraint con = new SymbolicConstraint(
                                    SymbolicConstraint.Type.DIFFERENT);
                            con.setScope(new Variable[] { finternals[fi], tinternals[ti] });
                            result.add(new RoutedConstraint(con, var2solver[fi]));
                        }
                    }
                }
            }
            case POSITIVEVALUERESTRICTION -> {
                logger.fine("CREATING POSITIVEVALUERESTRICTION");
                if (restrictionIndices != null && restrictions != null
                        && restrictionIndices.length == restrictions.length) {
                    for (int i = 0; i < restrictionIndices.length; i++) {
                        int idx = restrictionIndices[i] + 1;
                        SymbolicConstraint con = new SymbolicConstraint(
                                SymbolicConstraint.Type.UNARYEQUALS);
                        con.setScope(new Variable[] { finternals[idx], finternals[idx] });
                        int[] values = compSolver.subSolver(var2solver[idx])
                                .indexArrayOfSymbols(restrictions[i]);
                        con.setUnaryValues(values);
                        result.add(new RoutedConstraint(con, var2solver[idx]));
                    }
                }
            }
            case NEGATIVEVALUERESTRICTION -> {
                logger.fine("CREATING NEGATIVEVALUERESTRICTION");
                if (restrictionIndices != null && restrictions != null
                        && restrictionIndices.length == restrictions.length) {
                    for (int i = 0; i < restrictionIndices.length; i++) {
                        int idx = restrictionIndices[i] + 1;
                        SymbolicConstraint con = new SymbolicConstraint(
                                SymbolicConstraint.Type.UNARYDIFFERENT);
                        con.setScope(new Variable[] { finternals[idx], finternals[idx] });
                        int[] values = compSolver.subSolver(var2solver[idx])
                                .indexArrayOfSymbols(restrictions[i]);
                        con.setUnaryValues(values);
                        result.add(new RoutedConstraint(con, var2solver[idx]));
                    }
                }
            }
        }
        this.routedConstraints = result;
        return result;
    }

    /** Returns the sub-constraints generated by the last call to
     *  {@link #createInternalConstraints}. */
    List<RoutedConstraint> getRoutedConstraints() {
        return routedConstraints;
    }

    // -- Constraint contract --

    @Override
    public CompoundSymbolicConstraint clone() {
        if (connections != null) {
            return new CompoundSymbolicConstraint(type, connections);
        }
        return new CompoundSymbolicConstraint(type,
                restrictionIndices != null ? restrictionIndices.clone() : null,
                restrictions != null ? restrictions.clone() : null);
    }

    @Override
    public String getEdgeLabel() {
        return type.toString();
    }

    @Override
    public boolean isEquivalent(Constraint other) { return false; }

    // -- Routing helper --

    /** A sub-constraint paired with its target sub-solver index. */
    public record RoutedConstraint(SymbolicConstraint constraint, int solverIndex) {}
}
