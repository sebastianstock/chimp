package chimp.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ResolverDescription {
    private final int repositoryItemId;
    private final String repositoryItemName;
    private final int depth;
    private final int preferenceWeight;
    private final Map<String, Long> selectedPreconditions;
    private final List<FluentPrototypeDescription> prototypes;
    private final List<ResolverSemanticKind> semanticKinds;
    private final List<SymbolicConstraintDescription> symbolicConstraints;
    private final List<ResolverOrderingEdge> orderingEdges;
    private final List<ResolverTemporalConstraintDescription> temporalConstraints;
    private final List<ResolverIntegerConstraintDescription> integerConstraints;
    private final List<ResourceRequirement> resourceRequirements;
    private final int bindingCount;

    public ResolverDescription(
            int repositoryItemId,
            String repositoryItemName,
            int depth,
            int preferenceWeight,
            Map<String, Long> selectedPreconditions,
            List<FluentPrototypeDescription> prototypes,
            List<ResolverSemanticKind> semanticKinds,
            List<SymbolicConstraintDescription> symbolicConstraints,
            List<ResolverOrderingEdge> orderingEdges,
            List<ResolverTemporalConstraintDescription> temporalConstraints,
            List<ResolverIntegerConstraintDescription> integerConstraints,
            List<ResourceRequirement> resourceRequirements,
            int bindingCount) {
        if (repositoryItemId < 0 || depth < 0 || bindingCount < 0) {
            throw new IllegalArgumentException("IDs, depth, and bindingCount must not be negative");
        }
        this.repositoryItemId = repositoryItemId;
        this.repositoryItemName = Objects.requireNonNull(repositoryItemName, "repositoryItemName");
        this.depth = depth;
        this.preferenceWeight = preferenceWeight;
        this.selectedPreconditions = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(selectedPreconditions, "selectedPreconditions")));
        this.prototypes = List.copyOf(Objects.requireNonNull(prototypes, "prototypes"));
        this.semanticKinds = List.copyOf(Objects.requireNonNull(semanticKinds, "semanticKinds"));
        this.symbolicConstraints = List.copyOf(
                Objects.requireNonNull(symbolicConstraints, "symbolicConstraints"));
        this.orderingEdges = List.copyOf(Objects.requireNonNull(orderingEdges, "orderingEdges"));
        this.temporalConstraints = List.copyOf(
                Objects.requireNonNull(temporalConstraints, "temporalConstraints"));
        this.integerConstraints = List.copyOf(
                Objects.requireNonNull(integerConstraints, "integerConstraints"));
        this.resourceRequirements = List.copyOf(
                Objects.requireNonNull(resourceRequirements, "resourceRequirements"));
        this.bindingCount = bindingCount;
    }

    public int repositoryItemId() { return repositoryItemId; }
    public String repositoryItemName() { return repositoryItemName; }
    public int depth() { return depth; }
    public int preferenceWeight() { return preferenceWeight; }
    public Map<String, Long> selectedPreconditions() { return selectedPreconditions; }
    public List<FluentPrototypeDescription> prototypes() { return prototypes; }
    public List<ResolverSemanticKind> semanticKinds() { return semanticKinds; }
    public List<SymbolicConstraintDescription> symbolicConstraints() { return symbolicConstraints; }
    public List<ResolverOrderingEdge> orderingEdges() { return orderingEdges; }
    public List<ResolverTemporalConstraintDescription> temporalConstraints() { return temporalConstraints; }
    public List<ResolverIntegerConstraintDescription> integerConstraints() { return integerConstraints; }
    public List<ResourceRequirement> resourceRequirements() { return resourceRequirements; }
    public int bindingCount() { return bindingCount; }
}
