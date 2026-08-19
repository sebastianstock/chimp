package characterization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import chimp.model.PlannedAction;
import externalPathPlanning.LookUpTableDurationEstimator;
import chimp.kernel.fluent.FluentNetworkSolver;
import htn.valOrderingHeuristics.CHIMPValOH;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import planner.CHIMP;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Baseline characterization for M15 Step 0.
 *
 * <p>Captures fluent count, constraint count, plan length, and action
 * descriptions for every domain/problem pair exercised by the JUnit test
 * suite. Results are written to {@code build/characterization/baseline.txt}.
 *
 * <p>This is <em>not</em> an auto-asserting regression test — it is a
 * capture-only tool to produce a reference file that can be manually diffed
 * after each M15 step.
 *
 * <p>Run with:
 * <pre>
 *   ./gradlew test --tests characterization.Step0Baseline
 * </pre>
 */
public class Step0Baseline {

    private static final Path BASELINE_FILE =
            Paths.get("build", "characterization", "baseline.txt");
    private static final Path BASELINE_ERR =
            Paths.get("build", "characterization", "baseline-err.txt");
    private static PrintWriter out;
    private static PrintWriter err;
    private static int captured;
    private static int errors;

    @BeforeClass
    public static void setup() throws IOException {
        Files.createDirectories(BASELINE_FILE.getParent());
        out = new PrintWriter(new FileWriter(BASELINE_FILE.toFile()));
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        out.println("# Step 0 baseline captured " + ts);
        out.println("# Format: LABEL | fluents | totalConstraints | allenConstraints | planLength | timeMs");
        out.println("# Then: action summary per planned action");
        out.println();
        out.flush();
        err = new PrintWriter(new FileWriter(BASELINE_ERR.toFile()));
        captured = 0;
        errors = 0;
    }

    @AfterClass
    public static void teardown() {
        out.println();
        out.println("# Total captured: " + captured + "  errors: " + errors);
        out.close();
        err.close();
        System.out.println("Baseline written to " + BASELINE_FILE);
        if (errors > 0) {
            System.out.println("Errors written to " + BASELINE_ERR);
        }
    }

    // ============================================================
    // DWR — matches JUnitTestDWRDomain exactly
    // ============================================================

    private static final String DWR_DOMAIN = "domains/dwr/dwr.ddl";
    private static final CHIMPValOH DWR_VALOH = new UnifyDeepestWeightNewestbindingsValOH();
    private static final LookUpTableDurationEstimator DWR_MB =
            new LookUpTableDurationEstimator();

    private void captureDWR(String label, String problemFile) {
        capture(label, DWR_DOMAIN, problemFile, DWR_VALOH, DWR_MB, true);
    }

    @Test public void dwr_leave() { captureDWR("DWR-leave",
            "domains/dwr/test/test_op_leave.pdl"); }
    @Test public void dwr_enter() { captureDWR("DWR-enter",
            "domains/dwr/test/test_op_enter.pdl"); }
    @Test public void dwr_move() { captureDWR("DWR-move",
            "domains/dwr/test/test_op_move.pdl"); }
    @Test public void dwr_stack() { captureDWR("DWR-stack",
            "domains/dwr/test/test_op_stack.pdl"); }
    @Test public void dwr_unstack() { captureDWR("DWR-unstack",
            "domains/dwr/test/test_op_unstack.pdl"); }
    @Test public void dwr_put() { captureDWR("DWR-put",
            "domains/dwr/test/test_op_put.pdl"); }
    @Test public void dwr_take() { captureDWR("DWR-take",
            "domains/dwr/test/test_op_take.pdl"); }
    @Test public void dwr_m_load() { captureDWR("DWR-m_load",
            "domains/dwr/test/test_m_load.pdl"); }
    @Test public void dwr_m_unload() { captureDWR("DWR-m_unload",
            "domains/dwr/test/test_m_unload.pdl"); }
    @Test public void dwr_m_uncover0() { captureDWR("DWR-m_uncover0",
            "domains/dwr/test/test_m_uncover0.pdl"); }
    @Test public void dwr_m_uncover1() { captureDWR("DWR-m_uncover1",
            "domains/dwr/test/test_m_uncover1.pdl"); }
    @Test public void dwr_m_navigate0() { captureDWR("DWR-m_navigate0",
            "domains/dwr/test/test_m_navigate0.pdl"); }
    @Test public void dwr_m_navigate1() { captureDWR("DWR-m_navigate1",
            "domains/dwr/test/test_m_navigate1.pdl"); }
    @Test public void dwr_m_goto0() { captureDWR("DWR-m_goto0",
            "domains/dwr/test/test_m_goto0.pdl"); }
    @Test public void dwr_m_goto1() { captureDWR("DWR-m_goto1",
            "domains/dwr/test/test_m_goto1.pdl"); }
    @Test public void dwr_m_bring0() { captureDWR("DWR-m_bring0",
            "domains/dwr/test/test_m_bring0.pdl"); }
    @Test public void dwr_m_bring1() { captureDWR("DWR-m_bring1",
            "domains/dwr/test/test_m_bring1.pdl"); }
    @Test public void dwr_m_bring2() { captureDWR("DWR-m_bring2",
            "domains/dwr/test/test_m_bring2.pdl"); }

    // ============================================================
    // RACE — matches JUnitTestRACEDomain exactly
    // ============================================================

    private static final String RACE_DOMAIN = "domains/ordered_domain.ddl";
    private static final CHIMPValOH RACE_VALOH = new UnifyDeepestWeightNewestbindingsValOH();
    private static final LookUpTableDurationEstimator RACE_MB =
            new LookUpTableDurationEstimator();

    private void captureRACE(String label, String problemFile) {
        capture(label, RACE_DOMAIN, problemFile, RACE_VALOH, RACE_MB, true);
    }

    @Test public void race_tuck_arms() { captureRACE("RACE-tuck_arms",
            "problems/test_op_tuck_arms.pdl"); }
    @Test public void race_move_base() { captureRACE("RACE-move_base",
            "problems/test_op_move_base.pdl"); }
    @Test public void race_move_torso() { captureRACE("RACE-move_torso",
            "problems/test_op_move_torso.pdl"); }
    @Test public void race_move_base_blind() { captureRACE("RACE-move_base_blind",
            "problems/test_op_move_base_blind.pdl"); }
    @Test public void race_pick_up() { captureRACE("RACE-pick_up",
            "problems/test_op_pick_up_object.pdl"); }
    @Test public void race_place() { captureRACE("RACE-place",
            "problems/test_op_place_object.pdl"); }
    @Test public void race_move_arm_side() { captureRACE("RACE-move_arm_to_side",
            "problems/test_op_move_arm_to_side.pdl"); }
    @Test public void race_carryposture() { captureRACE("RACE-move_arms_to_carryposture",
            "problems/test_op_move_arms_to_carryposture.pdl"); }
    @Test public void race_observe() { captureRACE("RACE-observe",
            "problems/test_op_observe_objects_on_area.pdl"); }

    // RACE method decomposition — from TestRACEDomain.java
    @Test public void race_serve_coffee() { captureRACE("RACE-serve_coffee",
            "problems/test_m_serve_coffee_problem_1.pdl"); }

    // ============================================================
    // TransTerra — matches JUnitTestTransterraDomain exactly
    // ============================================================

    private static final String TT_DOMAIN = "domains/transterra_v1.ddl";

    private void captureTT(String label, String problemFile) {
        capture(label, TT_DOMAIN, problemFile, RACE_VALOH, RACE_MB, true);
    }

    @Test public void tt_move_to() { captureTT("TransTerra-move_to",
            "problems/transterra_problems_v1/test_op_move_to.pdl"); }
    @Test public void tt_sample() { captureTT("TransTerra-sample",
            "problems/transterra_problems_v1/test_op_sample_regolith.pdl"); }
    @Test public void tt_transfer_sample() { captureTT("TransTerra-transfer_sample",
            "problems/transterra_problems_v1/test_op_transfer_sample.pdl"); }
    @Test public void tt_transfer_payload() { captureTT("TransTerra-transfer_payload",
            "problems/transterra_problems_v1/test_op_transfer_payload.pdl"); }
    @Test public void tt_pickup() { captureTT("TransTerra-pickup",
            "problems/transterra_problems_v1/test_op_pickup_basecamp.pdl"); }
    @Test public void tt_pickup2() { captureTT("TransTerra-pickup2",
            "problems/transterra_problems_v1/test_op_pickup_basecamp_2.pdl"); }
    @Test public void tt_place() { captureTT("TransTerra-place",
            "problems/transterra_problems_v1/test_op_place_basecamp.pdl"); }

    // ============================================================
    // Mobipick
    // ============================================================

    private static final String MOBI_DOMAIN =
            "domains/mobipick/simple_navigation/domain.ddl";
    private static final String MOBI_PROBLEM =
            "domains/mobipick/simple_navigation/problem.pdl";

    @Test public void mobipick_simple_nav() {
        capture("Mobipick-simple_nav", MOBI_DOMAIN, MOBI_PROBLEM,
                DWR_VALOH, DWR_MB, true);
    }

    // ============================================================
    // Integer planning
    // ============================================================

    @Test public void integer_simple() {
        capture("Integer-simple",
                "domains/simple_integer_domain.ddl",
                "problems/simple_integer_problem.pdl",
                new UnifyDeepestWeightNewestbindingsValOH(), null, true);
    }

    // ============================================================
    // Core capture
    // ============================================================

    private void capture(String label, String domainFile, String problemFile,
                          CHIMPValOH valOH, LookUpTableDurationEstimator mb,
                          boolean htnUnification) {
        long startNanos = System.nanoTime();
        try {
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .htnUnification(htnUnification);
            if (mb != null) {
                builder.mbEstimator(mb);
            }
            CHIMP chimp = builder.build();

            assertTrue(label + ": plan generation failed", chimp.generatePlan());

            PlannedAction[] plan = chimp.extractActions();
            assertNotNull(label + ": null plan", plan);

            FluentNetworkSolver solver = chimp.getPlanner().getFluentNetworkSolver();
            int fluentCount = solver.getVariables().length;
            int totalConstraints = solver.getConstraints().length;
            int allenConstraints = (solver.getConstraintSolvers() != null
                    && solver.getConstraintSolvers().length > 1)
                    ? solver.getConstraintSolvers()[1].getConstraints().length
                    : 0;
            int planLength = plan.length;
            double elapsedMs = (System.nanoTime() - startNanos) / 1_000_000.0;

            // Write baseline line
            String metricLine = String.format(
                    "%-35s | f=%4d  tc=%4d  ac=%4d  pl=%2d  t=%.0fms",
                    label, fluentCount, totalConstraints, allenConstraints,
                    planLength, elapsedMs);
            out.println(metricLine);

            for (int i = 0; i < plan.length; i++) {
                out.printf("  act[%d] %s%n", i, describeAction(plan[i]));
            }
            out.flush();
            captured++;
            System.out.println("OK  " + metricLine);

        } catch (Exception e) {
            errors++;
            String msg = label + ": " + e.getMessage();
            err.println(msg);
            err.flush();
            System.out.println("ERR " + msg);
            // Re-throw as assertion error so the test fails
            fail(label + " threw: " + e.getMessage());
        }
    }

    private static String describeAction(PlannedAction action) {
        return String.format("%s(%s) t=[%d,%d]-[%d,%d]",
                action.name(),
                String.join(" ", action.arguments()),
                action.est(), action.lst(), action.eet(), action.let());
    }
}
