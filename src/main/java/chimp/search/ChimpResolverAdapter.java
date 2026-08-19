package chimp.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.Variable;
import chimp.kernel.VariablePrototype;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.integer.IntegerConstraint;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.model.ResolverDescription;
import chimp.model.SubVariableKind;
import htn.PlanReportroryItem;

/**
 * Compiles resolver descriptions into CHIMP-native resolver networks and
 * performs scope repair during materialization.
 */
public final class ChimpResolverAdapter {

    private static final ChimpSearchAdapter SEARCH_ADAPTER = new ChimpSearchAdapter();

    public List<ConstraintNetwork> compile(
            PlanReportroryItem item,
            List<ResolverDescription> descriptions,
            Fluent taskFluent,
            int depth,
            FluentNetworkSolver groundSolver,
            Fluent[] openFluents) {
        if (descriptions.isEmpty()) {
            return List.of();
        }
        for (ResolverDescription description : descriptions) {
            if (description.repositoryItemId() != item.getId()) {
                throw new IllegalArgumentException("Resolver description belongs to another repository item");
            }
        }
        List<ConstraintNetwork> compiled = new ArrayList<>();
        for (ResolverDescription description : descriptions) {
            ConstraintNetwork network =
                    item.buildResolverFromDescription(description, taskFluent, depth, groundSolver, openFluents);
            if (network != null) {
                SEARCH_ADAPTER.annotateExpansion(network, taskFluent, description);
                tagConstraints(network);
                compiled.add(network);
            }
        }
        return List.copyOf(compiled);
    }

    private static void tagConstraints(ConstraintNetwork network) {
        Map<Constraint, SubVariableKind> tags = new HashMap<>();
        for (Constraint constraint : network.getConstraints()) {
            tags.put(constraint, subVariableKindOf(constraint));
        }
        network.setAnnotation(tags);
    }

    /**
     * Maps a CHIMP-native constraint to the sub-variable kind it operates on.
     * Used during scope repair to know which sub-variable to extract from a materialized Fluent.
     */
    public static SubVariableKind subVariableKindOf(Constraint constraint) {
        if (constraint instanceof CompoundSymbolicConstraint) {
            return SubVariableKind.SYMBOLIC;
        }
        if (constraint instanceof TemporalConstraint) {
            return SubVariableKind.TEMPORAL;
        }
        if (constraint instanceof IntegerConstraint) {
            return SubVariableKind.INTEGER;
        }
        return SubVariableKind.FLUENT;
    }

    /**
     * Resolves a scope entry (potentially a VariablePrototype) to the correct
     * sub-variable for the given constraint kind, using the substitution map
     * maintained by the constraint network.
     *
     * @param scopeEntry  the variable from the constraint's original scope
     * @param kind        the sub-variable kind of the owning constraint
     * @param network     the resolver network (provides prototype→real substitutions)
     * @return the resolved variable, or the original entry if it is not a prototype
     */
    @SuppressWarnings("unchecked")
    public static Variable resolveScopeVariable(
            Variable scopeEntry,
            SubVariableKind kind,
            ConstraintNetwork network) {
        if (scopeEntry instanceof VariablePrototype vp) {
            if (kind == SubVariableKind.INTEGER) {
                VariablePrototype fluentPrototype = vp.parentPrototype();
                int index = vp.subIndex();
                Fluent fluent = (Fluent) network.getSubstitution(fluentPrototype);
                return fluent.getIntegerVariables()[index];
            }
            Fluent fluent = (Fluent) network.getSubstitution(vp);
            return switch (kind) {
                case FLUENT -> fluent;
                case SYMBOLIC -> fluent.getCompoundSymbolicVariable();
                case TEMPORAL -> fluent.getTemporalVariable();
                default -> fluent;
            };
        }
        return scopeEntry;
    }
}
