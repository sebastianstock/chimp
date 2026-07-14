package chimp.kernel.symbolic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CHIMP-native domain for symbolic (name) variables.
 *
 * <p>Port of {@code unify.NameDomain}. Maintains a boolean array
 * indicating which symbols are currently possible. Supports unary
 * (equals/different) and binary (equals/different) revision operations
 * used by the AC-3 propagation in {@link SymbolicSolver}.
 */
public final class SymbolicDomain {

    private final SymbolicVariable variable;
    private final boolean[] domain;

    /** -1 if not a constant; otherwise the index of the sole allowed value. */
    private int constantIndex = -1;

    /**
     * Creates a domain for the given variable with all {@code symbolCount}
     * symbols initially possible.
     */
    SymbolicDomain(SymbolicVariable variable, int symbolCount) {
        if (symbolCount < 1) {
            throw new IllegalArgumentException("symbolCount must be > 0");
        }
        this.variable = variable;
        this.domain = new boolean[symbolCount];
        Arrays.fill(this.domain, true);
    }

    /** Copy constructor (used for AC-3 backup). */
    SymbolicDomain(SymbolicDomain other) {
        this.variable = other.variable;
        this.domain = Arrays.copyOf(other.domain, other.domain.length);
        this.constantIndex = other.constantIndex;
    }

    SymbolicVariable variable() { return variable; }

    // -- state queries --

    /** Reset to initial state: all true, or just the constant. */
    void reset() {
        if (constantIndex >= 0) {
            Arrays.fill(domain, false);
            domain[constantIndex] = true;
        } else {
            Arrays.fill(domain, true);
        }
    }

    /** Indices of currently possible symbols. */
    public int[] possibleIndices() {
        if (constantIndex >= 0) {
            return new int[] { constantIndex };
        }
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < domain.length; i++) {
            if (domain[i]) list.add(i);
        }
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++) ret[i] = list.get(i);
        return ret;
    }

    /** True if at least one symbol is still possible. */
    public boolean hasModel() {
        for (boolean b : domain) {
            if (b) return true;
        }
        return false;
    }

    /** True if exactly one symbol is possible. */
    public boolean isGround() {
        if (constantIndex >= 0) return true;
        boolean found = false;
        for (boolean b : domain) {
            if (b) {
                if (found) return false;
                found = true;
            }
        }
        return true;
    }

    // -- mutation --

    /** Makes this domain a constant equal to the given symbol index. */
    void setConstant(int index) {
        if (index < 0 || index >= domain.length) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        Arrays.fill(domain, false);
        domain[index] = true;
        constantIndex = index;
    }

    int constantIndex() { return constantIndex; }

    // -- revision operations (used by AC-3) --

    /**
     * Unary equals: retain only the values in {@code unaryValues}.
     * @return true if the domain changed
     */
    boolean setUnaryEquals(int[] unaryValues) {
        boolean changed = false;
        int vi = 0;
        for (int di = 0; di < domain.length; di++) {
            if (di != unaryValues[vi]) {
                if (domain[di]) {
                    domain[di] = false;
                    changed = true;
                }
            } else {
                if (vi < unaryValues.length - 1) vi++;
            }
        }
        return changed;
    }

    /**
     * Unary different: remove the values in {@code unaryValues}.
     * @return true if the domain changed
     */
    boolean setUnaryDifferent(int[] unaryValues) {
        boolean changed = false;
        for (int i : unaryValues) {
            if (domain[i]) {
                domain[i] = false;
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Binary equals: mutually prune domains.
     * @return 0, 1 (from changed), 2 (to changed), or 3 (both changed)
     */
    int applyEqualsConstr(SymbolicDomain to) {
        if (domain.length != to.domain.length) {
            throw new IllegalArgumentException("Domain sizes differ");
        }
        boolean changedFrom = false, changedTo = false;
        for (int i = 0; i < domain.length; i++) {
            if (this.domain[i]) {
                if (!to.domain[i]) { this.domain[i] = false; changedFrom = true; }
            } else {
                if (to.domain[i]) { to.domain[i] = false; changedTo = true; }
            }
        }
        int ret = 0;
        if (changedFrom) ret += 1;
        if (changedTo) ret += 2;
        return ret;
    }

    /**
     * Binary different: if one is ground, prune it from the other.
     * If both are ground and equal, prune both.
     * @return 0, 1 (from changed), 2 (to changed), or 3 (both changed)
     */
    int applyDifferentConstr(SymbolicDomain to) {
        if (domain.length != to.domain.length) {
            throw new IllegalArgumentException("Domain sizes differ");
        }
        if (this.isGround()) {
            int fromIdx = this.possibleIndices()[0];
            if (to.isGround()) {
                int toIdx = to.possibleIndices()[0];
                if (fromIdx == toIdx) {
                    this.domain[toIdx] = false;
                    to.domain[toIdx] = false;
                    return 3;
                }
            } else {
                if (to.domain[fromIdx]) {
                    to.domain[fromIdx] = false;
                    return 2;
                }
            }
        } else if (to.isGround()) {
            int toIdx = to.possibleIndices()[0];
            if (this.domain[toIdx]) {
                this.domain[toIdx] = false;
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return Arrays.toString(possibleIndices());
    }

    /** Copies the state of {@code other} into this domain (for AC-3 restore). */
    void copyFrom(SymbolicDomain other) {
        if (this.domain.length != other.domain.length) {
            throw new IllegalArgumentException("Domain lengths differ");
        }
        System.arraycopy(other.domain, 0, this.domain, 0, this.domain.length);
        this.constantIndex = other.constantIndex;
    }
}
