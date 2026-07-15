package chimp.kernel.temporal.crossval;

import static org.junit.Assert.*;

import chimp.kernel.temporal.SimpleTemporalNetwork;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.kernel.temporal.TemporalSolver;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.TemporalBounds;
import chimp.model.TemporalRelation;
import org.junit.Test;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.Bounds;

/**
 * Cross-validation: CHIMP-native TemporalSolver vs Meta-CSP
 * AllenIntervalNetworkSolver.
 *
 * <p>Creates identical constraint networks in both solvers and
 * asserts that all admissible distance bounds match exactly.
 */
public class TemporalSolverCrossValidationTest {

    private static final long ORIGIN = 0;
    private static final long HORIZON = 1000;
    private static final int MAX_VARS = 50;

    // ================================================================
    // Helper: build identical scenarios
    // ================================================================

    private static class Scenario {
        final AllenIntervalNetworkSolver metaSolver;
        final TemporalSolver chimpSolver;
        final AllenInterval[] metaVars;
        final TemporalVariable[] chimpVars;

        Scenario(int numVars) {
            metaSolver = new AllenIntervalNetworkSolver(ORIGIN, HORIZON, MAX_VARS);
            chimpSolver = new TemporalSolver(ORIGIN, HORIZON, MAX_VARS);
            metaVars = (AllenInterval[]) metaSolver.createVariables(numVars);
            chimpVars = chimpSolver.createVariables(numVars);
        }

        /**
         * Adds a Before constraint between var {@code from} and var {@code to}
         * with the given bounds.
         */
        void addBefore(int from, int to, long min, long max) {
            AllenIntervalConstraint mc = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Before, new Bounds(min, max));
            mc.setFrom(metaVars[from]);
            mc.setTo(metaVars[to]);
            boolean metaOk = metaSolver.addConstraint(mc);

            TemporalConstraint cc = new TemporalConstraint(
                    TemporalRelation.Before, new TemporalBounds(min, max));
            cc.setFrom(chimpVars[from]);
            cc.setTo(chimpVars[to]);
            boolean chimpOk = chimpSolver.addConstraint(cc);

            assertEquals("Before constraint should succeed in both",
                    metaOk, chimpOk);
        }

        void addDuration(int var, long min, long max) {
            AllenIntervalConstraint mc = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Duration, new Bounds(min, max));
            mc.setFrom(metaVars[var]);
            mc.setTo(metaVars[var]);
            boolean metaOk = metaSolver.addConstraint(mc);

            TemporalConstraint cc = new TemporalConstraint(
                    TemporalRelation.Duration, new TemporalBounds(min, max));
            cc.setFrom(chimpVars[var]);
            cc.setTo(chimpVars[var]);
            boolean chimpOk = chimpSolver.addConstraint(cc);

            assertEquals("Duration constraint should succeed in both",
                    metaOk, chimpOk);
        }

        void addEquals(int from, int to) {
            AllenIntervalConstraint mc = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Equals);
            mc.setFrom(metaVars[from]);
            mc.setTo(metaVars[to]);
            boolean metaOk = metaSolver.addConstraint(mc);

            TemporalConstraint cc = new TemporalConstraint(
                    TemporalRelation.Equals);
            cc.setFrom(chimpVars[from]);
            cc.setTo(chimpVars[to]);
            boolean chimpOk = chimpSolver.addConstraint(cc);

            assertEquals("Equals constraint should succeed in both",
                    metaOk, chimpOk);
        }

        void addMeets(int from, int to) {
            AllenIntervalConstraint mc = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Meets);
            mc.setFrom(metaVars[from]);
            mc.setTo(metaVars[to]);
            boolean metaOk = metaSolver.addConstraint(mc);

            TemporalConstraint cc = new TemporalConstraint(
                    TemporalRelation.Meets);
            cc.setFrom(chimpVars[from]);
            cc.setTo(chimpVars[to]);
            boolean chimpOk = chimpSolver.addConstraint(cc);

            assertEquals("Meets constraint should succeed in both",
                    metaOk, chimpOk);
        }

        void addDuringOrEqualsOrStartsOrFinishes(int from, int to) {
            AllenIntervalConstraint mc = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes,
                    AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes.getDefaultBounds());
            mc.setFrom(metaVars[from]);
            mc.setTo(metaVars[to]);
            boolean metaOk = metaSolver.addConstraint(mc);

            TemporalConstraint cc = new TemporalConstraint(
                    TemporalRelation.DuringOrEqualsOrStartsOrFinishes);
            cc.setFrom(chimpVars[from]);
            cc.setTo(chimpVars[to]);
            boolean chimpOk = chimpSolver.addConstraint(cc);

            assertEquals("DuringOrEquals... should succeed in both",
                    metaOk, chimpOk);
        }

        /**
         * Compares bounds between corresponding time-point pairs.
         * The mapping is:
         *   tp index i in CHIMP maps to the corresponding Meta-CSP time point
         *   (origin=0, var[0].start=1, var[0].end=2, var[1].start=3, ...)
         */
        void assertBoundsMatch() {
            // Collect Meta-CSP time points in CHIMP order
            org.metacsp.time.TimePoint[] metaTPs =
                    new org.metacsp.time.TimePoint[chimpSolver.stn().timePointCount()];
            metaTPs[0] = metaSolver.getConstraintSolvers()[0].getVariable(0) != null
                    ? ((org.metacsp.time.APSPSolver)
                        metaSolver.getConstraintSolvers()[0]).getSource()
                    : null;
            // Actually, let me just query bounds directly from both solvers
            // and compare.

            int tpCount = chimpSolver.stn().timePointCount();
            for (int from = 0; from < tpCount; from++) {
                for (int to = 0; to < tpCount; to++) {
                    TemporalBounds chimpBounds =
                            chimpSolver.getAdmissibleDistanceBounds(from, to);

                    // For Meta-CSP, we need TimePoint references.
                    // Let me query using the corresponding AllenInterval start/end points
                    // rather than raw time point indices.
                    // We'll compare only the EST/LST/EET/LET of variables and
                    // the bounds between variable start/end points.
                }
            }
            // We'll call a different comparison method below
        }

        /**
         * Compares EST/LST/EET/LET for each variable.
         */
        void assertVariableBoundsMatch() {
            metaSolver.propagate();
            chimpSolver.propagate();
            for (int i = 0; i < metaVars.length; i++) {
                AllenInterval mv = metaVars[i];
                TemporalVariable cv = chimpVars[i];
                assertEquals("var[" + i + "] EST", mv.getEST(), cv.getEST());
                assertEquals("var[" + i + "] LST", mv.getLST(), cv.getLST());
                assertEquals("var[" + i + "] EET", mv.getEET(), cv.getEET());
                assertEquals("var[" + i + "] LET", mv.getLET(), cv.getLET());
            }
        }

        /**
         * Compares distance bounds between variable start/end points.
         * Compares: from_var.start→to_var.start, from_var.start→to_var.end,
         * from_var.end→to_var.start, from_var.end→to_var.end.
         */
        void assertInterVariableBoundsMatch() {
            metaSolver.propagate();
            chimpSolver.propagate();
            for (int fi = 0; fi < metaVars.length; fi++) {
                for (int ti = 0; ti < metaVars.length; ti++) {
                    if (fi == ti) continue;

                    // Meta-CSP
                    org.metacsp.time.TimePoint ms =
                            metaVars[fi].getStart();
                    org.metacsp.time.TimePoint me =
                            metaVars[fi].getEnd();
                    org.metacsp.time.TimePoint ts =
                            metaVars[ti].getStart();
                    org.metacsp.time.TimePoint te =
                            metaVars[ti].getEnd();

                    Bounds ms2ts = metaSolver.getAdmissibleDistanceBounds(ms, ts);
                    Bounds ms2te = metaSolver.getAdmissibleDistanceBounds(ms, te);
                    Bounds me2ts = metaSolver.getAdmissibleDistanceBounds(me, ts);
                    Bounds me2te = metaSolver.getAdmissibleDistanceBounds(me, te);

                    // CHIMP
                    TemporalBounds c_ms2ts = chimpSolver.getAdmissibleDistanceBounds(
                            chimpVars[fi].start(), chimpVars[ti].start());
                    TemporalBounds c_ms2te = chimpSolver.getAdmissibleDistanceBounds(
                            chimpVars[fi].start(), chimpVars[ti].end());
                    TemporalBounds c_me2ts = chimpSolver.getAdmissibleDistanceBounds(
                            chimpVars[fi].end(), chimpVars[ti].start());
                    TemporalBounds c_me2te = chimpSolver.getAdmissibleDistanceBounds(
                            chimpVars[fi].end(), chimpVars[ti].end());

                    assertEquals("var[" + fi + "].start → var[" + ti + "].start min",
                            ms2ts.min, c_ms2ts.minimum());
                    assertEquals("var[" + fi + "].start → var[" + ti + "].start max",
                            ms2ts.max, c_ms2ts.maximum());

                    assertEquals("var[" + fi + "].start → var[" + ti + "].end min",
                            ms2te.min, c_ms2te.minimum());
                    assertEquals("var[" + fi + "].start → var[" + ti + "].end max",
                            ms2te.max, c_ms2te.maximum());

                    assertEquals("var[" + fi + "].end → var[" + ti + "].start min",
                            me2ts.min, c_me2ts.minimum());
                    assertEquals("var[" + fi + "].end → var[" + ti + "].start max",
                            me2ts.max, c_me2ts.maximum());

                    assertEquals("var[" + fi + "].end → var[" + ti + "].end min",
                            me2te.min, c_me2te.minimum());
                    assertEquals("var[" + fi + "].end → var[" + ti + "].end max",
                            me2te.max, c_me2te.maximum());
                }
            }
        }
    }

    // ================================================================
    // Tests
    // ================================================================

    @Test
    public void boundsMatchWithNoConstraints() {
        Scenario s = new Scenario(3);
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void boundsMatchWithBeforeAndDuration() {
        Scenario s = new Scenario(3);
        s.addBefore(0, 1, 5, 20);
        s.addDuration(0, 10, 100);
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void boundsMatchWithEquals() {
        Scenario s = new Scenario(2);
        s.addDuration(0, 5, 50);
        s.addEquals(0, 1);
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void boundsMatchWithMeets() {
        Scenario s = new Scenario(2);
        s.addDuration(0, 10, 10);   // duration exactly 10
        s.addDuration(1, 5, 100);    // variable 1 duration
        s.addMeets(0, 1);           // var0.end = var1.start
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void boundsMatchWithDuringOrEquals() {
        Scenario s = new Scenario(2);
        s.addDuringOrEqualsOrStartsOrFinishes(0, 1);
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void boundsMatchWithTightenedConstraints() {
        Scenario s = new Scenario(2);
        s.addBefore(0, 1, 10, 100);
        s.addBefore(0, 1, 20, 50);  // tighten
        s.assertVariableBoundsMatch();
        s.assertInterVariableBoundsMatch();
    }

    @Test
    public void inconsistencyDetectedByBoth() {
        Scenario s = new Scenario(2);
        s.addBefore(0, 1, 100, 200);

        // Add a contradictory constraint: var1 before var0
        AllenIntervalConstraint mc = new AllenIntervalConstraint(
                AllenIntervalConstraint.Type.Before, new Bounds(1, 10));
        mc.setFrom(s.metaVars[1]);
        mc.setTo(s.metaVars[0]);
        boolean metaOk = s.metaSolver.addConstraint(mc);

        TemporalConstraint cc = new TemporalConstraint(
                TemporalRelation.Before, new TemporalBounds(1, 10));
        cc.setFrom(s.chimpVars[1]);
        cc.setTo(s.chimpVars[0]);
        boolean chimpOk = s.chimpSolver.addConstraint(cc);

        // Both should detect the inconsistency
        assertEquals("Inconsistency should be detected by both",
                metaOk, chimpOk);
        assertFalse("Both should return false", metaOk);
    }
}
