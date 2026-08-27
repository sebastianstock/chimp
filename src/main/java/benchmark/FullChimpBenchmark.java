package benchmark;

import chimp.model.PlannedAction;
import chimp.logging.ChimpLogging;
import externalPathPlanning.LookUpTableDurationEstimator;
import externalPathPlanning.LookUpTableDurationEstimatorICO;
import externalPathPlanning.MoveBaseDurationEstimator;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Full-CHIMP end-to-end benchmark: runs every textual planning domain/problem
 * pair, times {@link CHIMP#generatePlan()}, and captures the resulting plan for
 * correctness comparison.
 *
 * <p>Uses only the stable {@code planner.CHIMP} facade API (no valHeuristic —
 * the default {@code UnifyFewestsubsEarliesttasksNewestbindingsValOH} is used on
 * both branches) so that this single source file compiles and runs identically
 * on the current (CHIMP-native) branch and the {@code legacy-metacsp} branch.
 *
 * <p>For each problem: 1 warm-up run (discarded) + N measured runs; reports the
 * median solver time (from {@link CHIMP#getPlanningTime()}) and the median
 * wall-clock time around {@code generatePlan()}.
 *
 * <p>Usage:
 * <pre>
 *   ./gradlew benchmark --args="build/benchmark/native.csv 3"
 * </pre>
 *
 * <p>Output CSV columns:
 * <pre>
 * label,domain,problem,found,planLength,solverMsMedian,wallMsMedian,actions
 * </pre>
 * where {@code actions} is a {@code ;}-separated list of {@code name(arg1,arg2)}
 * sorted by EST.
 */
public class FullChimpBenchmark {

    /** One entry per domain/problem pair. */
    private record Entry(String label, String domain, String problem,
                         MoveBaseDurationEstimator mb, boolean htnUnification) {}

    private static final MoveBaseDurationEstimator MB = new LookUpTableDurationEstimator();
    private static final MoveBaseDurationEstimator MB_ICO = new LookUpTableDurationEstimatorICO();

    private static final Entry[] SUITE = {
            // --- DWR (18) ---
            new Entry("DWR-leave",      "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_leave.pdl",      MB, true),
            new Entry("DWR-enter",      "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_enter.pdl",      MB, true),
            new Entry("DWR-move",       "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_move.pdl",       MB, true),
            new Entry("DWR-stack",      "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_stack.pdl",       MB, true),
            new Entry("DWR-unstack",    "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_unstack.pdl",    MB, true),
            new Entry("DWR-put",        "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_put.pdl",         MB, true),
            new Entry("DWR-take",       "domains/dwr/dwr.ddl", "domains/dwr/test/test_op_take.pdl",       MB, true),
            new Entry("DWR-m_load",     "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_load.pdl",        MB, true),
            new Entry("DWR-m_unload",   "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_unload.pdl",      MB, true),
            new Entry("DWR-m_uncover0", "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_uncover0.pdl",   MB, true),
            new Entry("DWR-m_uncover1", "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_uncover1.pdl",    MB, true),
            new Entry("DWR-m_navigate0","domains/dwr/dwr.ddl", "domains/dwr/test/test_m_navigate0.pdl",  MB, true),
            new Entry("DWR-m_navigate1","domains/dwr/dwr.ddl", "domains/dwr/test/test_m_navigate1.pdl",   MB, true),
            new Entry("DWR-m_goto0",    "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_goto0.pdl",       MB, true),
            new Entry("DWR-m_goto1",    "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_goto1.pdl",       MB, true),
            new Entry("DWR-m_bring0",   "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_bring0.pdl",     MB, true),
            new Entry("DWR-m_bring1",   "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_bring1.pdl",     MB, true),
            new Entry("DWR-m_bring2",   "domains/dwr/dwr.ddl", "domains/dwr/test/test_m_bring2.pdl",     MB, true),

            // --- RACE (10) ---
            new Entry("RACE-tuck_arms",        "domains/ordered_domain.ddl", "problems/test_op_tuck_arms.pdl",              MB, true),
            new Entry("RACE-move_base",         "domains/ordered_domain.ddl", "problems/test_op_move_base.pdl",             MB, true),
            new Entry("RACE-move_torso",        "domains/ordered_domain.ddl", "problems/test_op_move_torso.pdl",            MB, true),
            new Entry("RACE-move_base_blind",   "domains/ordered_domain.ddl", "problems/test_op_move_base_blind.pdl",        MB, true),
            new Entry("RACE-pick_up",           "domains/ordered_domain.ddl", "problems/test_op_pick_up_object.pdl",        MB, true),
            new Entry("RACE-place",             "domains/ordered_domain.ddl", "problems/test_op_place_object.pdl",          MB, true),
            new Entry("RACE-move_arm_side",    "domains/ordered_domain.ddl", "problems/test_op_move_arm_to_side.pdl",      MB, true),
            new Entry("RACE-carryposture",      "domains/ordered_domain.ddl", "problems/test_op_move_arms_to_carryposture.pdl", MB, true),
            new Entry("RACE-observe",           "domains/ordered_domain.ddl", "problems/test_op_observe_objects_on_area.pdl", MB, true),
            new Entry("RACE-serve_coffee",      "domains/ordered_domain.ddl", "problems/test_m_serve_coffee_problem_1.pdl",   MB, true),

            // --- TransTerra (7) ---
            new Entry("TransTerra-move_to",         "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_move_to.pdl",          MB, true),
            new Entry("TransTerra-sample",         "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_sample_regolith.pdl",  MB, true),
            new Entry("TransTerra-transfer_sample", "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_transfer_sample.pdl",  MB, true),
            new Entry("TransTerra-transfer_payload","domains/transterra_v1.ddl","problems/transterra_problems_v1/test_op_transfer_payload.pdl", MB, true),
            new Entry("TransTerra-pickup",          "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_pickup_basecamp.pdl",  MB, true),
            new Entry("TransTerra-pickup2",         "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl",MB, true),
            new Entry("TransTerra-place",           "domains/transterra_v1.ddl", "problems/transterra_problems_v1/test_op_place_basecamp.pdl",  MB, true),

            // --- Mobipick (1) ---
            new Entry("Mobipick-simple_nav", "domains/mobipick/simple_navigation/domain.ddl",
                    "domains/mobipick/simple_navigation/problem.pdl", MB, true),

            // --- Integer (1) ---
            new Entry("Integer-simple", "domains/simple_integer_domain.ddl",
                    "problems/simple_integer_problem.pdl", null, true),

            // --- Gecko (1) ---
            new Entry("Gecko-gecko1", "domains/gecko.ddl",
                    "problems/gecko/gecko1.pdl", null, false),

            // --- ICO (2) ---
            new Entry("ICO-move_2_objects",   "domains/ico.ddl", "problems/ico/move_2_objects.pdl",                MB_ICO, false),
            new Entry("ICO-serve_coffee",     "domains/ico.ddl", "problems/ico/test_m_serve_coffee_problem_1.pdl", MB_ICO, false),
    };

    public static void main(String[] args) throws Exception {
        String outFile = (args.length >= 1) ? args[0] : "build/benchmark/benchmark.csv";
        int runs = (args.length >= 2) ? Integer.parseInt(args[1]) : 3;

        // Suppress verbose planner logging so it doesn't skew wall-clock time.
        ChimpLogging.setLevel(java.util.logging.Level.OFF);

        Path out = Paths.get(outFile);
        Files.createDirectories(out.getParent());
        try (PrintWriter w = new PrintWriter(out.toFile())) {
            w.println("label,domain,problem,found,planLength,solverMsMedian,wallMsMedian,actions");
            System.out.println("label,domain,problem,found,planLength,solverMsMedian,wallMsMedian,actions");

            for (Entry e : SUITE) {
                runOne(w, e, runs);
            }
        }
        System.out.println();
        System.out.println("Wrote " + out);
    }

    private static void runOne(PrintWriter w, Entry e, int runs) {
        // Warm-up run (discarded) — primes JIT/class-loading.
        try {
            solve(e);
        } catch (Throwable t) {
            // warm-up failure is fine; real measurement below will catch it too
        }

        long[] solverMs = new long[runs];
        long[] wallMs   = new long[runs];
        boolean found = false;
        PlannedAction[] plan = null;
        String error = null;

        for (int i = 0; i < runs; i++) {
            try {
                CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(e.domain(), e.problem())
                        .htnUnification(e.htnUnification());
                if (e.mb() != null) builder.mbEstimator(e.mb());
                CHIMP chimp = builder.build();

                long t0 = System.nanoTime();
                found = chimp.generatePlan();
                long t1 = System.nanoTime();
                solverMs[i] = chimp.getPlanningTime();
                wallMs[i]   = (t1 - t0) / 1_000_000L;
                if (found) plan = chimp.extractActions();
            } catch (Throwable t) {
                error = t.getClass().getSimpleName() + ": " + t.getMessage();
                found = false;
                solverMs[i] = -1;
                wallMs[i] = -1;
            }
        }

        long solverMedian = median(solverMs);
        long wallMedian   = median(wallMs);
        String actions = (found && plan != null) ? actionSummary(plan) : "";
        if (!found && error != null) actions = "ERR:" + error.replaceAll("[,;]", " ");

        String row = String.format("%s,%s,%s,%b,%d,%d,%d,%s",
                e.label(), e.domain(), e.problem(), found,
                (plan != null ? plan.length : 0),
                solverMedian, wallMedian, actions);
        w.println(row);
        w.flush();
        System.out.println(row);
    }

    private static CHIMP solve(Entry e) throws DomainParsingException {
        CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(e.domain(), e.problem())
                .htnUnification(e.htnUnification());
        if (e.mb() != null) builder.mbEstimator(e.mb());
        CHIMP chimp = builder.build();
        chimp.generatePlan();
        return chimp;
    }

    private static String actionSummary(PlannedAction[] plan) {
        PlannedAction[] sorted = plan.clone();
        Arrays.sort(sorted, Comparator.comparingLong(PlannedAction::est));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.length; i++) {
            if (i > 0) sb.append(";");
            sb.append(sorted[i].name()).append("(")
              .append(String.join(" ", sorted[i].arguments())).append(")");
        }
        return sb.toString();
    }

    private static long median(long[] vals) {
        long[] copy = vals.clone();
        Arrays.sort(copy);
        // ignore -1 (error) entries if any
        int n = 0;
        for (long v : copy) if (v >= 0) n++;
        if (n == 0) return -1;
        long[] valid = new long[n];
        int j = 0;
        for (long v : copy) if (v >= 0) valid[j++] = v;
        Arrays.sort(valid);
        return valid[valid.length / 2];
    }
}
