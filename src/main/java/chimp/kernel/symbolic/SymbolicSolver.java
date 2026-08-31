package chimp.kernel.symbolic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.logging.ChimpLogging;

/**
 * CHIMP-native symbolic constraint solver.
 *
 * <p>Port of {@code unify.NameMatchingConstraintSolver}. Each solver
 * is parameterized by a fixed set of {@link #symbols symbol strings}.
 * It manages {@link SymbolicVariable}s whose domains are subsets of
 * those symbols and {@link SymbolicConstraint}s (EQUALS, DIFFERENT,
 * UNARYEQUALS, UNARYDIFFERENT).
 *
 * <p>Propagation uses the AC-3 arc-consistency algorithm.
 *
 * <p>The constraint adjacency map ({@link #varToConstraints}) is
 * maintained incrementally by every constraint mutation path — the
 * constraint list must therefore only be modified through
 * {@link #addConstraint}, {@link #removeConstraint},
 * {@link #addSubConstraint} or {@link #removeSubConstraint}.
 */
public class SymbolicSolver extends ConstraintSolver {

    private static final Logger logger = ChimpLogging.getLogger(SymbolicSolver.class);

    private final String[] symbols;
    private final Map<String, Integer> symbolsToIndex = new HashMap<>();
    private final Map<Variable, List<SymbolicConstraint>> varToConstraints = new HashMap<>();

    /** Reusable AC-3 domain backups (avoid per-propagate allocation). */
    private SymbolicDomain[] backupScratch;

    private long propagationTime;
    private int invocationCount;

    public SymbolicSolver(String[] symbols) {
        this.symbols = symbols;
        for (int i = 0; i < symbols.length; i++) {
            symbolsToIndex.put(symbols[i], i);
        }
    }

    // -- variable creation --

    /** Creates {@code num} new symbolic variables. */
    public SymbolicVariable[] createVariables(int num) {
        SymbolicVariable[] ret = new SymbolicVariable[num];
        for (int i = 0; i < num; i++) {
            ret[i] = new SymbolicVariable(this, nextVariableId(), symbols.length);
            registerVariable(ret[i]);
        }
        return ret;
    }

    // -- symbol mapping --

    public String[] getSymbols() { return symbols; }

    public int indexOfSymbol(String symbol) {
        Integer idx = symbolsToIndex.get(symbol);
        if (idx == null) {
            throw new IllegalArgumentException("Symbol does not exist: " + symbol);
        }
        return idx;
    }

    public int[] indexArrayOfSymbols(String[] syms) {
        int[] ret = new int[syms.length];
        for (int i = 0; i < syms.length; i++) {
            ret[i] = indexOfSymbol(syms[i]);
        }
        return ret;
    }

    // -- constraint management --

    @Override
    public boolean addConstraint(Constraint c) {
        if (c instanceof SymbolicConstraint sc) {
            addToVarToConstraints(sc);
        }
        if (!super.addConstraint(c)) {
            if (c instanceof SymbolicConstraint sc) {
                removeFromVarToConstraints(sc);
            }
            return false;
        }
        return true;
    }

    @Override
    public void removeConstraint(Constraint c) {
        super.removeConstraint(c);
        if (c instanceof SymbolicConstraint sc) {
            removeFromVarToConstraints(sc);
        }
    }

    /** Adds a routed sub-constraint without auto-propagation
     *  (used by {@link CompoundSymbolicSolver}). */
    void addSubConstraint(SymbolicConstraint c) {
        constraintList().add(c);
        addToVarToConstraints(c);
    }

    /** Removes a routed sub-constraint (used by
     *  {@link CompoundSymbolicSolver}, e.g. for rollback). */
    void removeSubConstraint(SymbolicConstraint c) {
        constraintList().remove(c);
        removeFromVarToConstraints(c);
    }

    // -- propagation (AC-3) --

    @Override
    public boolean propagate() {
        Variable[] vars = getVariables();
        if (backupScratch == null || backupScratch.length < vars.length) {
            backupScratch = new SymbolicDomain[vars.length];
            for (int i = 0; i < backupScratch.length; i++) {
                backupScratch[i] = new SymbolicDomain(symbols.length);
            }
        }
        for (int i = 0; i < vars.length; i++) {
            SymbolicDomain dom = ((SymbolicVariable) vars[i]).domain();
            backupScratch[i].copyFrom(dom);
            dom.reset();
        }

        boolean success = ac3();

        if (!success) {
            for (int i = 0; i < vars.length; i++) {
                ((SymbolicVariable) vars[i]).domain().copyFrom(backupScratch[i]);
            }
            logger.info("NameFailure");
        }
        return success;
    }

    private boolean ac3() {
        Deque<SymbolicConstraint> worklist = new ArrayDeque<>();
        Set<SymbolicConstraint> queued =
                Collections.newSetFromMap(new IdentityHashMap<>());
        for (Constraint con : constraintList()) {
            enqueue((SymbolicConstraint) con, worklist, queued);
        }

        while (!worklist.isEmpty()) {
            SymbolicConstraint con = worklist.remove();
            queued.remove(con);
            int changed = revise(con);
            if (changed > 0) {
                if (!checkDomains(con)) return false;
                if (changed == 1) {
                    enqueueAll(constraintsOfVarExcept(con.from(), con), worklist, queued);
                } else if (changed == 2) {
                    enqueueAll(constraintsOfVarExcept(con.to(), con), worklist, queued);
                } else {
                    enqueueAll(constraintsOfVarExcept(con.from(), con), worklist, queued);
                    enqueueAll(constraintsOfVarExcept(con.to(), con), worklist, queued);
                }
            }
        }
        return true;
    }

    private static void enqueue(
            SymbolicConstraint con,
            Deque<SymbolicConstraint> worklist, Set<SymbolicConstraint> queued) {
        if (queued.add(con)) {
            worklist.add(con);
        }
    }

    private static void enqueueAll(
            List<SymbolicConstraint> cons,
            Deque<SymbolicConstraint> worklist, Set<SymbolicConstraint> queued) {
        for (SymbolicConstraint con : cons) {
            enqueue(con, worklist, queued);
        }
    }

    private boolean checkDomains(SymbolicConstraint con) {
        if (!((SymbolicVariable) con.from()).hasModel()) return false;
        if (con.type() != SymbolicConstraint.Type.UNARYEQUALS
                && con.type() != SymbolicConstraint.Type.UNARYDIFFERENT) {
            return ((SymbolicVariable) con.to()).hasModel();
        }
        return true;
    }

    /**
     * Applies one constraint revision.
     * @return 0, 1 (from changed), 2 (to changed), or 3 (both changed)
     */
    private int revise(SymbolicConstraint con) {
        switch (con.type()) {
            case UNARYEQUALS: {
                SymbolicDomain dom = ((SymbolicVariable) con.from()).domain();
                return dom.setUnaryEquals(con.getUnaryValues()) ? 1 : 0;
            }
            case UNARYDIFFERENT: {
                SymbolicDomain dom = ((SymbolicVariable) con.from()).domain();
                return dom.setUnaryDifferent(con.getUnaryValues()) ? 1 : 0;
            }
            case EQUALS: {
                SymbolicDomain dFrom = ((SymbolicVariable) con.from()).domain();
                SymbolicDomain dTo = ((SymbolicVariable) con.to()).domain();
                return dFrom.applyEqualsConstr(dTo);
            }
            case DIFFERENT: {
                SymbolicDomain dFrom = ((SymbolicVariable) con.from()).domain();
                SymbolicDomain dTo = ((SymbolicVariable) con.to()).domain();
                return dFrom.applyDifferentConstr(dTo);
            }
            default:
                return 0;
        }
    }

    // -- constraint adjacency (maintained incrementally) --

    private void addToVarToConstraints(SymbolicConstraint sc) {
        addVarConstraint(sc.from(), sc);
        addVarConstraint(sc.to(), sc);
    }

    private void removeFromVarToConstraints(SymbolicConstraint sc) {
        removeVarConstraint(sc.from(), sc);
        removeVarConstraint(sc.to(), sc);
    }

    private void addVarConstraint(Variable var, SymbolicConstraint con) {
        varToConstraints.computeIfAbsent(var, k -> new ArrayList<>()).add(con);
    }

    private void removeVarConstraint(Variable var, SymbolicConstraint con) {
        List<SymbolicConstraint> all = varToConstraints.get(var);
        if (all != null) {
            all.remove(con);
        }
    }

    private List<SymbolicConstraint> constraintsOfVarExcept(
            Variable var, SymbolicConstraint except) {
        List<SymbolicConstraint> ret = new ArrayList<>();
        List<SymbolicConstraint> all = varToConstraints.get(var);
        if (all != null) {
            for (SymbolicConstraint con : all) {
                if (con != except) ret.add(con);
            }
        }
        return ret;
    }
}
