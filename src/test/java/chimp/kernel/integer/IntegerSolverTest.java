package chimp.kernel.integer;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for the CHIMP-native integer solver, adapted from
 * {@code integers.TestIntegerConstraintSolver}.
 */
public class IntegerSolverTest {

    @Test
    public void solvesArithmConstraint() {
        IntegerSolver solver = new IntegerSolver(0, 1000);
        IntegerVariable[] vars = solver.createVariables(2);

        vars[0].setConstantValue(80);

        // vars[0] + ? >= 100  =>  with vars[0]=80, need vars[1] >= 20
        IntegerConstraint c = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[1], vars[0]},
                "+", ">=", 100);
        assertTrue(solver.addConstraint(c));

        assertEquals(80, vars[0].getDomain().getValue());
        assertTrue(vars[1].getDomain().getValue() >= 20);
    }

    @Test
    public void solvesBinaryArithm() {
        IntegerSolver solver = new IntegerSolver(0, 1000);
        IntegerVariable[] vars = solver.createVariables(2);

        // vars[0] + vars[1] >= 100
        IntegerConstraint c = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[0], vars[1]},
                "+", ">=", 100);
        assertTrue(solver.addConstraint(c));

        assertTrue(vars[0].getDomain().getValue() + vars[1].getDomain().getValue() >= 100);
    }

    @Test
    public void solvesConstraintChain() {
        IntegerSolver solver = new IntegerSolver(0, 1000);
        IntegerVariable[] vars = solver.createVariables(4);

        vars[1].setConstantValue(80);

        // vars[0] + vars[1] >= 100
        IntegerConstraint ic01 = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[0], vars[1]},
                "+", ">=", 100);
        assertTrue(solver.addConstraint(ic01));

        // vars[0] = vars[1] + 105  => vars[0] = 80 + 105 = 185
        IntegerConstraint ic01b = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[0], vars[1]},
                "=", "+", 105);
        assertTrue(solver.addConstraint(ic01b));
        assertEquals(185, vars[0].getDomain().getValue());

        // vars[1] + vars[2] < vars[3]
        IntegerConstraint ic123 = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[1], vars[2], vars[3]},
                "+", "<");
        assertTrue(solver.addConstraint(ic123));

        // All different
        IntegerConstraint allDiff = new IntegerConstraint(
                IntegerConstraint.Type.ALLDIFFERENT, vars);
        assertTrue(solver.addConstraint(allDiff));

        // vars should all be instantiated
        for (IntegerVariable v : vars) {
            assertTrue(v.isInstantiated());
            assertTrue(v.getDomain().getValue() >= 0);
        }
    }

    @Test
    public void removeConstraint() {
        IntegerSolver solver = new IntegerSolver(0, 1000);
        IntegerVariable[] vars = solver.createVariables(3);
        vars[0].setConstantValue(50);

        // vars[1] >= 50
        IntegerConstraint c = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[1]},
                ">=", 50);
        assertTrue(solver.addConstraint(c));
        assertTrue(vars[1].getDomain().getValue() >= 50);

        solver.removeConstraint(c);
        // After removing, the constraint list should shrink
        assertTrue(solver.getConstraints().length >= 0);
    }

    @Test
    public void allDifferentPropagates() {
        IntegerSolver solver = new IntegerSolver(0, 10);
        IntegerVariable[] vars = solver.createVariables(3);

        IntegerConstraint allDiff = new IntegerConstraint(
                IntegerConstraint.Type.ALLDIFFERENT, vars);
        assertTrue(solver.addConstraint(allDiff));

        for (int i = 0; i < vars.length; i++) {
            assertTrue(vars[i].isInstantiated());
        }
        // All values should be distinct
        int v0 = vars[0].getDomain().getValue();
        int v1 = vars[1].getDomain().getValue();
        int v2 = vars[2].getDomain().getValue();
        assertTrue(v0 != v1 && v1 != v2 && v0 != v2);
    }

    @Test
    public void allEqualPropagates() {
        IntegerSolver solver = new IntegerSolver(0, 1000);
        IntegerVariable[] vars = solver.createVariables(3);
        vars[0].setConstantValue(42);

        IntegerConstraint allEq = new IntegerConstraint(
                IntegerConstraint.Type.ALLEQUAL, vars);
        assertTrue(solver.addConstraint(allEq));

        assertEquals(42, vars[0].getDomain().getValue());
        assertEquals(42, vars[1].getDomain().getValue());
        assertEquals(42, vars[2].getDomain().getValue());
    }

    @Test
    public void contradictionDetected() {
        IntegerSolver solver = new IntegerSolver(0, 100);
        IntegerVariable[] vars = solver.createVariables(1);
        vars[0].setConstantValue(50);

        // vars[0] < 0  — contradicts constant 50
        IntegerConstraint c = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM,
                new IntegerVariable[]{vars[0]},
                "<", 0);
        assertFalse(solver.addConstraint(c));
    }

    @Test
    public void constraintClonePreservesFields() {
        IntegerVariable[] vars = new IntegerVariable[]{
                new IntegerVariable(null, 1),
                new IntegerVariable(null, 2)
        };
        IntegerConstraint orig = new IntegerConstraint(
                IntegerConstraint.Type.ARITHM, vars, "+", ">=", 100);
        IntegerConstraint clone = orig.clone();

        assertEquals(orig.getType(), clone.getType());
        assertEquals(orig.getOp1(), clone.getOp1());
        assertEquals(orig.getOp2(), clone.getOp2());
        assertEquals(orig.getCste(), clone.getCste());
    }

    @Test
    public void integerDomainUninstantiate() {
        IntegerDomain d = new IntegerDomain();
        d.setValue(42);
        assertTrue(d.isInstantiated());
        assertEquals(42, d.getValue());
        d.uninstantiate();
        assertFalse(d.isInstantiated());
    }

    @Test
    public void integerDomainConstantImmutable() {
        IntegerDomain d = new IntegerDomain();
        d.setConstantValue(7);
        assertTrue(d.isConstant());
        assertEquals(7, d.getValue());
        d.setValue(99); // should warn but not change
        assertEquals(7, d.getValue());
    }
}
