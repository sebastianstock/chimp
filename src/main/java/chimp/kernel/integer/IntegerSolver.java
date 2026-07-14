package chimp.kernel.integer;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.logging.ChimpLogging;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CHIMP-native integer constraint solver backed by Choco.
 * Port of {@code integers.IntegerConstraintSolver}.
 */
public class IntegerSolver extends ConstraintSolver {

    private static final Logger logger = ChimpLogging.getLogger(IntegerSolver.class);

    private final int minIntValue;
    private final int maxIntValue;

    public IntegerSolver(int minIntValue, int maxIntValue) {
        this.minIntValue = minIntValue;
        this.maxIntValue = maxIntValue;
    }

    @Override
    public boolean propagate() {
        Map<Integer, IntVar> idToIntVarsMap = new HashMap<>();
        Model model = new Model();

        for (Constraint con : this.getConstraints()) {
            IntegerConstraint ic = (IntegerConstraint) con;

            if (isAllDifferentOrAllEqual(ic.getType())) {
                postAllDifferentOrAllEqualConstraint(model, idToIntVarsMap, ic);
            } else if (ic.getType() == IntegerConstraint.Type.ARITHM) {
                postArithmConstraint(model, idToIntVarsMap, ic);
            } else {
                logger.warning("Constraint " + ic.toString() + " is not implemented");
            }
        }

        Solver chocoSolver = model.getSolver();
        Solution solution = chocoSolver.findSolution();

        if (solution != null) {
            updateDomains(idToIntVarsMap);
            return solution.exists();
        } else {
            logger.info("Could not find a solution");
            return false;
        }
    }

    /** Creates {@code num} new integer variables. */
    public IntegerVariable[] createVariables(int num) {
        IntegerVariable[] ret = new IntegerVariable[num];
        for (int i = 0; i < num; i++) {
            ret[i] = new IntegerVariable(this, nextVariableId());
            registerVariable(ret[i]);
        }
        return ret;
    }

    // -- Choco constraint posting (verbatim from Meta-CSP version) --

    private boolean isAllDifferentOrAllEqual(IntegerConstraint.Type type) {
        return type == IntegerConstraint.Type.ALLDIFFERENT || type == IntegerConstraint.Type.ALLEQUAL;
    }

    private void checkArithmConstraintArgs(IntegerConstraint ic) {
        if (ic.getType() != IntegerConstraint.Type.ARITHM) {
            throw new IllegalArgumentException("Expected constraint of type ARITHM");
        }
        if (ic.getOp1() == null) {
            throw new IllegalArgumentException("Op1 of ARITHM constraint must not be null");
        }
        int scopeLength = ic.getScope().length;
        if (scopeLength == 0) {
            throw new IllegalArgumentException("Scope of ARITHM constraint must be > 0");
        } else if (scopeLength > 3) {
            throw new IllegalArgumentException("Scope of ARITHM with two operators must not be > 3");
        }
        if (ic.getOp2() != null && scopeLength < 2) {
            throw new IllegalArgumentException("Scope of ARITHM with two operators must be >= 2");
        }
    }

    private void postArithmConstraint(Model model, Map<Integer, IntVar> idToIntVarsMap,
                                      IntegerConstraint ic) {
        checkArithmConstraintArgs(ic);

        IntVar[] intVars = intVarsFromIntegerVariables(ic.getScope(), idToIntVarsMap, model);

        if (ic.getOp2() == null) {
            if (intVars.length == 1) {
                model.arithm(intVars[0], ic.getOp1(), ic.getCste()).post();
            } else if (intVars.length == 2) {
                model.arithm(intVars[0], ic.getOp1(), intVars[1]).post();
            } else {
                throw new IllegalArgumentException("Scope of ARITHM not valid: scope.length=" + intVars.length);
            }
        } else {
            if (intVars.length == 2) {
                model.arithm(intVars[0], ic.getOp1(), intVars[1], ic.getOp2(), ic.getCste()).post();
            } else if (intVars.length == 3) {
                model.arithm(intVars[0], ic.getOp1(), intVars[1], ic.getOp2(), intVars[2]).post();
            }
        }
    }

    private IntVar[] intVarsFromIntegerVariables(Variable[] vars, Map<Integer, IntVar> idToIntVarsMap, Model model) {
        IntVar[] intVars = new IntVar[vars.length];
        for (int i = 0; i < vars.length; i++) {
            IntegerVariable integerVariable = (IntegerVariable) vars[i];
            intVars[i] = idToIntVarsMap.get(integerVariable.getID());
            if (intVars[i] == null) {
                if (integerVariable.isConstant()) {
                    IntegerDomain iDomain = integerVariable.getDomain();
                    intVars[i] = model.intVar("i" + integerVariable.getID(), iDomain.getValue());
                } else {
                    intVars[i] = model.intVar("i" + integerVariable.getID(),
                            minIntValue, maxIntValue, false);
                }
                idToIntVarsMap.put(integerVariable.getID(), intVars[i]);
            }
        }
        return intVars;
    }

    private void postAllDifferentOrAllEqualConstraint(Model model, Map<Integer, IntVar> idToIntVarsMap,
                                                      IntegerConstraint ic) {
        if (!isAllDifferentOrAllEqual(ic.getType())) {
            throw new IllegalArgumentException("Expected constraint of type AllDIFFERENT or ALLEQUAL");
        }

        IntVar[] intVars = intVarsFromIntegerVariables(ic.getScope(), idToIntVarsMap, model);

        if (ic.getType() == IntegerConstraint.Type.ALLDIFFERENT) {
            model.allDifferent(intVars).post();
        } else {
            model.allEqual(intVars).post();
        }
    }

    private void updateDomains(Map<Integer, IntVar> idToIntVarsMap) {
        for (Variable v : getVariables()) {
            IntegerVariable integerVariable = (IntegerVariable) v;
            IntVar intVar = idToIntVarsMap.get(v.getID());
            if (intVar != null) {
                integerVariable.setValue(intVar.getValue());
            } else {
                IntegerDomain integerDomain = integerVariable.getDomain();
                if (!integerDomain.isConstant()) {
                    integerDomain.uninstantiate();
                }
            }
        }
    }
}
