package examples;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import org.metacsp.framework.Constraint;
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
        public String type;
        public int id;
        public long est;
        public long lst;
        public long eet;
        public long let;
        public FluentStruct[] preconditions;

        public FluentStruct(Fluent fluent) {
            name = fluent.getName();
            type = fluent.getTypeStr();
            id = fluent.getID();
            AllenInterval ai = fluent.getAllenInterval();
            est = ai.getEST();
            lst = ai.getLST();
            eet = ai.getEET();
            let = ai.getLET();
            preconditions = null;
        }
    }

    public static class FluentConstraintStruct {

        public int fromId;
        public int toId;
        public String type;
        public boolean negativeEffect;

        public FluentConstraintStruct(FluentConstraint fc) {
            fromId = fc.getFrom().getID();
            toId = fc.getTo().getID();
            type = fc.getType().toString();
            negativeEffect = fc.isNegativeEffect();
        }
    }

    public static class PlanResult {
        public FluentStruct[] fluents;
        public FluentConstraintStruct[] fluentConstraints;
        public FluentStruct[] allFluents;
        public boolean foundPlan = true;
    }

    public PlanResult plan(String problemFile, String domainFile) {

        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();
        CHIMP chimp;
        try {
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
                    .valHeuristic(valOH)
                    .htnUnification(USE_HTN_UNIFICATION)
                    .guessOrdering(GUESS_ORDERING);
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
        result.fluents = extractActions(planVector, chimp.getFluentSolver());

        result.fluentConstraints = extractConstraints(chimp.getFluentSolver());
        result.allFluents = convertFluentsToFluentStructs(chimp.getFluentSolver().getVariables());

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

    private static FluentStruct[] extractActions(Variable[] planVector, FluentNetworkSolver fluentSolver) {
        FluentStruct[] result = new FluentStruct[planVector.length];
        for (int i = 0; i < planVector.length; ++i) {
            Variable fl = planVector[i];

            FluentStruct operation = new FluentStruct((Fluent) fl);

            // get the preconditions of this fluent
            List<FluentConstraint> preConstraints =
                    fluentSolver.getFluentConstraintsOfTypeTo(fl, FluentConstraint.Type.PRE);
            List<FluentStruct> preFluents = new ArrayList<FluentStruct>();
            for (FluentConstraint constraint : preConstraints)
            {
                preFluents.add(new FluentStruct((Fluent) constraint.getFrom()));
            }
            operation.preconditions = preFluents.toArray(new FluentStruct[preFluents.size()]);
            result[i] = operation;
        }
        return result;
    }

    private static FluentConstraintStruct[] extractConstraints(FluentNetworkSolver fluentSolver) {
        List<FluentConstraintStruct> fluentConstraints = new ArrayList<>();

        for (Constraint con : fluentSolver.getConstraints()) {
            if (con instanceof FluentConstraint) {
                fluentConstraints.add(new FluentConstraintStruct((FluentConstraint) con));
            }
        }
        return fluentConstraints.toArray(new FluentConstraintStruct[fluentConstraints.size()]);
    }

    private static FluentStruct[] convertFluentsToFluentStructs(Variable[] vars) {
        FluentStruct[] fluentStructs = new FluentStruct[vars.length];
        for (int i = 0; i < vars.length; i++) {
            try {
                fluentStructs[i] = new FluentStruct((Fluent) vars[i]);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Variables that shall be converted to FluentStructs must be Fluents");
            }
        }
        return  fluentStructs;
    }

}
