package chimp.kernel.integer.crossval;

import static org.junit.Assert.*;

import chimp.kernel.integer.IntegerConstraint;
import chimp.kernel.integer.IntegerSolver;
import chimp.kernel.integer.IntegerVariable;
import integers.IntegerDomain;
import org.junit.Test;

/**
 * Cross-validation: CHIMP-native IntegerSolver vs Meta-CSP
 * IntegerConstraintSolver.
 *
 * <p>Creates identical constraint networks in both solvers and
 * asserts that both consistently find solutions or both detect
 * inconsistency. Since both use Choco internally, domain values
 * may differ but consistency and constraint satisfaction must match.
 */
public class IntegerSolverCrossValidationTest {

    // --- helpers ---

    private static int valueOf(org.metacsp.framework.Variable v) {
        return ((IntegerDomain) ((integers.IntegerVariable) v).getDomain()).getValue();
    }

    private static boolean isInst(org.metacsp.framework.Variable v) {
        return ((integers.IntegerVariable) v).isInstantiated();
    }

    // ================================================================
    // Arithmetic constraints
    // ================================================================

    @Test
    public void arithmConstantPlusVarGeq() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(2);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(2);

        // vars[0] = 80
        ((integers.IntegerVariable) mv[0]).setConstantValue(80);
        cv[0].setConstantValue(80);

        // vars[1] + vars[0] >= 100
        boolean metaOk = meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[1], mv[0]}, "+", ">=", 100));
        boolean chimpOk = chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[1], cv[0]}, "+", ">=", 100));

        assertEquals("Both should succeed", metaOk, chimpOk);
        assertTrue(metaOk);
        assertTrue("vars[1] >= 20", valueOf(mv[1]) >= 20);
        assertTrue("vars[1] >= 20", cv[1].getDomain().getValue() >= 20);
    }

    @Test
    public void binaryArithmVarsSumGeq() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(2);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(2);

        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0], mv[1]}, "+", ">=", 100)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0], cv[1]}, "+", ">=", 100)));

        assertTrue(valueOf(mv[0]) + valueOf(mv[1]) >= 100);
        assertTrue(cv[0].getDomain().getValue() + cv[1].getDomain().getValue() >= 100);
    }

    @Test
    public void threeVarArithm() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(3);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(3);

        ((integers.IntegerVariable) mv[0]).setConstantValue(10);
        cv[0].setConstantValue(10);

        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0], mv[1], mv[2]}, "+", "=")));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0], cv[1], cv[2]}, "+", "=")));

        assertEquals(valueOf(mv[0]) + valueOf(mv[1]), valueOf(mv[2]));
        assertEquals(cv[0].getDomain().getValue() + cv[1].getDomain().getValue(),
                cv[2].getDomain().getValue());
    }

    @Test
    public void constraintChain() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(4);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(4);

        // vars[1] = 80
        ((integers.IntegerVariable) mv[1]).setConstantValue(80);
        cv[1].setConstantValue(80);

        // vars[0] + vars[1] >= 100
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0], mv[1]}, "+", ">=", 100)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0], cv[1]}, "+", ">=", 100)));

        // vars[0] = vars[1] + 105
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0], mv[1]}, "=", "+", 105)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0], cv[1]}, "=", "+", 105)));

        assertEquals(185, valueOf(mv[0]));
        assertEquals(185, cv[0].getDomain().getValue());

        // vars[1] + vars[2] < vars[3]
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[1], mv[2], mv[3]}, "+", "<")));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[1], cv[2], cv[3]}, "+", "<")));

        // All different
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ALLDIFFERENT, mv)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ALLDIFFERENT,
                new IntegerVariable[]{cv[0], cv[1], cv[2], cv[3]})));

        // Verify instantiation
        for (int i = 0; i < 4; i++) {
            assertTrue("Meta var " + i, isInst(mv[i]));
            assertTrue("CHIMP var " + i, cv[i].isInstantiated());
        }
    }

    @Test
    public void allDifferentThreeVars() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 100);
        org.metacsp.framework.Variable[] mv = meta.createVariables(3);

        IntegerSolver chimp = new IntegerSolver(0, 100);
        IntegerVariable[] cv = chimp.createVariables(3);

        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ALLDIFFERENT, mv)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ALLDIFFERENT, cv)));

        for (int i = 0; i < 3; i++) {
            assertTrue(isInst(mv[i]));
            assertTrue(cv[i].isInstantiated());
        }

        int v0 = valueOf(mv[0]), v1 = valueOf(mv[1]), v2 = valueOf(mv[2]);
        assertTrue("Meta distinct", v0 != v1 && v1 != v2 && v0 != v2);

        int c0 = cv[0].getDomain().getValue();
        int c1 = cv[1].getDomain().getValue();
        int c2 = cv[2].getDomain().getValue();
        assertTrue("CHIMP distinct", c0 != c1 && c1 != c2 && c0 != c2);
    }

    @Test
    public void allEqualThreeVars() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(3);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(3);

        ((integers.IntegerVariable) mv[0]).setConstantValue(42);
        cv[0].setConstantValue(42);

        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ALLEQUAL, mv)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ALLEQUAL,
                new IntegerVariable[]{cv[0], cv[1], cv[2]})));

        assertEquals(42, valueOf(mv[1]));
        assertEquals(42, valueOf(mv[2]));
        assertEquals(42, cv[1].getDomain().getValue());
        assertEquals(42, cv[2].getDomain().getValue());
    }

    @Test
    public void contradictionDetectedByBoth() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 100);
        org.metacsp.framework.Variable[] mv = meta.createVariables(1);

        IntegerSolver chimp = new IntegerSolver(0, 100);
        IntegerVariable[] cv = chimp.createVariables(1);

        ((integers.IntegerVariable) mv[0]).setConstantValue(50);
        cv[0].setConstantValue(50);

        boolean metaOk = meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0]}, "<", 0));
        boolean chimpOk = chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0]}, "<", 0));

        assertEquals("Both should detect contradiction", metaOk, chimpOk);
        assertFalse(metaOk);
    }

    @Test
    public void threeTermsArithmEq() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(3);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(3);

        ((integers.IntegerVariable) mv[0]).setConstantValue(100);
        cv[0].setConstantValue(100);
        ((integers.IntegerVariable) mv[1]).setConstantValue(0);
        cv[1].setConstantValue(0);

        // vars[0] = vars[1] + vars[2] → 100 = 0 + vars[2]
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[0], mv[1], mv[2]}, "=", "+")));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[0], cv[1], cv[2]}, "=", "+")));

        assertEquals(100, valueOf(mv[2]));
        assertEquals(100, cv[2].getDomain().getValue());
    }

    @Test
    public void arithmWithConstantOffset() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 1000);
        org.metacsp.framework.Variable[] mv = meta.createVariables(2);

        IntegerSolver chimp = new IntegerSolver(0, 1000);
        IntegerVariable[] cv = chimp.createVariables(2);

        ((integers.IntegerVariable) mv[0]).setConstantValue(10);
        cv[0].setConstantValue(10);

        // vars[1] = vars[0] + 5
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ARITHM,
                new org.metacsp.framework.Variable[]{mv[1], mv[0]}, "=", "+", 5)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{cv[1], cv[0]}, "=", "+", 5)));

        assertEquals(15, valueOf(mv[1]));
        assertEquals(15, cv[1].getDomain().getValue());
    }

    @Test
    public void contradictionBetweenAllDiffAndAllEqual() {
        integers.IntegerConstraintSolver meta = new integers.IntegerConstraintSolver(0, 100);
        org.metacsp.framework.Variable[] mv = meta.createVariables(2);

        IntegerSolver chimp = new IntegerSolver(0, 100);
        IntegerVariable[] cv = chimp.createVariables(2);

        ((integers.IntegerVariable) mv[0]).setConstantValue(1);
        ((integers.IntegerVariable) mv[1]).setConstantValue(2);
        cv[0].setConstantValue(1);
        cv[1].setConstantValue(2);

        // AllDifferent passes
        assertTrue(meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ALLDIFFERENT, mv)));
        assertTrue(chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ALLDIFFERENT,
                new IntegerVariable[]{cv[0], cv[1]})));

        // AllEqual should fail
        boolean metaOk = meta.addConstraint(new integers.IntegerConstraint(
                integers.IntegerConstraint.Type.ALLEQUAL, mv));
        boolean chimpOk = chimp.addConstraint(new IntegerConstraint(
                IntegerConstraint.Type.ALLEQUAL,
                new IntegerVariable[]{cv[0], cv[1]}));

        assertEquals(metaOk, chimpOk);
        assertFalse(metaOk);
    }
}
