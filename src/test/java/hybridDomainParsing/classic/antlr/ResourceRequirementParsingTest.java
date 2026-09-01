package hybridDomainParsing.classic.antlr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;

public class ResourceRequirementParsingTest {

    private final ChimpClassicReader reader = new ChimpClassicReader(Map.of());

    @Test
    public void parsesActionResourceRequirement() {
        ChimpClassicParser parser = parser(
                "(ResourceUsage (Usage armCapacity 2) (Param 1 ur5))");

        ResourceRequirement requirement = reader.visitResource_usage_def(parser.resource_usage_def());

        assertEquals("armCapacity", requirement.resourceName());
        assertEquals(2, requirement.usageLevel());
        assertFalse(requirement.fluentType().isPresent());
        assertEquals(new ResourceParameterRequirement(1, "ur5"),
                requirement.parameterRequirements().get(0));
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void parsesFluentResourceRequirement() {
        ChimpClassicParser parser = parser(
                "(FluentResourceUsage (Usage armCapacity 1) (Fluent Holding) (Param 1 ur5))");

        ResourceRequirement requirement = reader.visitFluentresourceusage_def(
                parser.fluentresourceusage_def());

        assertEquals("Holding", requirement.fluentType().orElseThrow());
        assertEquals(new ResourceParameterRequirement(1, "ur5"),
                requirement.parameterRequirements().get(0));
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    private ChimpClassicParser parser(String input) {
        ChimpClassicLexer lexer = new ChimpClassicLexer(CharStreams.fromString(input));
        return new ChimpClassicParser(new CommonTokenStream(lexer));
    }
}
