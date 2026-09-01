package postprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import chimp.model.PlannedAction;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class PlanExtractorSnapshotTest {

    private static final String DOMAIN = "domains/ordered_domain.ddl";
    private static final String PROBLEM = "problems/test_m_drive_robot_1.pdl";

    @Test
    public void preservesRosPlanOutput() throws Exception {
        CHIMP chimp = plan();
        PlanExtractor extractor = new PlanExtractor(chimp.getFluentSolver());
        StringWriter writer = new StringWriter();

        extractor.writeROSPlanFormat(writer);

        assertEquals(String.join(System.lineSeparator(),
                "; Actions:",
                "0.001: (!move_torso TorsoDownPosture) [4.000]",
                "0.001: (!tuck_arms ArmTuckedPosture ArmTuckedPosture) [4.000]",
                "4.002: (!move_base preManipulationAreaNorthTable1) [0.002]",
                ""), writer.toString());
    }

    @Test
    public void preservesYamlActionOutputAndEqualStartOrdering() throws Exception {
        CHIMP chimp = plan();
        PlanExtractor extractor = new PlanExtractor(chimp.getFluentSolver());
        StringWriter writer = new StringWriter();

        extractor.writeActions(writer);

        PlannedAction[] actions = chimp.extractActions();
        String expected = String.join(System.lineSeparator(),
                "- id: " + actions[0].id(),
                "  name: \"!move_torso\"",
                "  arguments: [\"TorsoDownPosture\"]",
                "  time: [[1, 1], [4001, 3599997]]",
                "- id: " + actions[1].id(),
                "  name: \"!tuck_arms\"",
                "  arguments: [\"ArmTuckedPosture\", \"ArmTuckedPosture\"]",
                "  time: [[1, 3595997], [4001, 3599997]]",
                "- id: " + actions[2].id(),
                "  name: \"!move_base\"",
                "  arguments: [\"preManipulationAreaNorthTable1\"]",
                "  time: [[4002, 3599998], [4004, 3600000]]",
                "");
        assertEquals(expected, writer.toString());
        assertTrue(writer.toString().indexOf("!move_torso") < writer.toString().indexOf("!tuck_arms"));
    }

    @Test
    public void extractsServeCoffeePlanActivitiesDespiteUngroundedFluentArgs() throws Exception {
        CHIMP chimp = plan(DOMAIN, "problems/test_m_serve_coffee_problem_1.pdl");
        PlanExtractor extractor = new PlanExtractor(chimp.getFluentSolver());
        StringWriter writer = new StringWriter();

        extractor.writeActions(writer);

        // The YAML activity view must cover the same actions as the public
        // extractActions() API, including fluents whose arguments are not
        // fully grounded (arguments fall back to the first domain symbol).
        String[] lines = writer.toString().split(System.lineSeparator());
        long entryCount = Arrays.stream(lines).filter(l -> l.startsWith("- id:")).count();
        assertEquals(chimp.extractActions().length, entryCount);
    }

    private CHIMP plan() throws DomainParsingException {
        return plan(DOMAIN, PROBLEM);
    }

    private CHIMP plan(String domain, String problem) throws DomainParsingException {
        CHIMP chimp = new CHIMP.CHIMPBuilder(domain, problem)
                .horizon(3600000)
                .valHeuristic(new UnifyDeepestWeightNewestbindingsValOH())
                .build();
        assertTrue(chimp.generatePlan());
        return chimp;
    }
}
