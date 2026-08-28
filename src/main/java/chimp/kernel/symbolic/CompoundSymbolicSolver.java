package chimp.kernel.symbolic;

import java.util.*;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;

/**
 * CHIMP-native compound symbolic constraint solver.
 *
 * <p>Port of {@code unify.CompoundSymbolicVariableConstraintSolver}.
 * Composes multiple {@link SymbolicSolver}s — one per argument position —
 * and manages {@link CompoundSymbolicVariable}s whose internal
 * {@link SymbolicVariable}s are distributed across the sub-solvers.
 *
 * <p>When a {@link CompoundSymbolicConstraint} is added, it generates
 * sub-constraints via {@link CompoundSymbolicConstraint#createInternalConstraints}
 * and routes each to the appropriate sub-solver.
 */
public class CompoundSymbolicSolver extends ConstraintSolver {

    private final SymbolicSolver[] subSolvers;
    private final int[] ingredients;
    private final int[] varIndexToSolverIndex;
    private int totalInternalVars;

    /**
     * @param symbols     one {@code String[]} per argument position (position 0 = predicate name)
     * @param ingredients how many {@link SymbolicVariable}s each sub-solver contributes
     */
    public CompoundSymbolicSolver(String[][] symbols, int[] ingredients) {
        this.ingredients = ingredients.clone();
        this.subSolvers = new SymbolicSolver[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            subSolvers[i] = new SymbolicSolver(symbols[i]);
        }
        this.varIndexToSolverIndex = buildVarIndexToSolverIndex();
    }

    private int[] buildVarIndexToSolverIndex() {
        int total = 0;
        for (int c : ingredients) total += c;
        this.totalInternalVars = total;
        int[] map = new int[total];
        int idx = 0;
        for (int solverIdx = 0; solverIdx < ingredients.length; solverIdx++) {
            for (int j = 0; j < ingredients[solverIdx]; j++) {
                map[idx++] = solverIdx;
            }
        }
        return map;
    }

    // -- sub-solver access --

    public SymbolicSolver[] getSubSolvers() { return subSolvers; }

    public SymbolicSolver subSolver(int index) { return subSolvers[index]; }

    public int[] getIngredients() { return ingredients.clone(); }

    public int[] getVarIndexToSolverIndex() { return varIndexToSolverIndex; }

    // -- variable creation --

    /** Creates {@code num} compound symbolic variables. */
    public CompoundSymbolicVariable[] createVariables(int num) {
        // First, create all internal variables in the sub-solvers
        SymbolicVariable[][] internalVars = new SymbolicVariable[num][totalInternalVars];
        int offset = 0;
        for (int si = 0; si < subSolvers.length; si++) {
            int count = ingredients[si] * num;
            SymbolicVariable[] batch = subSolvers[si].createVariables(count);
            for (int vi = 0; vi < num; vi++) {
                for (int ii = 0; ii < ingredients[si]; ii++) {
                    internalVars[vi][offset + ii] = batch[vi * ingredients[si] + ii];
                }
            }
            offset += ingredients[si];
        }

        // Assemble into compound variables
        CompoundSymbolicVariable[] ret = new CompoundSymbolicVariable[num];
        for (int i = 0; i < num; i++) {
            ret[i] = new CompoundSymbolicVariable(this, nextVariableId(), internalVars[i]);
            registerVariable(ret[i]);
        }
        return ret;
    }

    // -- constraint management --

    @Override
    public boolean addConstraints(Constraint[] cs) {
        List<Constraint> added = new ArrayList<>();
        for (Constraint c : cs) {
            if (!addConstraint(c)) {
                for (Constraint a : added) {
                    removeConstraint(a);
                }
                return false;
            }
            added.add(c);
        }
        return true;
    }

    @Override
    public boolean addConstraint(Constraint c) {
        if (c instanceof CompoundSymbolicConstraint cc) {
            // Generate sub-constraints and route to sub-solvers
            List<CompoundSymbolicConstraint.RoutedConstraint> routed =
                    cc.createInternalConstraints(cc.from(), cc.to());
            if (routed != null) {
                List<SymbolicConstraint> added = new ArrayList<>();
                for (CompoundSymbolicConstraint.RoutedConstraint rc : routed) {
                    SymbolicSolver targetSolver = subSolvers[rc.solverIndex()];
                    // Don't auto-propagate yet — we'll propagate all at once
                    targetSolver.constraintList().add(rc.constraint());
                    added.add(rc.constraint());
                }
                // Now propagate all sub-solvers and roll back on failure
                try {
                    for (SymbolicSolver ss : subSolvers) {
                        if (!ss.propagate()) {
                            // Roll back: remove what we just added
                            for (SymbolicConstraint sc : added) {
                                for (SymbolicSolver sss : subSolvers) {
                                    sss.constraintList().remove(sc);
                                }
                            }
                            return false;
                        }
                    }
                } catch (Exception e) {
                    // Roll back on exception too
                    for (SymbolicConstraint sc : added) {
                        for (SymbolicSolver sss : subSolvers) {
                            sss.constraintList().remove(sc);
                        }
                    }
                    return false;
                }
            }
        }
        // Add to our own constraint list and propagate
        constraintList().add(c);
        // Already propagated above; skip base class propagation
        return true;
    }

    @Override
    public void removeConstraint(Constraint c) {
        if (c instanceof CompoundSymbolicConstraint cc) {
            // Also remove the generated sub-constraints from sub-solvers
            List<CompoundSymbolicConstraint.RoutedConstraint> routed =
                    cc.getRoutedConstraints();
            if (routed != null) {
                for (CompoundSymbolicConstraint.RoutedConstraint rc : routed) {
                    subSolvers[rc.solverIndex()].constraintList()
                            .remove(rc.constraint());
                }
            }
        }
        constraintList().remove(c);
    }

    @Override
    public void removeConstraints(Constraint[] cs) {
        for (Constraint c : cs) {
            removeConstraint(c);
        }
    }

    @Override
    public boolean propagate() {
        for (SymbolicSolver ss : subSolvers) {
            if (!ss.propagate()) return false;
        }
        return true;
    }

    /** Full propagation including all internal reductions. */
    public void propagateAllSub() {
        for (SymbolicSolver ss : subSolvers) {
            ss.propagate();
        }
    }

    /** Full propagation (same as propagateAllSub — for Meta-CSP compatibility). */
    public void propagateAllSubFull() {
        propagateAllSub();
    }
}
