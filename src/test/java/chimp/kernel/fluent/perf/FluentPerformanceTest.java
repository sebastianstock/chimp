package chimp.kernel.fluent.perf;

import static org.junit.Assert.assertTrue;

import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.Variable;

import org.junit.Test;

/**
 * Performance comparison: CHIMP-native fluentSolver vs Meta-CSP fluentSolver.
 *
 * <p>Each test runs identical workloads on both solvers and prints
 * timing ratios.  Higher ratio = CHIMP faster.  Ratios above 2 are
 * interesting; below 0.5 are regressions.
 */
public class FluentPerformanceTest {

    private static final long ORIGIN = 0;
    private static final long HORIZON = 100_000;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;

    // -- shared symbol domains --
    private static final String[][] SYMBOLS = buildSymbols();
    private static final int[] SYMBOL_INGREDIENTS = {1, 1, 1, 1, 1};

    private static String[][] buildSymbols() {
        String[] preds = {"on", "robotat", "get_mug", "move_base", "serve_coffee", "pick", "place"};
        String[] mugs  = {"m1","m2","m3","m4","m5","m6","m7","m8","m9","m10","none"};
        String[] pl    = {"pl1","pl2","pl3","pl4","pl5","pl6","pl7","pl8","pl9","pl10","none"};
        String[] ma    = {"ma1","ma2","ma3","ma4","ma5","ma6","ma7","ma8","ma9","ma10","none"};
        String[] pma   = {"pma1","pma2","pma3","pma4","pma5","pma6","pma7","pma8","pma9","pma10","none"};
        return new String[][] { preds, mugs, pl, ma, pma };
    }

    // ================================================================
    // Scenarios
    // ================================================================

    @Test public void compareCreateVariables()   { run("createVariables",   this::benchCreateVariables); }
    @Test public void compareAddMatches()         { run("addMatches",         this::benchAddMatches); }
    @Test public void compareAddCombined()        { run("addCombined",        this::benchAddCombined); }
    @Test public void compareQueries()            { run("queries",           this::benchQueries); }

    // ================================================================
    // Benchmarks
    // ================================================================

    private void benchCreateVariables(int n) {
        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long t0 = System.nanoTime();
            fluentSolver.FluentNetworkSolver ms =
                    new fluentSolver.FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            ms.createVariables(n);
            long t1 = System.nanoTime();

            long t2 = System.nanoTime();
            FluentNetworkSolver cs =
                    new FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            cs.createVariables(n);
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
        }
        report("create " + n + " vars", meta, chimp);
    }

    /** Add MATCHES constraints between N fluents (chain: 0↔1, 1↔2, …). */
    private void benchAddMatches(int n) {
        int cons = Math.max(1, n - 1);

        // Build warmup
        for (int w = 0; w < WARMUP_RUNS; w++) {
            fluentSolver.FluentNetworkSolver wm =
                    new fluentSolver.FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            fluentSolver.Fluent[] wmv =
                    (fluentSolver.Fluent[]) wm.createVariables(n);
            for (int i = 0; i < n; i++) wmv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
            for (int i = 0; i < cons; i++) {
                fluentSolver.FluentConstraint c =
                        new fluentSolver.FluentConstraint(fluentSolver.FluentConstraint.Type.MATCHES);
                c.setFrom(wmv[i]); c.setTo(wmv[i + 1]);
                wm.addConstraint(c);
            }

            FluentNetworkSolver wc =
                    new FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            Fluent[] wcv = wc.createVariables(n);
            for (int i = 0; i < n; i++) wcv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
            for (int i = 0; i < cons; i++) {
                FluentConstraint c =
                        new FluentConstraint(FluentConstraint.Type.MATCHES);
                c.setScope(new Variable[] { wcv[i], wcv[i + 1] });
                wc.addConstraint(c);
            }
        }

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            fluentSolver.FluentNetworkSolver ms =
                    new fluentSolver.FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            fluentSolver.Fluent[] mv =
                    (fluentSolver.Fluent[]) ms.createVariables(n);
            for (int i = 0; i < n; i++) mv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
            long t0 = System.nanoTime();
            for (int i = 0; i < cons; i++) {
                fluentSolver.FluentConstraint c =
                        new fluentSolver.FluentConstraint(fluentSolver.FluentConstraint.Type.MATCHES);
                c.setFrom(mv[i]); c.setTo(mv[i + 1]);
                ms.addConstraint(c);
            }
            long t1 = System.nanoTime();

            FluentNetworkSolver cs =
                    new FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
            Fluent[] cv = cs.createVariables(n);
            for (int i = 0; i < n; i++) cv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
            long t2 = System.nanoTime();
            for (int i = 0; i < cons; i++) {
                FluentConstraint c =
                        new FluentConstraint(FluentConstraint.Type.MATCHES);
                c.setScope(new Variable[] { cv[i], cv[i + 1] });
                cs.addConstraint(c);
            }
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
        }
        report(cons + " MATCHES on " + n + " vars", meta, chimp);
    }

    /** Mix of MATCHES, DC, PRE constraints on N fluents. */
    private void benchAddCombined(int n) {
        int cons = Math.max(1, n - 1);

        // Warmup identical to measured
        for (int w = 0; w < WARMUP_RUNS; w++) {
            benchAddCombinedOnce(n, cons);
        }

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long[] pair = benchAddCombinedOnce(n, cons);
            meta += pair[0];
            chimp += pair[1];
        }
        report(cons + " mixed on " + n + " vars", meta, chimp);
    }

    private long[] benchAddCombinedOnce(int n, int cons) {
        // Meta-CSP
        fluentSolver.FluentNetworkSolver ms =
                new fluentSolver.FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
        fluentSolver.Fluent[] mv = (fluentSolver.Fluent[]) ms.createVariables(n);
        for (int i = 0; i < n; i++) mv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
        long t0 = System.nanoTime();
        for (int i = 0; i < cons; i++) {
            fluentSolver.FluentConstraint.Type type;
            String name;
            if (i % 3 == 0) {
                type = fluentSolver.FluentConstraint.Type.MATCHES;
                name = mv[i].getName().replaceAll("\\d+", "?"); // wildcard
            } else if (i % 3 == 1) {
                type = fluentSolver.FluentConstraint.Type.DC;
                name = mv[i + 1].getName();
            } else {
                type = fluentSolver.FluentConstraint.Type.PRE;
                name = mv[i].getName();
            }
            fluentSolver.FluentConstraint c = new fluentSolver.FluentConstraint(type);
            c.setFrom(mv[i]); c.setTo(mv[i + 1]);
            ms.addConstraint(c);
        }
        long t1 = System.nanoTime();

        // CHIMP
        FluentNetworkSolver cs =
                new FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
        Fluent[] cv = cs.createVariables(n);
        for (int i = 0; i < n; i++) cv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
        long t2 = System.nanoTime();
        for (int i = 0; i < cons; i++) {
            FluentConstraint.Type type;
            if (i % 3 == 0) {
                type = FluentConstraint.Type.MATCHES;
            } else if (i % 3 == 1) {
                type = FluentConstraint.Type.DC;
            } else {
                type = FluentConstraint.Type.PRE;
            }
            FluentConstraint c =
                    new FluentConstraint(type);
            c.setScope(new Variable[] { cv[i], cv[i + 1] });
            cs.addConstraint(c);
        }
        long t3 = System.nanoTime();

        return new long[] { t1 - t0, t3 - t2 };
    }

    /** Query benchmarks: getOpenFluents, getConstraintsTo, getFluentConstraintsOfTypeTo. */
    private void benchQueries(int n) {
        int cons = Math.max(1, n - 1);

        // Pre-built meta solver
        fluentSolver.FluentNetworkSolver ms =
                new fluentSolver.FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
        fluentSolver.Fluent[] mv = (fluentSolver.Fluent[]) ms.createVariables(n);
        for (int i = 0; i < n; i++) mv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
        for (int i = 0; i < cons; i++) {
            fluentSolver.FluentConstraint c =
                    new fluentSolver.FluentConstraint(fluentSolver.FluentConstraint.Type.MATCHES);
            c.setFrom(mv[i]); c.setTo(mv[i + 1]);
            ms.addConstraint(c);
        }
        mv[0].setMarking(3); // OPEN
        mv[1].setMarking(4); // CLOSED
        mv[2].setMarking(3); // OPEN

        // Pre-built CHIMP solver
        FluentNetworkSolver cs =
                new FluentNetworkSolver(ORIGIN, HORIZON, SYMBOLS, SYMBOL_INGREDIENTS);
        Fluent[] cv = cs.createVariables(n);
        for (int i = 0; i < n; i++) cv[i].setName("on(m" + ((i%10)+1) + " pl" + ((i%10)+1) + " none none)");
        for (int i = 0; i < cons; i++) {
            FluentConstraint c =
                    new FluentConstraint(FluentConstraint.Type.MATCHES);
            c.setScope(new Variable[] { cv[i], cv[i + 1] });
            cs.addConstraint(c);
        }
        cv[0].setMarking(3); // OPEN
        cv[1].setMarking(4); // CLOSED
        cv[2].setMarking(3); // OPEN

        long meta = 0, chimp = 0;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long t0 = System.nanoTime();
            fluentSolver.Fluent[] openM = ms.getOpenFluents();
            var consToM = ms.getConstraintsTo(mv[0]);
            var byTypeM = ms.getFluentConstraintsOfTypeTo(
                    mv[0], fluentSolver.FluentConstraint.Type.MATCHES);
            long t1 = System.nanoTime();

            long t2 = System.nanoTime();
            Fluent[] openC = cs.getOpenFluents();
            var consToC = cs.getConstraintsTo(cv[0]);
            var byTypeC = cs.getFluentConstraintsOfTypeTo(
                    cv[0], FluentConstraint.Type.MATCHES);
            long t3 = System.nanoTime();

            meta += (t1 - t0);
            chimp += (t3 - t2);
            // Prevent dead-code elimination
            assertTrue(openM.length >= 0 && openC.length >= 0);
            assertTrue(consToM.size() >= 0 && consToC.size() >= 0);
            assertTrue(byTypeM.size() >= 0 && byTypeC.size() >= 0);
        }
        report("3 queries ×" + n, meta, chimp);
    }

    // ================================================================
    // Helpers
    // ================================================================

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
