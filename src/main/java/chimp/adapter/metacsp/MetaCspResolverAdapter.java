package chimp.adapter.metacsp;

import chimp.model.ResolverDescription;
import chimp.model.SubVariableKind;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.PlanReportroryItem;
import integers.IntegerConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import unify.CompoundSymbolicValueConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetaCspResolverAdapter {
    private static final MetaCspSearchAdapter SEARCH_ADAPTER = new MetaCspSearchAdapter();

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
     * Maps a Meta-CSP constraint to the sub-variable kind it operates on.
     * Used during scope repair to know which sub-variable to extract from a materialized Fluent.
     */
    public static SubVariableKind subVariableKindOf(Constraint constraint) {
        if (constraint instanceof CompoundSymbolicValueConstraint) {
            return SubVariableKind.SYMBOLIC;
        }
        if (constraint instanceof AllenIntervalConstraint) {
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
                VariablePrototype fluentPrototype = (VariablePrototype) vp.getParameters()[0];
                int index = (int) vp.getParameters()[1];
                Fluent fluent = (Fluent) network.getSubstitution(fluentPrototype);
                return fluent.getIntegerVariables()[index];
            }
            Fluent fluent = (Fluent) network.getSubstitution(vp);
            return switch (kind) {
                case FLUENT -> fluent;
                case SYMBOLIC -> fluent.getCompoundSymbolicVariable();
                case TEMPORAL -> fluent.getAllenInterval();
                default -> fluent;
            };
        }
        return scopeEntry;
    }
}
