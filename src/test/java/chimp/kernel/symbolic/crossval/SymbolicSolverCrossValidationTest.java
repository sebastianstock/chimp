package chimp.kernel.symbolic.crossval;

import static org.junit.Assert.*;

import chimp.kernel.Variable;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.symbolic.CompoundSymbolicSolver;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.symbolic.SymbolicConstraint;
import chimp.kernel.symbolic.SymbolicSolver;
import chimp.kernel.symbolic.SymbolicVariable;
import org.junit.Test;
import org.metacsp.framework.Constraint;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariableConstraintSolver;
import unify.NameMatchingConstraint;
import unify.NameMatchingConstraintSolver;
import unify.NameVariable;

/**
 * Cross-validation: CHIMP-native symbolic solvers vs Meta-CSP symbolic solvers.
 *
 * <p>Creates identical constraint networks in both solvers and asserts
 * that the resulting domains (possible symbols / ground values) match.
 */
public class SymbolicSolverCrossValidationTest {

    // ================================================================
    // Basic SymbolicSolver vs NameMatchingConstraintSolver
    // ================================================================

    @Test
    public void unaryEqualsMatchesMetaCsp() {
        String[] symbols = {"mug1", "mug2", "mug3"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(1);
        SymbolicVariable[] chimpVars = chimp.createVariables(1);

        NameMatchingConstraint mc = new NameMatchingConstraint(NameMatchingConstraint.Type.UNARYEQUALS);
        mc.setFrom(metaVars[0]);
        mc.setTo(metaVars[0]);
        mc.setUnaryValues(new int[]{1}); // "mug2"
        assertTrue(meta.addConstraint(mc));

        SymbolicConstraint cc = new SymbolicConstraint(SymbolicConstraint.Type.UNARYEQUALS);
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[0]});
        cc.setUnaryValues(new int[]{1});
        assertTrue(chimp.addConstraint(cc));

        assertDomainsMatch(metaVars, chimpVars);
    }

    @Test
    public void unaryDifferentMatchesMetaCsp() {
        String[] symbols = {"mug1", "mug2", "mug3"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(1);
        SymbolicVariable[] chimpVars = chimp.createVariables(1);

        NameMatchingConstraint mc = new NameMatchingConstraint(
                NameMatchingConstraint.Type.UNARYDIFFERENT);
        mc.setFrom(metaVars[0]);
        mc.setTo(metaVars[0]);
        mc.setUnaryValues(new int[]{1}); // NOT "mug2"
        assertTrue(meta.addConstraint(mc));

        SymbolicConstraint cc = new SymbolicConstraint(
                SymbolicConstraint.Type.UNARYDIFFERENT);
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[0]});
        cc.setUnaryValues(new int[]{1});
        assertTrue(chimp.addConstraint(cc));

        assertDomainsMatch(metaVars, chimpVars);
    }

    @Test
    public void equalsChainMatchesMetaCsp() {
        String[] symbols = {"mug1", "mug2", "mug3"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(3);
        SymbolicVariable[] chimpVars = chimp.createVariables(3);

        // vars[0] = "mug1"
        metaVars[0].setConstant("mug1");
        chimpVars[0].setConstant("mug1");

        // vars[0] == vars[1] == vars[2]
        NameMatchingConstraint mc01 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc01.setFrom(metaVars[0]); mc01.setTo(metaVars[1]);
        assertTrue(meta.addConstraint(mc01));

        SymbolicConstraint cc01 = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc01.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        assertTrue(chimp.addConstraint(cc01));

        NameMatchingConstraint mc12 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc12.setFrom(metaVars[1]); mc12.setTo(metaVars[2]);
        assertTrue(meta.addConstraint(mc12));

        SymbolicConstraint cc12 = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc12.setScope(new Variable[]{chimpVars[1], chimpVars[2]});
        assertTrue(chimp.addConstraint(cc12));

        assertDomainsMatch(metaVars, chimpVars);
    }

    @Test
    public void equalsContradictionMatchesMetaCsp() {
        String[] symbols = {"mug1", "mug2"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(2);
        SymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = "mug1", vars[1] = "mug2"
        metaVars[0].setConstant("mug1");
        metaVars[1].setConstant("mug2");
        chimpVars[0].setConstant("mug1");
        chimpVars[1].setConstant("mug2");

        // vars[0] == vars[1] — contradiction
        NameMatchingConstraint mc = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        boolean metaOk = meta.addConstraint(mc);

        SymbolicConstraint cc = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        boolean chimpOk = chimp.addConstraint(cc);

        assertEquals("Both should detect contradiction", metaOk, chimpOk);
        assertFalse("Should be false (contradiction)", metaOk);
    }

    @Test
    public void differentReducesDomain() {
        String[] symbols = {"mug1", "mug2", "mug3"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(2);
        SymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = "mug1"
        metaVars[0].setConstant("mug1");
        chimpVars[0].setConstant("mug1");

        // vars[1] != vars[0] → vars[1] ∈ {"mug2", "mug3"}
        NameMatchingConstraint mc = new NameMatchingConstraint(
                NameMatchingConstraint.Type.DIFFERENT);
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        assertTrue(meta.addConstraint(mc));

        SymbolicConstraint cc = new SymbolicConstraint(SymbolicConstraint.Type.DIFFERENT);
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        assertTrue(chimp.addConstraint(cc));

        assertDomainsMatch(metaVars, chimpVars);
    }

    @Test
    public void multipleConstraintsAndContradiction() {
        String[] symbols = {"mug1", "mug2", "mug3"};
        NameMatchingConstraintSolver meta = new NameMatchingConstraintSolver(symbols);
        SymbolicSolver chimp = new SymbolicSolver(symbols);

        NameVariable[] metaVars = (NameVariable[]) meta.createVariables(4);
        SymbolicVariable[] chimpVars = chimp.createVariables(4);

        // vars[0] = "mug1", vars[1] = "mug2"
        metaVars[0].setConstant("mug1");
        metaVars[1].setConstant("mug2");
        chimpVars[0].setConstant("mug1");
        chimpVars[1].setConstant("mug2");

        // vars[2] == vars[0] → vars[2] = "mug1"
        NameMatchingConstraint mc02 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc02.setFrom(metaVars[0]); mc02.setTo(metaVars[2]);
        assertTrue(meta.addConstraint(mc02));

        SymbolicConstraint cc02 = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc02.setScope(new Variable[]{chimpVars[0], chimpVars[2]});
        assertTrue(chimp.addConstraint(cc02));

        // vars[2] != vars[3] → vars[3] != "mug1"
        NameMatchingConstraint mc23 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.DIFFERENT);
        mc23.setFrom(metaVars[2]); mc23.setTo(metaVars[3]);
        assertTrue(meta.addConstraint(mc23));

        SymbolicConstraint cc23 = new SymbolicConstraint(SymbolicConstraint.Type.DIFFERENT);
        cc23.setScope(new Variable[]{chimpVars[2], chimpVars[3]});
        assertTrue(chimp.addConstraint(cc23));

        // vars[3] == vars[1] → contradiction with vars[3] != "mug1" above
        NameMatchingConstraint mc31 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc31.setFrom(metaVars[3]); mc31.setTo(metaVars[1]); // vars[3] == "mug2"
        assertTrue(meta.addConstraint(mc31));

        SymbolicConstraint cc31 = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc31.setScope(new Variable[]{chimpVars[3], chimpVars[1]});
        assertTrue(chimp.addConstraint(cc31));

        // Now try unifying vars[0] and vars[1] through vars[2] → vars[3] chain
        // vars[3] was constrained to be != "mug1" but also == "mug2" — OK
        // Now unify vars[3] with vars[0] (= "mug1") — should fail
        NameMatchingConstraint mc30 = new NameMatchingConstraint(
                NameMatchingConstraint.Type.EQUALS);
        mc30.setFrom(metaVars[3]); mc30.setTo(metaVars[0]);
        boolean metaOk = meta.addConstraint(mc30);

        SymbolicConstraint cc30 = new SymbolicConstraint(SymbolicConstraint.Type.EQUALS);
        cc30.setScope(new Variable[]{chimpVars[3], chimpVars[0]});
        boolean chimpOk = chimp.addConstraint(cc30);

        assertEquals("Both should detect contradiction", metaOk, chimpOk);
        assertFalse("Should be false", metaOk);
    }

    // ================================================================
    // CompoundSymbolicSolver vs CompoundSymbolicVariableConstraintSolver
    // ================================================================

    @Test
    public void compoundMatchesMatchesMetaCsp() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(2);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = On(a c)
        metaVars[0].setFullName("On(a c)");
        chimpVars[0].setFullName("On(a c)");
        // vars[1] unbound — MATCHES should unify

        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.MATCHES);
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        assertTrue(meta.addConstraint(mc));

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.MATCHES);
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        assertTrue(chimp.addConstraint(cc));

        assertEquals("Compound names should match",
                metaVars[1].getName(), chimpVars[1].getName());
    }

    @Test
    public void compoundSubMatchesMatchesMetaCsp() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(2);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = On(a c), vars[1] = Under(b d)
        metaVars[0].setFullName("On(a c)");
        metaVars[1].setFullName("Under(b d)");
        chimpVars[0].setFullName("On(a c)");
        chimpVars[1].setFullName("Under(b d)");

        // SUBMATCHES: arg0 == arg0 → "a" == "b" — contradiction
        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.SUBMATCHES, new int[]{0, 0});
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        boolean metaOk = meta.addConstraint(mc);

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBMATCHES, new int[]{0, 0});
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        boolean chimpOk = chimp.addConstraint(cc);

        assertEquals("Both should detect contradiction", metaOk, chimpOk);
        assertFalse("Should be false", metaOk);
    }

    @Test
    public void compoundSubMatchesSucceeds() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(2);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = On(a c), vars[1] = Under(a d) — both have "a" as arg0
        metaVars[0].setFullName("On(a c)");
        metaVars[1].setFullName("Under(a d)");
        chimpVars[0].setFullName("On(a c)");
        chimpVars[1].setFullName("Under(a d)");

        // SUBMATCHES: arg0 == arg0 → "a" == "a" — OK
        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.SUBMATCHES, new int[]{0, 0});
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        assertTrue(meta.addConstraint(mc));

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBMATCHES, new int[]{0, 0});
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        assertTrue(chimp.addConstraint(cc));

        // Names should still be ground
        assertEquals("On(a c)", metaVars[0].getName());
        assertEquals("Under(a d)", metaVars[1].getName());
        assertEquals(metaVars[0].getName(), chimpVars[0].getName());
        assertEquals(metaVars[1].getName(), chimpVars[1].getName());
    }

    @Test
    public void compoundSubDifferentMatchesMetaCsp() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(2);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(2);

        // vars[0] = On(a c), vars[1] = On(a d) — arg1 differs but arg0 same
        metaVars[0].setFullName("On(a c)");
        metaVars[1].setFullName("On(a d)");
        chimpVars[0].setFullName("On(a c)");
        chimpVars[1].setFullName("On(a d)");

        // SUBDIFFERENT: arg0 != arg0 of to → "a" != "a" — contradiction
        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.SUBDIFFERENT, new int[]{0, 0});
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[1]);
        boolean metaOk = meta.addConstraint(mc);

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBDIFFERENT, new int[]{0, 0});
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[1]});
        boolean chimpOk = chimp.addConstraint(cc);

        assertEquals("Both should detect contradiction", metaOk, chimpOk);
        assertFalse("Should be false", metaOk);
    }

    @Test
    public void compoundValueRestrictionMatchesMetaCsp() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(1);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(1);

        // vars[0] unbound — restrict arg0 to "a"
        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.POSITIVEVALUERESTRICTION,
                new int[]{0}, new String[][]{{"a"}});
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[0]);
        assertTrue(meta.addConstraint(mc));

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.POSITIVEVALUERESTRICTION,
                new int[]{0}, new String[][]{{"a"}});
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[0]});
        assertTrue(chimp.addConstraint(cc));

        // Both should have arg0 ground to "a"
        assertEquals("a",
                ((NameVariable) metaVars[0].getInternalVariables()[1])
                        .getPossibleSymbols()[0]);
        assertEquals("a",
                chimpVars[0].getInternalVariables()[1].possibleSymbols()[0]);
    }

    @Test
    public void compoundNegativeValueRestrictionMatchesMetaCsp() {
        String[][] symbols = {{"On", "Under"}, {"a", "b"}, {"c", "d"}};
        int[] ingredients = {1, 1, 1};

        CompoundSymbolicVariableConstraintSolver meta =
                new CompoundSymbolicVariableConstraintSolver(symbols, ingredients);
        CompoundSymbolicSolver chimp =
                new CompoundSymbolicSolver(symbols, ingredients);

        unify.CompoundSymbolicVariable[] metaVars =
                (unify.CompoundSymbolicVariable[]) meta.createVariables(1);
        CompoundSymbolicVariable[] chimpVars = chimp.createVariables(1);

        // vars[0] = On(a c) — restrict arg0 NOT to be "b" (OK, it's "a")
        metaVars[0].setFullName("On(a c)");
        chimpVars[0].setFullName("On(a c)");

        CompoundSymbolicValueConstraint mc = new CompoundSymbolicValueConstraint(
                CompoundSymbolicValueConstraint.Type.NEGATIVEVALUERESTRICTION,
                new int[]{0}, new String[][]{{"b"}});
        mc.setFrom(metaVars[0]); mc.setTo(metaVars[0]);
        assertTrue(meta.addConstraint(mc));

        CompoundSymbolicConstraint cc = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.NEGATIVEVALUERESTRICTION,
                new int[]{0}, new String[][]{{"b"}});
        cc.setScope(new Variable[]{chimpVars[0], chimpVars[0]});
        assertTrue(chimp.addConstraint(cc));

        // Names still ground
        assertEquals("On(a c)", metaVars[0].getName());
        assertEquals(metaVars[0].getName(), chimpVars[0].getName());
    }

    // ================================================================
    // Helpers
    // ================================================================

    /** Assert that Meta-CSP NameVariable and CHIMP SymbolicVariable domains match. */
    private void assertDomainsMatch(NameVariable[] metaVars, SymbolicVariable[] chimpVars) {
        assertEquals("Variable count mismatch", metaVars.length, chimpVars.length);
        for (int i = 0; i < metaVars.length; i++) {
            String[] metaSymbols = metaVars[i].getPossibleSymbols();
            String[] chimpSymbols = chimpVars[i].possibleSymbols();
            assertEquals("Variable[" + i + "] domain size mismatch",
                    metaSymbols.length, chimpSymbols.length);
            for (int j = 0; j < metaSymbols.length; j++) {
                assertEquals("Variable[" + i + "] symbol[" + j + "] mismatch",
                        metaSymbols[j], chimpSymbols[j]);
            }
        }
    }
}
