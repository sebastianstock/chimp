package chimp.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class StateVariableDefinitionTest {

    @Test
    public void copiesExclusiveValues() {
        List<String> values = new ArrayList<>(List.of("left", "right"));

        StateVariableDefinition definition = new StateVariableDefinition("arm", 1, values);
        values.add("stowed");

        assertEquals(List.of("left", "right"), definition.exclusiveValues());
    }
}
