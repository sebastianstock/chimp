package hybridDomainParsing.classic.antlr;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import chimp.model.IntegerConstraintKind;
import htn.IntegerConstraintTemplate;

public class IntegerConstraintParsingTest {

    @Test
    public void parsesIntegerConstraintIntoNeutralTemplate() {
        ChimpClassicLexer lexer = new ChimpClassicLexer(
                CharStreams.fromString("(IC ?end = ?start - 2)"));
        ChimpClassicParser parser = new ChimpClassicParser(new CommonTokenStream(lexer));
        ChimpClassicReader reader = new ChimpClassicReader(Map.of());

        IntegerConstraintTemplate parsed = reader.visitInteger_constraint_def(
                parser.integer_constraint_def());

        assertEquals(IntegerConstraintKind.ARITHMETIC, parsed.kind());
        assertArrayEquals(new String[] {"?end", "?start"}, parsed.variableKeys());
        assertEquals("=", parsed.operator1());
        assertEquals("-", parsed.operator2());
        assertEquals(2, parsed.constant());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }
}
