package chimp.search;

import chimp.kernel.fluent.Fluent;

/**
 * CHIMP-native replacement for {@code org.metacsp.meta.symbolsAndTime.MCSData}.
 *
 * <p>Captures a minimal critical set (MCS) ordering decision: a pair of
 * conflicting activities and the temporal bounds required to order them.
 */
public final class MCSData implements Comparable<MCSData> {

    public final float pc;
    public final Fluent mcsActFrom;
    public final Fluent mcsActTo;
    public final float k;

    public MCSData(float pc, Fluent mcsActFrom, Fluent mcsActTo, float k) {
        this.pc = pc;
        this.mcsActFrom = mcsActFrom;
        this.mcsActTo = mcsActTo;
        this.k = k;
    }

    @Override
    public int compareTo(MCSData other) {
        return Float.compare(this.pc, other.pc);
    }
}
