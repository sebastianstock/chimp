package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.metacsp.framework.Variable;

import chimp.model.IntegerConstraintKind;
import htn.IntegerConstraintTemplate;
import integers.IntegerConstraint;
import integers.IntegerConstraintSolver;

public class MetaCspIntegerConstraintAdapterTest {

    @Test
    public void compilesNeutralArithmeticTemplate() {
        IntegerConstraintSolver solver = new IntegerConstraintSolver(0, 100);
        Variable[] scope = solver.createVariables(2);
        IntegerConstraintTemplate template = new IntegerConstraintTemplate(
                IntegerConstraintKind.ARITHMETIC,
                new String[] {"?start", "?end"},
                "=",
                "-",
                2);

        IntegerConstraint constraint = new MetaCspIntegerConstraintAdapter()
                .toIntegerConstraint(template, scope);

        assertEquals(IntegerConstraint.Type.ARITHM, constraint.getType());
        assertEquals("=", constraint.getOp1());
        assertEquals("-", constraint.getOp2());
        assertEquals(2, constraint.getCste());
        assertEquals(scope[0], constraint.getScope()[0]);
        assertEquals(scope[1], constraint.getScope()[1]);
    }

    @Test
    public void mapsEveryNeutralConstraintKind() {
        IntegerConstraintSolver solver = new IntegerConstraintSolver(0, 100);
        Variable[] scope = solver.createVariables(2);
        MetaCspIntegerConstraintAdapter adapter = new MetaCspIntegerConstraintAdapter();

        assertEquals(IntegerConstraint.Type.ARITHM,
                adapter.toIntegerConstraint(template(IntegerConstraintKind.ARITHMETIC), scope).getType());
        assertEquals(IntegerConstraint.Type.ALLDIFFERENT,
                adapter.toIntegerConstraint(template(IntegerConstraintKind.ALL_DIFFERENT), scope).getType());
        assertEquals(IntegerConstraint.Type.ALLEQUAL,
                adapter.toIntegerConstraint(template(IntegerConstraintKind.ALL_EQUAL), scope).getType());
    }

    private IntegerConstraintTemplate template(IntegerConstraintKind kind) {
        return new IntegerConstraintTemplate(kind, new String[] {"?a", "?b"}, null, null, 0);
    }
}
