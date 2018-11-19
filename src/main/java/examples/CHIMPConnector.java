package examples;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;

import org.metacsp.multi.allenInterval.AllenInterval;
import planner.CHIMP;

import java.util.ArrayList;
import java.util.List;
// import java.util.logging.Level;

/**
 * A simple wrapper that makes calling CHIMP via JNI more convenient.
 *
 * @author Sebastian Stock
 *
 */
public class CHIMPConnector {

    static final boolean GUESS_ORDERING = false;
    static final boolean USE_HTN_UNIFICATION = true;
    static  final boolean PRINT_PLAN = true;

    /**
     * Simple collection of the relative properties of a fluent to simplify information exchange via JNI.
     * Note:
     * In the PlanResult, the FluentStruct[] "fluents" contains only the operations to be executed,
     * while each operation uses the FluentStruct[] "preconditions" to list only the symbolic
     * preconditions (like On(Object1 Table1)), but does not reference other operations that are to
     * be executed before this one.
     */
    public static class FluentStruct {
        public String name;
        public String type = "action";
        public int id;
        public long est;
        public long lst;
        public long eet;
        public long let;
        public FluentStruct[] preconditions;

        public FluentStruct(Fluent fluent) {
            name = fluent.getName();
            id = fluent.getID();
            AllenInterval ai = fluent.getAllenInterval();
            est = ai.getEST();
            lst = ai.getLST();
            eet = ai.getEET();
            let = ai.getLET();
            preconditions = null;
        }
    }

    public static class PlanResult {
        public FluentStruct[] fluents;
        public boolean foundPlan = true;
    }

    public PlanResult plan(String problemFile, String domainFile) {

        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                .valHeuristic(valOH)
                .htnUnification(USE_HTN_UNIFICATION)
                .guessOrdering(GUESS_ORDERING);

        CHIMP chimp;
        try {
            chimp = builder.build();
        } catch (DomainParsingException e) {
            e.printStackTrace();
            PlanResult ret = new PlanResult();
            ret.foundPlan = false;
            return ret;
        }

        PlanResult result = new PlanResult();
        result.foundPlan = chimp.generatePlan();
        System.out.println("Found plan? " + result.foundPlan);
        chimp.printStats(System.out);

        Variable[] planVector = chimp.extractActions();
        List<FluentStruct> taskList = new ArrayList<FluentStruct>();
        for (int i = 0; i < planVector.length; ++i) {
            Variable fl = planVector[i];
            if (fl.getComponent() != null) {
                FluentStruct operation = new FluentStruct((Fluent) fl);

                // get the preconditions of this fluent
                List<FluentConstraint> preConstraints =
                    chimp.getFluentSolver()
                         .getFluentConstraintsOfTypeTo(fl, FluentConstraint.Type.PRE);
                List<FluentStruct> preFluents = new ArrayList<FluentStruct>();
                for (FluentConstraint constraint : preConstraints)
                {
                    // Todo: check with fluent.getComponent() != null?
                    preFluents.add(new FluentStruct((Fluent) constraint.getFrom()));
                }
                operation.preconditions = preFluents.toArray(new FluentStruct[preFluents.size()]);
                taskList.add(operation);
            }
        }
        result.fluents = taskList.toArray(new FluentStruct[taskList.size()]);

        if (PRINT_PLAN) {
            int c = 0;
            for (Variable act : planVector) {
                if (act.getComponent() != null)
                    System.out.println(c++ +".\t" + act);
            }
//            chimp.printFullPlan();
        }

        return result;
    }

}
