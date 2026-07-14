package chimp.kernel.symbolic;

import java.util.*;
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
 */
public class SymbolicSolver extends ConstraintSolver {

    private static final Logger logger = ChimpLogging.getLogger(SymbolicSolver.class);

    private final String[] symbols;
    private final Map<String, Integer> symbolsToIndex = new HashMap<>();
    private final Map<Variable, List<SymbolicConstraint>> varToConstraints = new HashMap<>();

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

    // -- propagation (AC-3) --

    @Override
    public boolean propagate() {
        buildVarToConstraints();

        Variable[] vars = getVariables();
        SymbolicDomain[] backups = new SymbolicDomain[vars.length];
        for (int i = 0; i < vars.length; i++) {
            SymbolicDomain dom = ((SymbolicVariable) vars[i]).domain();
            backups[i] = new SymbolicDomain(dom);
            dom.reset();
        }

        boolean success = ac3();

        if (!success) {
            for (int i = 0; i < vars.length; i++) {
                ((SymbolicVariable) vars[i]).domain().copyFrom(backups[i]);
            }
            logger.info("NameFailure");
        }
        return success;
    }

    private boolean ac3() {
        Deque<SymbolicConstraint> worklist = new ArrayDeque<>();
        for (Constraint con : getConstraints()) {
            worklist.add((SymbolicConstraint) con);
        }

        while (!worklist.isEmpty()) {
            SymbolicConstraint con = worklist.remove();
            int changed = revise(con);
            if (changed > 0) {
                if (!checkDomains(con)) return false;
                if (changed == 1) {
                    worklist.addAll(constraintsOfVarExcept(con.from(), con));
                } else if (changed == 2) {
                    worklist.addAll(constraintsOfVarExcept(con.to(), con));
                } else {
                    worklist.addAll(constraintsOfVarExcept(con.from(), con));
                    worklist.addAll(constraintsOfVarExcept(con.to(), con));
                }
            }
        }
        return true;
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

    // -- constraint adjacency --

    private void buildVarToConstraints() {
        varToConstraints.clear();
        for (Constraint con : getConstraints()) {
            if (con instanceof SymbolicConstraint sc) {
                addVarConstraint(sc.from(), sc);
                addVarConstraint(sc.to(), sc);
            }
        }
    }

    private void addVarConstraint(Variable var, SymbolicConstraint con) {
        varToConstraints.computeIfAbsent(var, k -> new ArrayList<>()).add(con);
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
