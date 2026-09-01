package htn;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import chimp.model.IntegerConstraintKind;

public class IntegerConstraintTemplateTest {

    @Test
    public void storesIndependentNeutralValues() {
        String[] keys = {"?start", "?end"};
        IntegerConstraintTemplate template = new IntegerConstraintTemplate(
                IntegerConstraintKind.ARITHMETIC,
                keys,
                "=",
                "-",
                2);

        keys[0] = "changed";
        String[] returnedKeys = template.variableKeys();
        returnedKeys[0] = "changed again";

        assertEquals(IntegerConstraintKind.ARITHMETIC, template.kind());
        assertArrayEquals(new String[] {"?start", "?end"}, template.variableKeys());
        assertEquals("=", template.operator1());
        assertEquals("-", template.operator2());
        assertEquals(2, template.constant());
    }
}
