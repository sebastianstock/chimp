package hybridDomainParsing.classic.antlr;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import chimp.model.ResourceCapacity;

public class ResourceCapacityParsingTest {

    @Test
    public void parsesResourceCapacityWithoutCreatingScheduler() {
        ChimpClassicLexer lexer = new ChimpClassicLexer(
                CharStreams.fromString("(Resource armCapacity 2)"));
        ChimpClassicParser parser = new ChimpClassicParser(new CommonTokenStream(lexer));
        ChimpClassicReader reader = new ChimpClassicReader(Map.of());

        ResourceCapacity capacity = reader.visitResource_def(parser.resource_def());

        assertEquals(new ResourceCapacity("armCapacity", 2), capacity);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }
}
