package hybridDomainParsing.classic.antlr;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import chimp.model.StateVariableDefinition;

public class StateVariableParsingTest {

    @Test
    public void parsesNeutralStateVariableDefinition() {
        ChimpClassicLexer lexer = new ChimpClassicLexer(
                CharStreams.fromString("(StateVariable arm_posture 2 folded extended)"));
        ChimpClassicParser parser = new ChimpClassicParser(new CommonTokenStream(lexer));
        ChimpClassicReader reader = new ChimpClassicReader(Map.of());

        StateVariableDefinition definition = reader.visitStatevariable_def(parser.statevariable_def());

        assertEquals(
                new StateVariableDefinition("arm_posture", 2, List.of("folded", "extended")),
                definition);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }
}
