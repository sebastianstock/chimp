package chimp.adapter.metacsp;

import chimp.model.ResolverDescription;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.PlanReportroryItem;
import fluentSolver.FluentConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<ConstraintNetwork> compiled =
                item.materializeResolverDescriptions(taskFluent, depth, groundSolver, openFluents);
        List<List<Long>> describedSelections = descriptions.stream()
                .map(description -> sortedIds(description.selectedPreconditions().values()))
                .toList();
        List<ConstraintNetwork> validated = new ArrayList<>();
        for (ConstraintNetwork network : compiled) {
            List<Long> selection = selectedPreconditionIds(network);
            for (int i = 0; i < descriptions.size(); i++) {
                if (describedSelections.get(i).equals(selection)) {
                    SEARCH_ADAPTER.annotateExpansion(network, taskFluent, descriptions.get(i));
                    validated.add(network);
                    break;
                }
            }
        }
        return List.copyOf(validated);
    }

    private List<Long> selectedPreconditionIds(ConstraintNetwork network) {
        List<Long> ids = new ArrayList<>();
        for (Constraint constraint : network.getConstraints()) {
            if (constraint instanceof FluentConstraint fluentConstraint
                    && fluentConstraint.getType() == FluentConstraint.Type.PRE) {
                ids.add((long) fluentConstraint.getFrom().getID());
            }
        }
        Collections.sort(ids);
        return List.copyOf(ids);
    }

    private List<Long> sortedIds(java.util.Collection<Long> values) {
        List<Long> ids = new ArrayList<>(values);
        Collections.sort(ids);
        return List.copyOf(ids);
    }
}
