package hybridDomainParsing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import chimp.model.StateVariableDefinition;

public class StateVariableLegacyParsingTest {

    @Test
    public void parsesNeutralStateVariableDefinition() {
        StateVariableDefinition definition = HybridDomain.parseStateVariableDefinition(
                "arm_posture 2 folded extended");

        assertEquals(
                new StateVariableDefinition("arm_posture", 2, List.of("folded", "extended")),
                definition);
    }
}
