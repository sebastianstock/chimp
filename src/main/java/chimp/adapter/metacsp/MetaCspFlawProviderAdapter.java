package chimp.adapter.metacsp;

import chimp.search.FlawProvider;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import java.util.Comparator;

/**
 * Adapts a Meta-CSP {@link MetaConstraint} to the CHIMP-owned
 * {@link FlawProvider} port, so {@link chimp.search.ChimpSearchEngine}
 * can drive it without knowing about the Meta-CSP search protocol.
 *
 * <p>Each method delegates to the corresponding {@code MetaConstraint}
 * method, wrapping/unwrapping {@link MetaVariable} as needed.
 */
public final class MetaCspFlawProviderAdapter implements FlawProvider {

    private final MetaConstraint metaConstraint;

    public MetaCspFlawProviderAdapter(MetaConstraint metaConstraint) {
        this.metaConstraint = metaConstraint;
    }

    public MetaConstraint metaConstraint() {
        return metaConstraint;
    }

    @Override
    public ConstraintNetwork currentFlaw() {
        return metaConstraint.getMetaVariable();
    }

    @Override
    public ConstraintNetwork[] resolvers(ConstraintNetwork flaw) {
        return metaConstraint.getMetaValues(new MetaVariable(metaConstraint, flaw));
    }

    @Override
    public void markResolved(ConstraintNetwork flaw, ConstraintNetwork resolver) {
        metaConstraint.markResolvedSub(new MetaVariable(metaConstraint, flaw), resolver);
    }

    @Override
    public Comparator<ConstraintNetwork> resolverOrdering() {
        return metaConstraint.getValOH();
    }
}
