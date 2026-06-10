package postprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;
import org.metacsp.framework.Variable;

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

        Variable[] actions = chimp.extractActions();
        String expected = String.join(System.lineSeparator(),
                "- id: " + actions[0].getID(),
                "  name: \"!move_torso\"",
                "  arguments: [\"TorsoDownPosture\"]",
                "  time: [[1, 1], [4001, 3599997]]",
                "- id: " + actions[1].getID(),
                "  name: \"!tuck_arms\"",
                "  arguments: [\"ArmTuckedPosture\", \"ArmTuckedPosture\"]",
                "  time: [[1, 3595997], [4001, 3599997]]",
                "- id: " + actions[2].getID(),
                "  name: \"!move_base\"",
                "  arguments: [\"preManipulationAreaNorthTable1\"]",
                "  time: [[4002, 3599998], [4004, 3600000]]",
                "");
        assertEquals(expected, writer.toString());
        assertTrue(writer.toString().indexOf("!move_torso") < writer.toString().indexOf("!tuck_arms"));
    }

    private CHIMP plan() throws DomainParsingException {
        CHIMP chimp = new CHIMP.CHIMPBuilder(DOMAIN, PROBLEM)
                .horizon(3600000)
                .valHeuristic(new UnifyDeepestWeightNewestbindingsValOH())
                .build();
        assertTrue(chimp.generatePlan());
        return chimp;
    }
}
