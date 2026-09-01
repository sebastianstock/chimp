package hybridDomainParsing.classic.antlr;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import chimp.model.TemporalBounds;
import chimp.model.TemporalRelation;
import htn.AdditionalConstraintTemplate;

public class TemporalConstraintParsingTest {

    @Test
    public void parsesTemporalConstraintIntoNeutralValue() {
        ChimpClassicLexer lexer = new ChimpClassicLexer(
                CharStreams.fromString("(Constraint Duration[5,INF](task))"));
        ChimpClassicParser parser = new ChimpClassicParser(new CommonTokenStream(lexer));
        ChimpClassicReader reader = new ChimpClassicReader(Map.of());

        AdditionalConstraintTemplate parsed = reader.visitTemporal_constraint_def(
                parser.temporal_constraint_def());

        assertEquals(TemporalRelation.Duration, parsed.getConstraint().relation());
        assertEquals(new TemporalBounds(5, TemporalBounds.INFINITY),
                parsed.getConstraint().bounds().get(0));
        assertEquals("task", parsed.getFromKey());
        assertEquals("task", parsed.getToKey());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }
}
