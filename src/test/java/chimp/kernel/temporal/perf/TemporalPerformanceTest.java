package chimp.kernel.temporal.perf;

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
 * Performance comparison: CHIMP-native TemporalSolver vs Meta-CSP
 * AllenIntervalNetworkSolver.
 *
 * <p>Each test runs identical workloads on both solvers and prints
 * timing ratios.  Higher is better for CHIMP; lower is worse.
 */
public class TemporalPerformanceTest {

    private static final long ORIGIN = 0;
    private static final long HORIZON = 100_000;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;

    // ================================================================
    // Scenarios
    // ================================================================

    @Test public void compareCreateVariables()   { run("createVariables",   this::benchCreateVariables); }
    @Test public void compareAddConstraints()     { run("addConstraints",    this::benchAddConstraints); }
    @Test public void compareQueryBounds()        { run("queryBounds",       this::benchQueryBounds); }
    @Test public void compareAddAndRemove()       { run("addAndRemove",      this::benchAddAndRemove); }

    // ================================================================
    // Benchmarks
    // ================================================================

    /** Variable creation: N variables. */
    private void benchCreateVariables(int n) {
        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long t0 = System.nanoTime();
            AllenIntervalNetworkSolver ms =
                    new AllenIntervalNetworkSolver(ORIGIN, HORIZON, n * 2 + 10);
            ms.createVariables(n);
            long t1 = System.nanoTime();
            TemporalSolver cs = new TemporalSolver(ORIGIN, HORIZON, n * 2 + 10);
            cs.createVariables(n);
            long t2 = System.nanoTime();
            meta += (t1 - t0);
            chimp += (t2 - t1);
        }
        report("create " + n + " vars", meta, chimp);
    }

    /** Constraint addition: N constraints on M variables. */
    private void benchAddConstraints(int scale) {
        int vars = scale;
        int cons = Math.max(1, scale / 2);

        // Build meta solver
        AllenIntervalNetworkSolver ms =
                new AllenIntervalNetworkSolver(ORIGIN, HORIZON, vars * 2 + 10);
        AllenInterval[] mv = (AllenInterval[]) ms.createVariables(vars);

        // Build CHIMP solver
        TemporalSolver cs = new TemporalSolver(ORIGIN, HORIZON, vars * 2 + 10);
        TemporalVariable[] cv = cs.createVariables(vars);

        // Warmup both
        for (int r = 0; r < WARMUP_RUNS; r++) {
            AllenIntervalNetworkSolver wm = freshMeta(vars);
            AllenInterval[] wmv = (AllenInterval[]) wm.createVariables(vars);
            for (int i = 0; i < cons; i++) {
                AllenIntervalConstraint c = new AllenIntervalConstraint(
                        AllenIntervalConstraint.Type.Before,
                        new Bounds((i % 10) + 1, (i % 10) + 51));
                c.setFrom(wmv[i % vars]);
                c.setTo(wmv[(i + 1) % vars]);
                wm.addConstraint(c);
            }
            TemporalSolver wc = freshChimp(vars);
            TemporalVariable[] wcv = wc.createVariables(vars);
            for (int i = 0; i < cons; i++) {
                TemporalConstraint c = new TemporalConstraint(
                        TemporalRelation.Before,
                        new TemporalBounds((i % 10) + 1, (i % 10) + 51));
                c.setFrom(wcv[i % vars]);
                c.setTo(wcv[(i + 1) % vars]);
                wc.addConstraint(c);
            }
        }

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            // Meta
            AllenIntervalNetworkSolver mRun = freshMeta(vars);
            AllenInterval[] mvRun = (AllenInterval[]) mRun.createVariables(vars);
            long t0 = System.nanoTime();
            for (int i = 0; i < cons; i++) {
                AllenIntervalConstraint c = new AllenIntervalConstraint(
                        AllenIntervalConstraint.Type.Before,
                        new Bounds((i % 10) + 1, (i % 10) + 51));
                c.setFrom(mvRun[i % vars]);
                c.setTo(mvRun[(i + 1) % vars]);
                mRun.addConstraint(c);
            }
            long t1 = System.nanoTime();

            // CHIMP
            TemporalSolver cRun = freshChimp(vars);
            TemporalVariable[] cvRun = cRun.createVariables(vars);
            long t2 = System.nanoTime();
            for (int i = 0; i < cons; i++) {
                TemporalConstraint c = new TemporalConstraint(
                        TemporalRelation.Before,
                        new TemporalBounds((i % 10) + 1, (i % 10) + 51));
                c.setFrom(cvRun[i % vars]);
                c.setTo(cvRun[(i + 1) % vars]);
                cRun.addConstraint(c);
            }
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
        }
        report(cons + " constraints on " + vars + " vars", meta, chimp);
    }

    /** Bound query: EST/LST/EET/LET on all variables after adding constraints. */
    private void benchQueryBounds(int scale) {
        int vars = scale;
        int cons = vars / 2;

        AllenIntervalNetworkSolver ms = freshMeta(vars);
        AllenInterval[] mv = (AllenInterval[]) ms.createVariables(vars);
        for (int i = 0; i < cons; i++) {
            AllenIntervalConstraint c = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Before,
                    new Bounds(1, HORIZON / 2));
            c.setFrom(mv[i % vars]);
            c.setTo(mv[(i + 1) % vars]);
            ms.addConstraint(c);
        }

        TemporalSolver cs = freshChimp(vars);
        TemporalVariable[] cv = cs.createVariables(vars);
        for (int i = 0; i < cons; i++) {
            TemporalConstraint c = new TemporalConstraint(
                    TemporalRelation.Before,
                    new TemporalBounds(1, HORIZON / 2));
            c.setFrom(cv[i % vars]);
            c.setTo(cv[(i + 1) % vars]);
            cs.addConstraint(c);
        }

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < vars; i++) {
                mv[i].getEST(); mv[i].getLST();
                mv[i].getEET(); mv[i].getLET();
            }
            long t1 = System.nanoTime();

            long t2 = System.nanoTime();
            for (int i = 0; i < vars; i++) {
                cv[i].getEST(); cv[i].getLST();
                cv[i].getEET(); cv[i].getLET();
            }
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
        }
        report("query bounds ×" + vars, meta, chimp);
    }

    /** Add then remove constraints: tests incremental rebuild performance. */
    private void benchAddAndRemove(int scale) {
        int vars = scale;
        int cons = vars / 2;

        AllenIntervalNetworkSolver ms = freshMeta(vars);
        AllenInterval[] mv = (AllenInterval[]) ms.createVariables(vars);
        TemporalSolver cs = freshChimp(vars);
        TemporalVariable[] cv = cs.createVariables(vars);

        // Pre-build both with constraints
        AllenIntervalConstraint[] metaCons = new AllenIntervalConstraint[cons];
        TemporalConstraint[] chimpCons = new TemporalConstraint[cons];
        for (int i = 0; i < cons; i++) {
            metaCons[i] = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.Before,
                    new Bounds((i % 10) + 1, (i % 10) + 51));
            metaCons[i].setFrom(mv[i % vars]);
            metaCons[i].setTo(mv[(i + 1) % vars]);
            ms.addConstraint(metaCons[i]);

            chimpCons[i] = new TemporalConstraint(
                    TemporalRelation.Before,
                    new TemporalBounds((i % 10) + 1, (i % 10) + 51));
            chimpCons[i].setFrom(cv[i % vars]);
            chimpCons[i].setTo(cv[(i + 1) % vars]);
            cs.addConstraint(chimpCons[i]);
        }

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            // Remove half, then re-add
            long t0 = System.nanoTime();
            for (int i = 0; i < cons / 2; i++) {
                ms.removeConstraint(metaCons[i]);
            }
            for (int i = 0; i < cons / 2; i++) {
                ms.addConstraint(metaCons[i]);
            }
            long t1 = System.nanoTime();

            long t2 = System.nanoTime();
            for (int i = 0; i < cons / 2; i++) {
                cs.removeConstraint(chimpCons[i]);
            }
            for (int i = 0; i < cons / 2; i++) {
                cs.addConstraint(chimpCons[i]);
            }
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
        }
        report("remove+add " + (cons / 2) + "× on " + vars + " vars", meta, chimp);
    }

    // ================================================================
    // Helpers
    // ================================================================

    private AllenIntervalNetworkSolver freshMeta(int maxVars) {
        return new AllenIntervalNetworkSolver(ORIGIN, HORIZON, maxVars * 2 + 10);
    }

    private TemporalSolver freshChimp(int maxVars) {
        return new TemporalSolver(ORIGIN, HORIZON, maxVars * 2 + 10);
    }

    private void run(String name, java.util.function.IntConsumer bench) {
        System.out.println("\n=== " + name + " ===");
        for (int scale : new int[]{5, 20, 50, 100}) {
            bench.accept(scale);
        }
    }

    private void report(String label, long metaNs, long chimpNs) {
        double metaMs = metaNs / (MEASURED_RUNS * 1_000_000.0);
        double chimpMs = chimpNs / (MEASURED_RUNS * 1_000_000.0);
        double ratio = metaMs / Math.max(chimpMs, 0.001);
        String verdict = ratio >= 0.95 ? "≈" : (ratio >= 1.0 ? "✓" : "✗");
        System.out.printf("  %-45s meta=%7.3fms  chimp=%7.3fms  ratio=%5.2f %s%n",
                label, metaMs, chimpMs, ratio, verdict);
    }
}
