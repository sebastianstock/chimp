package chimp.kernel.symbolic;

import java.util.Arrays;

/**
 * CHIMP-native domain for symbolic (name) variables.
 *
 * <p>Port of {@code unify.NameDomain}. Tracks which symbols are currently
 * possible as a bit set (one bit per symbol). Supports unary
 * (equals/different) and binary (equals/different) revision operations
 * used by the AC-3 propagation in {@link SymbolicSolver}.
 *
 * <p>Note: {@link #constantIndex} is a cache for the "grounded to a
 * single symbol via {@link #setConstant}" case. It is deliberately
 * <i>not</i> invalidated when {@link #applyDifferentConstr} clears bits —
 * this replicates the legacy {@code NameDomain} behavior exactly.
 */
public final class SymbolicDomain {

    private final SymbolicVariable variable;
    private final int symbolCount;
    private final long[] bits;

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
        this.symbolCount = symbolCount;
        this.bits = new long[words(symbolCount)];
        setAllBits();
    }

    /** Scratch domain without an owning variable (used for AC-3 backups). */
    SymbolicDomain(int symbolCount) {
        this.variable = null;
        this.symbolCount = symbolCount;
        this.bits = new long[words(symbolCount)];
    }

    /** Copy constructor (used for AC-3 backup). */
    SymbolicDomain(SymbolicDomain other) {
        this.variable = other.variable;
        this.symbolCount = other.symbolCount;
        this.bits = other.bits.clone();
        this.constantIndex = other.constantIndex;
    }

    SymbolicVariable variable() { return variable; }

    private static int words(int symbolCount) {
        return (symbolCount + 63) >> 6;
    }

    private static long lastWordMask(int symbolCount) {
        int rem = symbolCount & 63;
        return rem == 0 ? -1L : (1L << rem) - 1;
    }

    private void setAllBits() {
        Arrays.fill(bits, -1L);
        bits[bits.length - 1] &= lastWordMask(symbolCount);
    }

    private boolean get(int index) {
        return (bits[index >> 6] & (1L << index)) != 0;
    }

    private void clear(int index) {
        bits[index >> 6] &= ~(1L << index);
    }

    // -- state queries --

    /** Reset to initial state: all true, or just the constant. */
    void reset() {
        if (constantIndex >= 0) {
            Arrays.fill(bits, 0L);
            bits[constantIndex >> 6] |= 1L << constantIndex;
        } else {
            setAllBits();
        }
    }

    /** Indices of currently possible symbols, in ascending order. */
    public int[] possibleIndices() {
        if (constantIndex >= 0) {
            return new int[] { constantIndex };
        }
        int cardinality = 0;
        for (long word : bits) {
            cardinality += Long.bitCount(word);
        }
        int[] ret = new int[cardinality];
        int out = 0;
        for (int w = 0; w < bits.length && out < cardinality; w++) {
            long word = bits[w];
            while (word != 0) {
                int bit = Long.numberOfTrailingZeros(word);
                ret[out++] = (w << 6) + bit;
                word &= word - 1;
            }
        }
        return ret;
    }

    /** True if at least one symbol is still possible. */
    public boolean hasModel() {
        for (long word : bits) {
            if (word != 0L) return true;
        }
        return false;
    }

    /**
     * True if exactly one symbol is possible. Note: replicates the legacy
     * {@code NameDomain.isGround()}, which also returns true for an
     * <b>empty</b> domain (subsequent index access then throws, which the
     * compound solver treats as a failed constraint).
     */
    public boolean isGround() {
        if (constantIndex >= 0) return true;
        int cardinality = 0;
        for (long word : bits) {
            cardinality += Long.bitCount(word);
            if (cardinality > 1) return false;
        }
        return true;
    }

    /**
     * The single possible index; only meaningful when {@link #isGround()}
     * is true. Prefers the constant cache, mirroring
     * {@code possibleIndices()[0]} under {@code isGround()}. Throws for an
     * empty domain (mirroring the legacy out-of-bounds access).
     */
    private int soleIndex() {
        if (constantIndex >= 0) return constantIndex;
        for (int w = 0; w < bits.length; w++) {
            if (bits[w] != 0L) {
                return (w << 6) + Long.numberOfTrailingZeros(bits[w]);
            }
        }
        throw new IllegalStateException("empty domain has no index");
    }

    // -- mutation --

    /** Makes this domain a constant equal to the given symbol index. */
    void setConstant(int index) {
        if (index < 0 || index >= symbolCount) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        Arrays.fill(bits, 0L);
        bits[index >> 6] |= 1L << index;
        constantIndex = index;
    }

    int constantIndex() { return constantIndex; }

    // -- revision operations (used by AC-3) --

    /**
     * Unary equals: retain only the values in {@code unaryValues}.
     * Walks the value list exactly like the legacy implementation
     * (which assumes a sorted array).
     * @return true if the domain changed
     */
    boolean setUnaryEquals(int[] unaryValues) {
        boolean changed = false;
        int vi = 0;
        for (int di = 0; di < symbolCount; di++) {
            if (di != unaryValues[vi]) {
                if (get(di)) {
                    clear(di);
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
            if (get(i)) {
                clear(i);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Binary equals: both domains converge to the intersection of their
     * current values.
     * @return 0, 1 (from changed), 2 (to changed), or 3 (both changed)
     */
    int applyEqualsConstr(SymbolicDomain to) {
        if (symbolCount != to.symbolCount) {
            throw new IllegalArgumentException("Domain sizes differ");
        }
        boolean changedFrom = false, changedTo = false;
        for (int w = 0; w < bits.length; w++) {
            long a = this.bits[w];
            long b = to.bits[w];
            long inter = a & b;
            if (a != inter) {
                this.bits[w] = inter;
                changedFrom = true;
            }
            if (b != inter) {
                to.bits[w] = inter;
                changedTo = true;
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
        if (symbolCount != to.symbolCount) {
            throw new IllegalArgumentException("Domain sizes differ");
        }
        if (isGround()) {
            int fromIdx = soleIndex();
            if (to.isGround()) {
                int toIdx = to.soleIndex();
                if (fromIdx == toIdx) {
                    clear(toIdx);
                    to.clear(toIdx);
                    return 3;
                }
            } else {
                if (to.get(fromIdx)) {
                    to.clear(fromIdx);
                    return 2;
                }
            }
        } else if (to.isGround()) {
            int toIdx = to.soleIndex();
            if (get(toIdx)) {
                clear(toIdx);
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
        if (this.bits.length != other.bits.length) {
            throw new IllegalArgumentException("Domain lengths differ");
        }
        System.arraycopy(other.bits, 0, this.bits, 0, this.bits.length);
        this.constantIndex = other.constantIndex;
    }
}
