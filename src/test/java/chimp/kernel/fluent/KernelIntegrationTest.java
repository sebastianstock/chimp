package chimp.kernel.fluent;

import static org.junit.Assert.*;

import java.util.List;

import chimp.kernel.Constraint;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.TemporalBounds;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for the CHIMP-native kernel, exercising the
 * FluentNetworkSolver with realistic planning-like workloads.
 *
 * <p>These tests verify that the kernel's sub-solvers (temporal,
 * symbolic, integer) interact correctly through FluentConstraints
 * and that the overall API supports the patterns used by the planner.
 */
public class KernelIntegrationTest {

    private FluentNetworkSolver solver;

    @Before
    public void setUp() {
        // Symbol table for a simple DWR-like domain (5 positions)
        // Position 0: predicate names
        // Position 1: first arg (e.g., mug or location)
        // Position 2: second arg (e.g., location)
        // Positions 3-4: padding for "none"
        String[] preds = {"on", "robotat", "holding", "empty", "pick", "move", "place"};
        String[] arg0 = {"mug1", "mug2", "loc1", "loc2", "none"};
        String[] arg1 = {"mug1", "mug2", "loc1", "loc2", "none"};
        String[] arg2 = {"nil", "none"};
        String[] arg3 = {"nil", "none"};
        String[][] symbols = {preds, arg0, arg1, arg2, arg3};
        int[] ingredients = {1, 1, 1, 1, 1};

        solver = new FluentNetworkSolver(0, 100_000, symbols, ingredients,
                /* minInt */ 0, /* maxInt */ 1000, /* numIntVars */ 2);
    }

    // ================================================================
    // Basic fluent lifecycle
    // ================================================================

    @Test
    public void createFluentsAndQueryBounds() {
        Fluent[] fluents = solver.createVariables(3);

        // Set initial state fluents
        fluents[0].setName("on(mug1 loc1 none none)");
        fluents[1].setName("robotat(loc1 none none none)");
        fluents[2].setName("empty(none none none none)");

        // After creation, all fluents should have valid temporal bounds
        for (Fluent f : fluents) {
            TemporalVariable tv = f.getTemporalVariable();
            assertTrue("EST >= 0", tv.getEST() >= 0);
            assertTrue("LST <= horizon", tv.getLST() <= solver.getHorizon());
            assertTrue("EST <= LST", tv.getEST() <= tv.getLST());
            assertTrue("EET >= EST", tv.getEET() >= tv.getEST());
            assertTrue("LET >= EET", tv.getLET() >= tv.getEET());
        }
    }

    // ================================================================
    // MATCHES constraint (unification, like planning step effect)
    // ================================================================

    @Test
    public void matchesUnifiesVariables() {
        Fluent[] fluents = solver.createVariables(4);

        // Ground initial state
        fluents[0].setName("on(mug1 loc1 none none)");
        // Goal pattern (partially unbound — same arg1 to allow matching)
        fluents[1].setName("on(?mug loc1 none none)");

        // MATCHES: goal must match a state fluent
        FluentConstraint match = new FluentConstraint(FluentConstraint.Type.MATCHES);
        match.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertTrue("MATCHES should succeed", solver.addConstraint(match));

        // The goal pattern's mug variable should be unified to "mug1"
        assertEquals("on(mug1 loc1 none none)",
                fluents[1].getCompoundSymbolicVariable().getName());

        // The two fluents should be temporally equal after MATCHES
        TemporalVariable tv0 = fluents[0].getTemporalVariable();
        TemporalVariable tv1 = fluents[1].getTemporalVariable();
        assertEquals("EST should match", tv0.getEST(), tv1.getEST());
        assertEquals("EET should match", tv0.getEET(), tv1.getEET());
    }

    @Test
    public void matchesDetectsSymbolicConflict() {
        Fluent[] fluents = solver.createVariables(2);

        fluents[0].setName("on(mug1 loc1 none none)");
        fluents[1].setName("on(mug2 loc1 none none)");

        // MATCHES between two ground fluents with different mugs → conflict
        FluentConstraint match = new FluentConstraint(FluentConstraint.Type.MATCHES);
        match.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertFalse("MATCHES with conflicting ground values should fail",
                solver.addConstraint(match));

        // Domains should be unchanged after failure
        assertEquals("on(mug1 loc1 none none)",
                fluents[0].getCompoundSymbolicVariable().getName());
        assertEquals("on(mug2 loc1 none none)",
                fluents[1].getCompoundSymbolicVariable().getName());
    }

    // ================================================================
    // DC constraint (causal link — task protects a fluent)
    // ================================================================

    @Test
    public void dcEnsuresTemporalContainment() {
        Fluent[] fluents = solver.createVariables(3);

        // A task fluent (the plan step)
        fluents[0].setName("on(mug1 loc2 none none)");
        // The protected fluent (effect of a previous step)
        fluents[1].setName("on(mug1 loc2 none none)");
        // A threatening task
        fluents[2].setName("on(mug1 loc1 none none)");

        // DC: fluents[1] must contain fluents[0] temporally
        // (task must be within the protected fluent's interval)
        FluentConstraint dc = new FluentConstraint(FluentConstraint.Type.DC);
        dc.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertTrue("DC constraint should succeed", solver.addConstraint(dc));

        // After DC, the task (fluents[0]) should be temporally within fluents[1]
        TemporalVariable task = fluents[0].getTemporalVariable();
        TemporalVariable prot = fluents[1].getTemporalVariable();
        assertTrue("Task EST >= Protected EST", task.getEST() >= prot.getEST());
        assertTrue("Task LET <= Protected LET", task.getLET() <= prot.getLET());
    }

    // ================================================================
    // PRE + OPENS + CLOSES (task decomposition)
    // ================================================================

    @Test
    public void preOpensClosesOrdering() {
        Fluent[] fluents = solver.createVariables(4);

        // Parent task
        fluents[0].setName("on(mug1 loc2 none none)");
        // Precondition (must hold before or at start of task)
        fluents[1].setName("robotat(loc1 none none none)");
        // Effect (opens at task start)
        fluents[2].setName("holding(mug1 none none none)");
        // Delete effect (closes at task end)
        fluents[3].setName("empty(none none none none)");

        // PRE: precondition must precede or meet task
        FluentConstraint pre = new FluentConstraint(FluentConstraint.Type.PRE);
        pre.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertTrue("PRE should succeed", solver.addConstraint(pre));

        // OPENS: effect starts when task starts
        FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS);
        opens.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[2]});
        assertTrue("OPENS should succeed", solver.addConstraint(opens));

        // CLOSES: delete effect ends when task ends
        FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
        closes.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[3]});
        assertTrue("CLOSES should succeed", solver.addConstraint(closes));

        // Verify temporal consistency: all fluents have valid bounds
        TemporalVariable task = fluents[0].getTemporalVariable();
        TemporalVariable preTv = fluents[1].getTemporalVariable();
        TemporalVariable effTv = fluents[2].getTemporalVariable();
        TemporalVariable delTv = fluents[3].getTemporalVariable();

        // All time points should be within [0, horizon]
        for (TemporalVariable tv : new TemporalVariable[]{task, preTv, effTv, delTv}) {
            assertTrue("EST >= 0: " + tv.getEST(), tv.getEST() >= 0);
            assertTrue("LET <= horizon: " + tv.getLET(), tv.getLET() <= solver.getHorizon());
            assertTrue("EST <= LST", tv.getEST() <= tv.getLST());
        }

        // PRE: task must overlap precondition (MeetsOrOverlapsOrFinishedByOrContains)
        // At minimum they must not be disjoint — verified by network consistency

        // OPENS: effect starts after task starts (Overlaps)
        assertTrue("OPENS: effect EET >= task EST", effTv.getEET() >= task.getEST());

        // CLOSES: delete ends before task ends (OverlappedBy)
        assertTrue("CLOSES: delete EST <= task LET", delTv.getEST() <= task.getLET());
    }

    // ================================================================
    // MOVEDURATION constraint
    // ================================================================

    @Test
    public void moveDurationConstrainsTaskLength() {
        Fluent[] fluents = solver.createVariables(1);
        fluents[0].setName("on(mug1 loc2 none none)");

        // Constrain the task duration to [10, 50]
        FluentConstraint dur = new FluentConstraint(FluentConstraint.Type.MOVEDURATION,
                new TemporalBounds(10, 50));
        dur.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[0]});
        assertTrue("MOVEDURATION should succeed", solver.addConstraint(dur));

        TemporalVariable tv = fluents[0].getTemporalVariable();
        long duration = tv.getEET() - tv.getEST();
        assertTrue("Duration >= 10: " + duration, duration >= 10);
        assertTrue("Duration <= 50: " + duration, duration <= 50);
    }

    // ================================================================
    // Full planning-like scenario: multi-step with mixed constraints
    // ================================================================

    @Test
    public void smallDwrScenario() {
        // Simulate a simple DWR problem:
        //   Initial: robotat(loc1), on(mug1 loc1), empty
        //   Goal: on(mug1 loc2)
        //
        // Steps:
        //   pick-mug1-at-loc1: PRE robotat(loc1), PRE on(mug1 loc1),
        //                      OPENS holding(mug1), CLOSES empty
        //   move-to-loc2: PRE holding(mug1),
        //                 OPENS robotat(loc2), CLOSES robotat(loc1)
        //   place-mug1-at-loc2: PRE robotat(loc2), PRE holding(mug1),
        //                       OPENS on(mug1 loc2), CLOSES holding(mug1)

        Fluent[] fluents = solver.createVariables(12);

        // --- State fluents ---
        Fluent robotLoc1 = fluents[0];
        Fluent robotLoc2 = fluents[1];
        Fluent mugAtLoc1 = fluents[2];
        Fluent mugAtLoc2 = fluents[3];
        Fluent empty = fluents[4];
        Fluent holding = fluents[5];

        robotLoc1.setName("robotat(loc1 none none none)");
        robotLoc2.setName("robotat(loc2 none none none)");
        mugAtLoc1.setName("on(mug1 loc1 none none)");
        mugAtLoc2.setName("on(mug1 loc2 none none)");
        empty.setName("empty(none none none none)");
        holding.setName("holding(mug1 none none none)");

        // --- Task fluents ---
        Fluent pick = fluents[6];
        Fluent move = fluents[7];
        Fluent place = fluents[8];
        pick.setName("pick(mug1 loc1 none none)");
        move.setName("move(loc1 loc2 none none)");
        place.setName("place(mug1 loc2 none none)");

        // --- OPEN state fluents (goals) ---
        mugAtLoc2.setMarking(3); // OPEN

        // --- PICK constraints ---
        // PRE: robotat(loc1) before pick
        FluentConstraint pickPreRobot = new FluentConstraint(FluentConstraint.Type.PRE);
        pickPreRobot.setScope(new chimp.kernel.Variable[]{pick, robotLoc1});
        assertTrue(solver.addConstraint(pickPreRobot));

        // PRE: on(mug1 loc1) before pick
        FluentConstraint pickPreMug = new FluentConstraint(FluentConstraint.Type.PRE);
        pickPreMug.setScope(new chimp.kernel.Variable[]{pick, mugAtLoc1});
        assertTrue(solver.addConstraint(pickPreMug));

        // OPENS: holding(mug1)
        FluentConstraint pickOpens = new FluentConstraint(FluentConstraint.Type.OPENS);
        pickOpens.setScope(new chimp.kernel.Variable[]{pick, holding});
        assertTrue(solver.addConstraint(pickOpens));

        // CLOSES: empty
        FluentConstraint pickCloses = new FluentConstraint(FluentConstraint.Type.CLOSES);
        pickCloses.setScope(new chimp.kernel.Variable[]{pick, empty});
        assertTrue(solver.addConstraint(pickCloses));

        // --- MOVE constraints ---
        FluentConstraint movePreHold = new FluentConstraint(FluentConstraint.Type.PRE);
        movePreHold.setScope(new chimp.kernel.Variable[]{move, holding});
        assertTrue(solver.addConstraint(movePreHold));

        FluentConstraint moveOpens = new FluentConstraint(FluentConstraint.Type.OPENS);
        moveOpens.setScope(new chimp.kernel.Variable[]{move, robotLoc2});
        assertTrue(solver.addConstraint(moveOpens));

        FluentConstraint moveCloses = new FluentConstraint(FluentConstraint.Type.CLOSES);
        moveCloses.setScope(new chimp.kernel.Variable[]{move, robotLoc1});
        assertTrue(solver.addConstraint(moveCloses));

        // --- PLACE constraints ---
        // place needs robotat(loc2) as precondition
        FluentConstraint placePreRobot = new FluentConstraint(FluentConstraint.Type.PRE);
        placePreRobot.setScope(new chimp.kernel.Variable[]{place, robotLoc2});
        assertTrue(solver.addConstraint(placePreRobot));

        // place opens on(mug1 loc2) (add effect)
        FluentConstraint placeOpens = new FluentConstraint(FluentConstraint.Type.OPENS);
        placeOpens.setScope(new chimp.kernel.Variable[]{place, mugAtLoc2});
        assertTrue(solver.addConstraint(placeOpens));

        // place closes holding(mug1) (delete effect)
        // NOTE: holding is NOT a PRE of place — it's opened by pick,
        // needed by move (as PRE), and closed by place.  Adding both
        // PRE and CLOSES on the same fluent creates incompatible
        // Allen relations (MeetsOrOverlapsOrFinishedByOrContains vs
        // OverlappedBy).
        FluentConstraint placeCloses = new FluentConstraint(FluentConstraint.Type.CLOSES);
        placeCloses.setScope(new chimp.kernel.Variable[]{place, holding});
        assertTrue(solver.addConstraint(placeCloses));

        // --- Tasks are temporally ordered through shared fluents ---
        // (No explicit ordering needed — the shared fluents holding/robotat
        //  implicitly order pick→move→place)

        // --- Verify network is consistent ---
        Fluent[] openFluents = solver.getOpenFluents();
        // mugAtLoc2 should be the open goal
        assertTrue("mugAtLoc2 should be open",
                List.of(openFluents).contains(mugAtLoc2));

        // All task temporal bounds should be valid
        for (int i = 6; i <= 8; i++) {
            TemporalVariable tv = fluents[i].getTemporalVariable();
            assertTrue("Task " + i + " EST >= 0: " + tv.getEST(),
                    tv.getEST() >= 0);
            assertTrue("Task " + i + " EST <= LST: " + tv.getEST() + " <= " + tv.getLST(),
                    tv.getEST() <= tv.getLST());
        }
    }

    // ================================================================
    // Integer variable integration
    // ================================================================

    @Test
    public void integerVariablesPreservedThroughFluentCreation() {
        // Solver with integer variables
        assertTrue("Should have integer solver", solver.hasIntegerConstraintSolver());
        assertEquals(2, solver.getNumIntVars());

        Fluent[] fluents = solver.createVariables(2);
        for (Fluent f : fluents) {
            assertEquals("Each fluent should have 2 integer vars",
                    2, f.getIntegerVariables().length);
            for (chimp.kernel.integer.IntegerVariable iv : f.getIntegerVariables()) {
                assertNotNull("Integer variable should exist", iv);
            }
        }
    }

    // ================================================================
    // Constraint query APIs
    // ================================================================

    @Test
    public void getFluentConstraintsOfType() {
        Fluent[] fluents = solver.createVariables(4);
        fluents[0].setName("on(mug1 loc1 none none)");
        fluents[1].setName("on(?mug loc1 none none)");
        fluents[2].setName("holding(mug1 none none none)");

        // MATCHES: fl0 → fl1
        FluentConstraint m1 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m1.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        solver.addConstraint(m1);

        // OPENS: fl2 → fl1
        FluentConstraint o1 = new FluentConstraint(FluentConstraint.Type.OPENS);
        o1.setScope(new chimp.kernel.Variable[]{fluents[2], fluents[1]});
        solver.addConstraint(o1);

        // PRE: fl3 → fl1
        FluentConstraint p1 = new FluentConstraint(FluentConstraint.Type.PRE);
        p1.setScope(new chimp.kernel.Variable[]{fluents[3], fluents[1]});
        solver.addConstraint(p1);

        // Query constraints TO fl1 of type MATCHES
        List<FluentConstraint> matches = solver.getFluentConstraintsOfTypeTo(
                fluents[1], FluentConstraint.Type.MATCHES);
        assertEquals(1, matches.size());
        assertEquals(m1, matches.get(0));

        // Query constraints FROM fl2 of type OPENS
        List<FluentConstraint> opens = solver.getFluentConstraintsOfTypeFrom(
                fluents[2], FluentConstraint.Type.OPENS);
        assertEquals(1, opens.size());
        assertEquals(o1, opens.get(0));
    }

    // ================================================================
    // Sub-solver cross-consistency
    // ================================================================

    @Test
    public void symbolicAndTemporalPropagationAreConsistent() {
        Fluent[] fluents = solver.createVariables(4);

        fluents[0].setName("on(mug1 loc1 none none)");
        fluents[1].setName("on(?mug ?loc none none)");
        fluents[2].setName("on(mug1 loc2 none none)");
        fluents[3].setName("on(mug2 loc1 none none)");

        // MATCHES between fl0 and fl1 should bind ?mug→mug1, ?loc→loc1
        FluentConstraint m1 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m1.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertTrue(solver.addConstraint(m1));

        // After binding, fl1 should be ground
        CompoundSymbolicVariable csv1 = fluents[1].getCompoundSymbolicVariable();
        assertEquals("on(mug1 loc1 none none)", csv1.getName());

        // The temporal solver should also be consistent (MATCHES adds Equals constraint)
        TemporalVariable tv0 = fluents[0].getTemporalVariable();
        TemporalVariable tv1 = fluents[1].getTemporalVariable();
        assertEquals("Temporal equality after MATCHES", tv0.getEST(), tv1.getEST());
        assertEquals("Temporal equality after MATCHES", tv0.getEET(), tv1.getEET());
    }

    @Test
    public void multipleMatchesFormConsistentBindingChain() {
        Fluent[] fluents = solver.createVariables(5);

        fluents[0].setName("on(mug1 loc1 none none)");
        fluents[1].setName("on(?mug1 loc1 none none)");
        fluents[2].setName("on(?mug2 ?loc none none)");
        fluents[3].setName("on(mug2 loc1 none none)");
        fluents[4].setName("on(?mug3 loc1 none none)");

        // Chain: fl0 = fl1 = fl2 = fl4
        FluentConstraint m01 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m01.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[1]});
        assertTrue(solver.addConstraint(m01));

        FluentConstraint m12 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m12.setScope(new chimp.kernel.Variable[]{fluents[1], fluents[2]});
        assertTrue(solver.addConstraint(m12));

        FluentConstraint m24 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m24.setScope(new chimp.kernel.Variable[]{fluents[2], fluents[4]});
        assertTrue(solver.addConstraint(m24));

        // All should be ground to mug1
        assertEquals("on(mug1 loc1 none none)",
                fluents[4].getCompoundSymbolicVariable().getName());

        // fl3 = on(mug2 loc1) is different — MATCHES with fl0 should fail
        FluentConstraint m03 = new FluentConstraint(FluentConstraint.Type.MATCHES);
        m03.setScope(new chimp.kernel.Variable[]{fluents[0], fluents[3]});
        assertFalse(solver.addConstraint(m03));
    }
}
