package chimp.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ProblemDefinitionTest {

    @Test
    public void copiesCollectionsAndPreservesTypeDeclarationOrder() {
        List<String> symbols = new ArrayList<>(List.of("a"));
        Map<String, List<String>> types = new LinkedHashMap<>();
        types.put("robot", new ArrayList<>(List.of("r1")));
        types.put("location", List.of("l1"));

        ProblemDefinition definition = new ProblemDefinition(
                symbols, types, List.of(), List.of(), List.of(), List.of());
        symbols.add("b");
        types.get("robot").add("r2");

        assertEquals(List.of("a"), definition.argumentSymbols());
        assertEquals(List.of("robot", "location"), List.copyOf(definition.typeInstances().keySet()));
        assertEquals(List.of("r1"), definition.typeInstances().get("robot"));
        assertThrows(UnsupportedOperationException.class,
                () -> definition.typeInstances().put("item", List.of("i1")));
    }
}
