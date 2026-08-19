package chimp.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

import chimp.kernel.ConstraintNetwork;

import chimp.logging.ChimpLogging;

/**
 * CHIMP-owned replacement for {@code org.metacsp.framework.meta.MetaConstraint}.
 * Provides the meta-constraint search protocol (flaw enumeration +
 * variable ordering) through the {@link FlawProvider} port.
 *
 * <p>Subclasses implement {@link #getMetaVariables()} (flaw enumeration) and
 * {@link #resolvers(ChimpFlaw)} (resolver generation). The
 * {@link #currentFlaw()} method applies the variable ordering heuristic and
 * returns the highest-priority flaw.
 *
 * <p>Value ordering ({@code valOH}) is stored internally as a
 * {@code Comparator<ConstraintNetwork>} because all existing heuristics
 * compare the internal Meta-CSP representation. The
 * {@link #resolverOrdering()} method wraps it as a CHIMP-owned
 * {@code Comparator<ChimpResolver>} for the search interface.
 */
public abstract class ChimpMetaConstraint implements FlawProvider {

    protected final transient Logger logger = ChimpLogging.getLogger(this.getClass());

    protected Comparator<ChimpFlaw> varOH;
    /** Internal: all existing heuristics compare ConstraintNetwork. */
    protected Comparator<ConstraintNetwork> valOH;

    public ChimpMetaConstraint(Comparator<ChimpFlaw> varOH,
                               Comparator<ConstraintNetwork> valOH) {
        this.varOH = varOH;
        this.valOH = valOH;
    }

    /** Returns all flaws according to this meta-constraint. */
    public abstract ChimpFlaw[] getMetaVariables();

    @Override
    public abstract ChimpResolver[] resolvers(ChimpFlaw flaw);

    @Override
    public void markResolved(ChimpFlaw flaw, ChimpResolver resolver) {
        // no-op
    }

    @Override
    public ChimpFlaw currentFlaw() {
        ChimpFlaw[] vars = getMetaVariables();
        if (vars != null && vars.length > 0) {
            if (varOH != null) {
                Arrays.sort(vars, varOH);
            }
            return vars[0];
        }
        return null;
    }

    @Override
    public Comparator<ChimpResolver> resolverOrdering() {
        if (valOH == null) return null;
        return (r1, r2) -> valOH.compare(r1.delegate, r2.delegate);
    }

    public void setValOH(Comparator<ConstraintNetwork> valOH) {
        this.valOH = valOH;
    }

    public void setVarOH(Comparator<ChimpFlaw> varOH) {
        this.varOH = varOH;
    }
}
