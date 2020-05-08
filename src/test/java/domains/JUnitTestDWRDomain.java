package domains;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metacsp.framework.ValueOrderingH;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class JUnitTestDWRDomain {

    private String domainFile = "domains/dwr/dwr.ddl";

    private void testPlanning(String problemFile) {
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

        try {
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
                    .htnUnification(true);
            builder.htnUnification(true);
            CHIMP chimp;
            chimp = builder.build();
            assertTrue(chimp.generatePlan());
        } catch (DomainParsingException e) {
            e.printStackTrace();
            return;
        }
    }

    @Test
    public void test1() {
        testPlanning("domains/dwr/test/test_op_leave.pdl");
    }

    @Test
    public void test2() {
        testPlanning("domains/dwr/test/test_op_enter.pdl");
    }

    @Test
    public void test3() {
        testPlanning("domains/dwr/test/test_op_move.pdl");
    }

    @Test
    public void test4() {
        testPlanning("domains/dwr/test/test_op_stack.pdl");
    }

    @Test
    public void test5() {
        testPlanning("domains/dwr/test/test_op_unstack.pdl");
    }

    @Test
    public void test6() {
        testPlanning("domains/dwr/test/test_op_put.pdl");
    }

    @Test
    public void test7() {
        testPlanning("domains/dwr/test/test_op_take.pdl");
    }

    @Test
    public void test8() {
        testPlanning("domains/dwr/test/test_m_load.pdl");
    }

    @Test
    public void test9() {
        testPlanning("domains/dwr/test/test_m_unload.pdl");
    }

    @Test
    public void test10() {
        testPlanning("domains/dwr/test/test_m_uncover0.pdl");
    }

    @Test
    public void test11() {
        testPlanning("domains/dwr/test/test_m_uncover0.pdl");
    }

    @Test
    public void test12() {
        testPlanning("domains/dwr/test/test_m_uncover1.pdl");
    }

    @Test
    public void test13() {
        testPlanning("domains/dwr/test/test_m_navigate0.pdl");
    }

    @Test
    public void test14() {
        testPlanning("domains/dwr/test/test_m_navigate1.pdl");
    }

    @Test
    public void test15() {
        testPlanning("domains/dwr/test/test_m_goto0.pdl");
    }

    @Test
    public void test16() {
        testPlanning("domains/dwr/test/test_m_goto1.pdl");
    }

    @Test
    public void test17() {
        testPlanning("domains/dwr/test/test_m_bring0.pdl");
    }

    @Test
    public void test18() {
        testPlanning("domains/dwr/test/test_m_bring1.pdl");
    }

    @Test
    public void test19() {
        testPlanning("domains/dwr/test/test_m_bring2.pdl");
    }

}
