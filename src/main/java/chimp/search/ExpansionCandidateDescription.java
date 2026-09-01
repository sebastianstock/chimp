package chimp.search;

import chimp.model.ResolverDescription;

import java.util.Objects;

public record ExpansionCandidateDescription(ResolverDescription resolverDescription)
        implements ResolverCandidateDescription {
    public ExpansionCandidateDescription {
        Objects.requireNonNull(resolverDescription, "resolverDescription");
    }
}
