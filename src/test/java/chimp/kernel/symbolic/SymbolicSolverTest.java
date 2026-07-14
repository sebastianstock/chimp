package chimp.kernel.symbolic;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for the CHIMP-native symbolic solver, mirroring
 * {@code unify.JUnitTestNameMatchingConstraintSolver} and
 * {@code unify.JunitTestCompoundNameMatchingConstraintSolver}.
 */
public class SymbolicSolverTest {

    // ================================================================
    // SymbolicVariable + SymbolicDomain
    // ================================================================

    @Test
    public void variableStartsWithAllSymbolsPossible() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable v = solver.createVariables(1)[0];
        assertEquals(3, v.possibleSymbols().length);
        assertFalse(v.isGround());
        assertTrue(v.hasModel());
    }

    @Test
    public void setConstantMakesGround() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"x", "y", "z"});
        SymbolicVariable v = solver.createVariables(1)[0];
        v.setConstant("y");
        assertTrue(v.isGround());
        assertEquals("y", v.possibleSymbols()[0]);
        assertEquals("y", v.toString());
    }

    @Test
    public void domainCopyAndReset() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable v = solver.createVariables(1)[0];
        // Restrict via constraint, not setConstant (which creates a permanent constant)
        SymbolicConstraint c = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        c.setScope(new chimp.kernel.Variable[]{v, v});
        c.setUnaryValues(new int[]{0}); // only "a"
        solver.addConstraint(c);
        assertEquals("a", v.possibleSymbols()[0]);
        // Backup, then reset (reset keeps the constraint-based restriction?)
        SymbolicDomain backup = new SymbolicDomain(v.domain());
        v.domain().reset();
        // After reset, all 3 symbols are possible again (no permanent constant)
        assertEquals(3, v.possibleSymbols().length);
        v.domain().copyFrom(backup);
        assertEquals("a", v.possibleSymbols()[0]);
    }

    @Test
    public void toStringShort() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable v = solver.createVariables(1)[0];
        v.setConstant("a");
        assertEquals("a", v.toStringShort());
        SymbolicVariable u = solver.createVariables(1)[0];
        assertEquals("{...}", u.toStringShort());
    }

    // ================================================================
    // SymbolicConstraint
    // ================================================================

    @Test
    public void equalsConstraintClones() {
        SymbolicConstraint c = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b"});
        SymbolicVariable[] vars = solver.createVariables(2);
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        SymbolicConstraint clone = c.clone();
        assertEquals(c.type(), clone.type());
    }

    @Test
    public void unaryConstraintCarriesValues() {
        SymbolicConstraint c = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        c.setUnaryValues(new int[]{2, 0, 1});
        assertArrayEquals(new int[]{0, 1, 2}, c.getUnaryValues());
    }

    // ================================================================
    // SymbolicSolver propagation
    // ================================================================

    @Test
    public void singleVarUnaryEqualsPropagates() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable[] vars = solver.createVariables(1);
        SymbolicConstraint c = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        c.setUnaryValues(new int[]{0, 2}); // a or c
        assertTrue(solver.addConstraint(c));
        assertEquals(2, vars[0].possibleSymbols().length); // a, c (not b)
    }

    @Test
    public void singleVarUnaryDifferentPropagates() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable[] vars = solver.createVariables(1);
        SymbolicConstraint c = new SymbolicConstraint(SymbolicConstraint.Type.UNARYDIFFERENT);
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        c.setUnaryValues(new int[]{1}); // not b
        assertTrue(solver.addConstraint(c));
        assertEquals(2, vars[0].possibleSymbols().length); // a, c
    }

    @Test
    public void equalsPropagates() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable[] vars = solver.createVariables(2);
        // Constrain first variable to "a"
        SymbolicConstraint u = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        u.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        u.setUnaryValues(new int[]{0});
        assertTrue(solver.addConstraint(u));
        // Equals between vars[0] and vars[1]
        SymbolicConstraint eq = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        eq.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        assertTrue(solver.addConstraint(eq));
        assertEquals("a", vars[1].possibleSymbols()[0]);
        assertTrue(vars[1].isGround());
    }

    @Test
    public void differentPropagates() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b", "c"});
        SymbolicVariable[] vars = solver.createVariables(2);
        // vars[0] = "a"
        SymbolicConstraint u = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        u.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        u.setUnaryValues(new int[]{0});
        assertTrue(solver.addConstraint(u));
        // vars[1] != vars[0]
        SymbolicConstraint ne = new SymbolicConstraint(SymbolicConstraint.Type.DIFFERENT);
        ne.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        assertTrue(solver.addConstraint(ne));
        String[] possible = vars[1].possibleSymbols();
        assertEquals(2, possible.length); // b, c (not a)
    }

    @Test
    public void contradictionDetected() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b"});
        SymbolicVariable[] vars = solver.createVariables(1);
        // Set to "a" first
        SymbolicConstraint ok = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        ok.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        ok.setUnaryValues(new int[]{0}); // "a"
        assertTrue(solver.addConstraint(ok));
        assertEquals("a", vars[0].possibleSymbols()[0]);
        // Now contradict: cannot be "a"
        SymbolicConstraint bad = new SymbolicConstraint(SymbolicConstraint.Type.UNARYDIFFERENT);
        bad.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        bad.setUnaryValues(new int[]{0}); // not "a" — contradicts "a"
        assertFalse(solver.addConstraint(bad));
        assertTrue(vars[0].hasModel()); // domain restored
        assertEquals("a", vars[0].possibleSymbols()[0]);
    }

    @Test
    public void backupRestoredOnFailure() {
        SymbolicSolver solver = new SymbolicSolver(new String[]{"a", "b"});
        SymbolicVariable[] vars = solver.createVariables(1);
        // Set up a valid state first
        SymbolicConstraint ok = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        ok.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        ok.setUnaryValues(new int[]{0}); // "a"
        assertTrue(solver.addConstraint(ok));
        assertEquals(1, vars[0].possibleSymbols().length);
        // Now add a contradiction
        SymbolicConstraint bad = new SymbolicConstraint(SymbolicConstraint.Type.UNARYDIFFERENT);
        bad.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        bad.setUnaryValues(new int[]{0}); // not "a" — contradicts
        assertFalse(solver.addConstraint(bad));
        // Domain should be restored to "a"
        assertEquals(1, vars[0].possibleSymbols().length);
        assertEquals("a", vars[0].possibleSymbols()[0]);
    }

    // ================================================================
    // CompoundSymbolicVariable
    // ================================================================

    @Test
    public void compoundVariableSetAndGetName() {
        String[][] symbols = {{"On"}, {"mug1", "cup1"}, {"table1", "table2"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(1);
        vars[0].setName("On", "mug1", "table1");
        assertEquals("On(mug1 table1)", vars[0].getName());
        assertEquals("On(mug1 table1)", vars[0].toString());
    }

    @Test
    public void compoundVariableSetFullName() {
        String[][] symbols = {{"At"}, {"r1", "r2"}, {"w1", "w2"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(1);
        vars[0].setFullName("At(r1 w1)");
        assertEquals("r1", vars[0].getGroundArgs()[0]);
        assertEquals("w1", vars[0].getGroundArgs()[1]);
    }

    @Test
    public void compoundVariablePossibleMatch() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(1);
        assertTrue(vars[0].possibleMatch("On", new String[]{"a", "x"}));
        assertFalse(vars[0].possibleMatch("Beside", new String[]{"a", "x"}));
    }

    // ================================================================
    // CompoundSymbolicConstraint + CompoundSymbolicSolver
    // ================================================================

    @Test
    public void matchesConstraintPropagates() {
        String[][] symbols = {{"On"}, {"a", "b"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(2);

        vars[0].setName("On", "a", "x");
        // vars[1] is unbound — should unify with vars[0]

        CompoundSymbolicConstraint c = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.MATCHES);
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        assertTrue(solver.addConstraint(c));

        assertEquals("On(a x)", vars[1].getName());
        assertTrue(vars[1].getInternalVariables()[1].isGround()); // "a"
        assertTrue(vars[1].getInternalVariables()[2].isGround()); // "x"
    }

    @Test
    public void subMatchesPropagates() {
        // Connection {0, 0}: from.arg0 must equal to.arg0
        String[][] symbols = {{"On"}, {"a", "b"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(2);

        vars[0].setName("On", "a", "x");
        // vars[1] unbound — submatches on arg0 with unground var should bind it
        CompoundSymbolicConstraint c = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBMATCHES, new int[]{0, 0});
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        assertTrue(solver.addConstraint(c));
        assertEquals("a", vars[1].getInternalVariables()[1].possibleSymbols()[0]);
    }

    @Test
    public void valueRestrictionPropagates() {
        String[][] symbols = {{"On"}, {"a", "b"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(1);

        // vars[0] unbound; restrict arg0 to "a"
        CompoundSymbolicConstraint c = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.POSITIVEVALUERESTRICTION,
                new int[]{0}, new String[][]{{"a"}});
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[0]});
        assertTrue(solver.addConstraint(c));
        assertEquals("a", vars[0].getInternalVariables()[1].possibleSymbols()[0]);
    }

    @Test
    public void subDifferentPropagates() {
        String[][] symbols = {{"On"}, {"a", "b"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        CompoundSymbolicVariable[] vars = solver.createVariables(2);

        vars[0].setName("On", "a", "x");
        vars[1].setName("On", "b", "y");
        // SUBDIFFERENT: arg0 must differ
        CompoundSymbolicConstraint c = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBDIFFERENT, new int[]{0, 0});
        c.setScope(new chimp.kernel.Variable[]{vars[0], vars[1]});
        assertTrue(solver.addConstraint(c));
        // vars[1].arg0 should still be "b" (not "a" — they're already different)
        assertEquals("b", vars[1].getInternalVariables()[1].possibleSymbols()[0]);
    }

    @Test
    public void compoundSolverPropagateAllSub() {
        String[][] symbols = {{"On"}, {"a"}, {"x", "y"}};
        int[] ingredients = {1, 1, 1};
        CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);
        solver.createVariables(2);
        solver.propagateAllSub();
        assertTrue(true); // No exception
    }
}
