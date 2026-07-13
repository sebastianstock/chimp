package chimp.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;

import chimp.logging.ChimpLogging;

/**
 * CHIMP-owned replacement for {@code org.metacsp.framework.meta.MetaConstraint}.
 * Provides the meta-constraint search protocol ({@code getMetaVariables} +
 * variable ordering) through the {@link FlawProvider} port, without inheriting
 * from the Meta-CSP framework.
 *
 * <p>Subclasses implement {@link #getMetaVariables()} (flaw enumeration) and
 * {@link #resolvers(ConstraintNetwork)} (resolver generation). The
 * {@link #currentFlaw()} method applies the variable ordering heuristic and
 * returns the highest-priority flaw. {@link #resolverOrdering()} returns the
 * value ordering heuristic.
 */
public abstract class ChimpMetaConstraint implements FlawProvider {

    protected final transient Logger logger = ChimpLogging.getLogger(this.getClass());

    protected Comparator<ConstraintNetwork> varOH;
    protected Comparator<ConstraintNetwork> valOH;

    public ChimpMetaConstraint(Comparator<ConstraintNetwork> varOH,
                               Comparator<ConstraintNetwork> valOH) {
        this.varOH = varOH;
        this.valOH = valOH;
    }

    /**
     * Returns all flaws (meta-variables) according to this meta-constraint.
     * Must be implemented by subclasses.
     */
    public abstract ConstraintNetwork[] getMetaVariables();

    /**
     * Returns all resolver candidates for the given flaw network.
     * Replaces {@code getMetaValues(MetaVariable)} — the {@code MetaVariable}
     * wrapper is no longer needed; the flaw network is passed directly.
     */
    @Override
    public abstract ConstraintNetwork[] resolvers(ConstraintNetwork flawNetwork);

    @Override
    public void markResolved(ConstraintNetwork flaw, ConstraintNetwork resolver) {
        // no-op (matches legacy markResolvedSub)
    }

    @Override
    public ConstraintNetwork currentFlaw() {
        ConstraintNetwork[] vars = getMetaVariables();
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

    public void setVarOH(Comparator<ConstraintNetwork> varOH) {
        this.varOH = varOH;
    }
}
