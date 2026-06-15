package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ResolverDescriptionTest {

    @Test
    public void copiesNeutralResolverData() {
        Map<String, Long> preconditions = new LinkedHashMap<>();
        preconditions.put("p0", 4L);
        List<ResolverSemanticKind> kinds = new ArrayList<>(List.of(
                ResolverSemanticKind.PRECONDITION));

        ResolverDescription description = new ResolverDescription(
                1, "move", 2, 3, preconditions, List.of(), kinds,
                List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        preconditions.put("p1", 5L);
        kinds.add(ResolverSemanticKind.EFFECT);

        assertEquals(Map.of("p0", 4L), description.selectedPreconditions());
        assertEquals(List.of(ResolverSemanticKind.PRECONDITION), description.semanticKinds());
        assertThrows(UnsupportedOperationException.class,
                () -> description.selectedPreconditions().put("p2", 6L));
    }
}
