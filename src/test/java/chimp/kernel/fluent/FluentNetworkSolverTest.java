package chimp.kernel.fluent;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chimp.kernel.Constraint;

/**
 * Mirror of {@code htn.JUnitTestFluentNetworkSolver} using the
 * CHIMP-native fluent types.
 */
public class FluentNetworkSolverTest {

    private FluentNetworkSolver solver;

    @Before
    public void setUp() {
        String[] symbolsPredicates = {"on", "robotat", "get_mug"};
        String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5",
                "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
        String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5",
                "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
        String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5",
                "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
        String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5",
                "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
        String[][] symbols = new String[5][];
        symbols[0] = symbolsPredicates;
        symbols[1] = symbolsMugs;
        symbols[2] = symbolsPlAreas;
        symbols[3] = symbolsManAreas;
        symbols[4] = symbolsPreAreas;
        solver = new FluentNetworkSolver(0, 500, symbols, new int[] {1, 1, 1, 1, 1});
    }

    @After
    public void tearDown() {
        solver = null;
    }

    @Test
    public void testGetOpenFluents() {
        Fluent[] fluents = solver.createVariables(5);
        fluents[0].setMarking(3); // OPEN
        fluents[1].setMarking(4); // CLOSED
        fluents[2].setMarking(0); // UNPLANNED
        fluents[3].setMarking(3); // OPEN
        fluents[4].setMarking(2); // PLANNED

        Fluent[] openFluents = solver.getOpenFluents();
        assertEquals(2, openFluents.length);
        assertSame(fluents[0], openFluents[0]);
        assertSame(fluents[3], openFluents[1]);
    }

    @Test
    public void testGetConstraintsTo() {
        Fluent[] fluents = solver.createVariables(4);
        fluents[0].setName("on(mug1 pl1 none none)");
        fluents[1].setName("on(?mug pl1 none none)");
        fluents[2].setName("on(mug1 pl2 none none)");

        FluentConstraint fcon1 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        fcon1.setScope(new chimp.kernel.Variable[] { fluents[0], fluents[1] });
        assertTrue(solver.addConstraint(fcon1));

        List<Constraint> res = solver.getConstraintsTo(fluents[1]);
        assertEquals(1, res.size());
        assertEquals(fcon1, res.get(0));

        // New constraint from fl1 → fl0
        FluentConstraint fcon2 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        fcon2.setScope(new chimp.kernel.Variable[] { fluents[1], fluents[0] });
        solver.addConstraint(fcon2);

        // getConstraintsTo(fl1) should still be only fcon1
        res = solver.getConstraintsTo(fluents[1]);
        assertEquals(1, res.size());
        assertTrue(res.contains(fcon1));
        assertFalse(res.contains(fcon2));

        // Add fcon3: fl3 → fl1
        FluentConstraint fcon3 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        fcon3.setScope(new chimp.kernel.Variable[] { fluents[3], fluents[1] });
        solver.addConstraint(fcon3);

        res = solver.getConstraintsTo(fluents[1]);
        assertEquals(2, res.size());
        assertTrue(res.contains(fcon1));
        assertTrue(res.contains(fcon3));
    }

    @Test
    public void testMatchUnifies() {
        Fluent[] fluents = solver.createVariables(3);
        fluents[0].setName("on(mug1 pl1 none none)");
        fluents[1].setName("on(?mug pl1 none none)");
        fluents[2].setName("on(mug1 pl2 none none)");

        FluentConstraint fcon01 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        fcon01.setScope(new chimp.kernel.Variable[] { fluents[0], fluents[1] });
        assertTrue("MATCHES should succeed", solver.addConstraint(fcon01));

        // Name of ?mug should unify to mug1
        assertEquals("on(mug1 pl1 none none)",
                fluents[1].getCompoundSymbolicVariable().getName());

        // Matching mug1 with pl2 should fail
        FluentConstraint fcon02 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        fcon02.setScope(new chimp.kernel.Variable[] { fluents[0], fluents[2] });
        assertFalse("MATCHES with different pl should fail", solver.addConstraint(fcon02));
    }
}
