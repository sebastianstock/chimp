package chimp.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;

import chimp.logging.ChimpLogging;

/**
 * CHIMP-owned replacement for {@code org.metacsp.framework.meta.MetaConstraint}.
 * Provides the meta-constraint search protocol (flaw enumeration +
 * variable ordering) through the {@link FlawProvider} port, without inheriting
 * from the Meta-CSP framework.
 *
 * <p>Subclasses implement {@link #getMetaVariables()} (flaw enumeration) and
 * {@link #resolvers(ChimpFlaw)} (resolver generation). The
 * {@link #currentFlaw()} method applies the variable ordering heuristic and
 * returns the highest-priority flaw. {@link #resolverOrdering()} returns the
 * value ordering heuristic.
 */
public abstract class ChimpMetaConstraint implements FlawProvider {

    protected final transient Logger logger = ChimpLogging.getLogger(this.getClass());

    protected Comparator<ChimpFlaw> varOH;
    protected Comparator<ConstraintNetwork> valOH;

    public ChimpMetaConstraint(Comparator<ChimpFlaw> varOH,
                               Comparator<ConstraintNetwork> valOH) {
        this.varOH = varOH;
        this.valOH = valOH;
    }

    /**
     * Returns all flaws according to this meta-constraint.
     * Must be implemented by subclasses.
     */
    public abstract ChimpFlaw[] getMetaVariables();

    /**
     * Returns all resolver candidates for the given flaw.
     * Replaces {@code getMetaValues(MetaVariable)} — the {@code MetaVariable}
     * wrapper is no longer needed; the flaw is passed directly.
     */
    @Override
    public abstract ConstraintNetwork[] resolvers(ChimpFlaw flaw);

    @Override
    public void markResolved(ChimpFlaw flaw, ConstraintNetwork resolver) {
        // no-op (matches legacy markResolvedSub)
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
    public Comparator<ConstraintNetwork> resolverOrdering() {
        return valOH;
    }

    public void setValOH(Comparator<ConstraintNetwork> valOH) {
        this.valOH = valOH;
    }

    public void setVarOH(Comparator<ChimpFlaw> varOH) {
        this.varOH = varOH;
    }
}
