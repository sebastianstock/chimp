package chimp.kernel.symbolic;

import java.util.Arrays;
import java.util.Objects;

import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;

/**
 * CHIMP-native symbolic (name) variable.
 *
 * <p>Port of {@code unify.NameVariable}. Each variable has a
 * {@link SymbolicDomain} that tracks which symbols are currently possible.
 * Ground variables have exactly one possible symbol.
 */
public class SymbolicVariable extends Variable {

    private final SymbolicDomain domain;

    /**
     * Creates a new symbolic variable belonging to {@code solver}
     * with the given number of possible symbols.
     */
    public SymbolicVariable(ConstraintSolver solver, int id, int symbolCount) {
        super(solver, id);
        this.domain = new SymbolicDomain(this, symbolCount);
    }

    public SymbolicDomain domain() { return domain; }

    // -- domain access (Meta-CSP compatibility) --

    /** Use {@link #domain()} in new code. */
    public SymbolicDomain getDomain() { return domain; }

    /** Makes the variable ground — only the given symbol remains possible. */
    public void setConstant(String symbol) {
        SymbolicSolver solver = (SymbolicSolver) solver();
        int index = solver.indexOfSymbol(symbol);
        domain.setConstant(index);
    }

    /** All symbols currently possible for this variable. */
    public String[] possibleSymbols() {
        int[] indices = domain.possibleIndices();
        SymbolicSolver solver = (SymbolicSolver) solver();
        String[] symbols = solver.getSymbols();
        String[] ret = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            ret[i] = symbols[indices[i]];
        }
        return ret;
    }

    /** True if exactly one symbol is possible. */
    public boolean isGround() { return domain.isGround(); }

    /** True if at least one symbol is possible. */
    public boolean hasModel() { return domain.hasModel(); }

    // -- standard --

    @Override
    public String toString() {
        String[] syms = possibleSymbols();
        if (syms.length == 1) return syms[0];
        return Arrays.toString(syms);
    }

    /** Short representation: ground value, or {…} if unground. */
    public String toStringShort() {
        String[] syms = possibleSymbols();
        if (syms.length == 1) return syms[0];
        return "{...}";
    }
}
