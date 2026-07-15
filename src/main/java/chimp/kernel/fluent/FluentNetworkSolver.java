package chimp.kernel.fluent;

import java.util.ArrayList;
import java.util.List;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.integer.IntegerConstraint;
import chimp.kernel.integer.IntegerSolver;
import chimp.kernel.integer.IntegerVariable;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.symbolic.CompoundSymbolicSolver;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.kernel.temporal.TemporalSolver;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.TemporalBounds;

/**
 * CHIMP-native fluent network solver — the single ground constraint network
 * that composes symbolic, temporal, and integer sub-solvers.
 *
 * <p>Replaces {@code fluentSolver.FluentNetworkSolver} (which extended
 * {@code MultiConstraintSolver} from Meta-CSP).  Fluent-level constraints
 * are compiled into sub-constraints and routed to the appropriate
 * sub-solver.
 */
public class FluentNetworkSolver extends ConstraintSolver {

    private final CompoundSymbolicSolver symbolicSolver;
    private final TemporalSolver temporalSolver;
    private final IntegerSolver integerSolver;
    private final boolean hasIntegerSolver;
    private final long origin;
    private final long horizon;
    private final int numIntVars;

    /**
     * Create a FluentNetworkSolver without integer variables.
     */
    public FluentNetworkSolver(long origin, long horizon,
                               String[][] symbols, int[] symbolicingredients) {
        this(origin, horizon, symbols, symbolicingredients,
                /* minIntValue */ 0, /* maxIntValue */ 0, /* numIntVars */ 0);
    }

    /**
     * Create a FluentNetworkSolver with internal IntegerVariables.
     *
     * @param origin             Origin for the temporal solver
     * @param horizon            Horizon for the temporal solver
     * @param symbols            Symbol domains per position
     * @param symbolicingredients Number of internal NameVariables per position
     * @param minIntValue        Minimum value for integer variables
     * @param maxIntValue        Maximum value for integer variables
     * @param numIntVars         Number of IntegerVariables per fluent
     */
    public FluentNetworkSolver(long origin, long horizon,
                               String[][] symbols, int[] symbolicingredients,
                               int minIntValue, int maxIntValue, int numIntVars) {
        super();
        this.origin = origin;
        this.horizon = horizon;
        this.numIntVars = numIntVars;
        this.hasIntegerSolver = numIntVars > 0;

        // Build sub-solvers
        this.symbolicSolver = new CompoundSymbolicSolver(symbols, symbolicingredients);
        // Reserve enough time points: 1 origin + 2 per fluent (start/end)
        this.temporalSolver = new TemporalSolver(origin, horizon, 2000);
        if (hasIntegerSolver) {
            this.integerSolver = new IntegerSolver(minIntValue, maxIntValue);
        } else {
            this.integerSolver = null;
        }
    }

    // -- fluent creation --

    /**
     * Creates {@code num} fluents, each with one symbolic variable,
     * one temporal variable, and (if configured) N integer variables.
     */
    public Fluent[] createVariables(int num) {
        Fluent[] fluents = new Fluent[num];
        for (int i = 0; i < num; i++) {
            int id = nextVariableId();
            CompoundSymbolicVariable sv =
                    (CompoundSymbolicVariable) symbolicSolver.createVariables(1)[0];
            TemporalVariable tv =
                    (TemporalVariable) temporalSolver.createVariables(1)[0];
            IntegerVariable[] ivs;
            if (hasIntegerSolver) {
                ivs = (IntegerVariable[]) integerSolver.createVariables(numIntVars);
            } else {
                ivs = new IntegerVariable[0];
            }
            Fluent f = new Fluent(this, id, sv, tv, ivs);
            registerVariable(f);
            fluents[i] = f;
        }
        return fluents;
    }

    // -- constraint management --

    @Override
    public boolean addConstraint(Constraint c) {
        if (c instanceof TemporalConstraint tc) {
            // Direct temporal constraint (e.g., from ordering)
            return temporalSolver.addConstraint(tc);
        }

        if (!(c instanceof FluentConstraint fc)) {
            return super.addConstraint(c);
        }

        Variable from = fc.from();
        Variable to = fc.to();
        if (from == null || to == null) return false;

        Constraint[] subs = fc.compileToSubConstraints(from, to);
        for (Constraint sub : subs) {
            boolean ok = dispatch(sub);
            if (!ok) {
                // Rollback previously added subs
                // (In practice, conflict detection happens before constraint addition;
                //  incomplete rollback mirrors the Meta-CSP behaviour)
                return false;
            }
        }
        constraintList().add(c);
        return true;
    }

    /** Route a sub-constraint to the right sub-solver. */
    private boolean dispatch(Constraint sub) {
        if (sub instanceof TemporalConstraint tc) {
            return temporalSolver.addConstraint(tc);
        } else if (sub instanceof CompoundSymbolicConstraint csc) {
            return symbolicSolver.addConstraint(csc);
        } else if (integerSolver != null && sub instanceof IntegerConstraint ic) {
            return integerSolver.addConstraint(ic);
        }
        return false;
    }

    @Override
    public boolean propagate() {
        // Delegated strategy: each sub-solver propagates on addConstraint.
        return true;
    }

    // -- queries --

    /** @return All fluents with the OPEN marking. */
    public Fluent[] getOpenFluents() {
        List<Fluent> ret = new ArrayList<>();
        for (Variable var : getVariables()) {
            if (var.marking() == 3 /* OPEN */) {
                ret.add((Fluent) var);
            }
        }
        return ret.toArray(new Fluent[0]);
    }

    /**
     * @param taskTemporal The temporal variable of the task.
     * @return All fluents that are (possibly temporally) active during
     *         the task's interval.
     */
    public Fluent[] getOpenFluents(TemporalVariable taskTemporal) {
        long taskLST = taskTemporal.getLST();
        List<Fluent> ret = new ArrayList<>();
        for (Variable var : getVariables()) {
            Fluent f = (Fluent) var;
            TemporalVariable vi = f.getTemporalVariable();
            if (vi.getEST() < taskLST) {
                TemporalBounds b = temporalSolver.getAdmissibleDistanceBounds(
                        taskTemporal.getStart(), vi.getEnd());
                if (b != null && b.maximum() > -1) {
                    ret.add(f);
                }
            }
        }
        return ret.toArray(new Fluent[0]);
    }

    /**
     * @return All constraints whose second scope variable is {@code to}.
     */
    public List<Constraint> getConstraintsTo(Variable to) {
        List<Constraint> ret = new ArrayList<>();
        for (Constraint con : getConstraints()) {
            if (con.scope().length == 2 && con.scope()[1].equals(to)) {
                ret.add(con);
            }
        }
        return ret;
    }

    /**
     * @return All FluentConstraints of the given type whose second scope
     *         variable is {@code to}.
     */
    public List<FluentConstraint> getFluentConstraintsOfTypeTo(
            Variable to, FluentConstraint.Type type) {
        List<FluentConstraint> ret = new ArrayList<>();
        for (Constraint con : getConstraints()) {
            if (con.scope().length == 2 && con.scope()[1].equals(to)) {
                if (con instanceof FluentConstraint fc && fc.getType() == type) {
                    ret.add(fc);
                }
            }
        }
        return ret;
    }

    /**
     * @return All FluentConstraints of the given type whose first scope
     *         variable is {@code from}.
     */
    public List<FluentConstraint> getFluentConstraintsOfTypeFrom(
            Variable from, FluentConstraint.Type type) {
        List<FluentConstraint> ret = new ArrayList<>();
        for (Constraint con : getConstraints()) {
            if (con.scope().length == 2 && con.scope()[0].equals(from)) {
                if (con instanceof FluentConstraint fc && fc.getType() == type) {
                    ret.add(fc);
                }
            }
        }
        return ret;
    }

    // -- accessors --

    public CompoundSymbolicSolver getSymbolicSolver() { return symbolicSolver; }
    public TemporalSolver getTemporalSolver() { return temporalSolver; }
    public IntegerSolver getIntegerSolver() { return integerSolver; }

    public long getOrigin() { return origin; }
    public long getHorizon() { return horizon; }
    public boolean hasIntegerConstraintSolver() { return hasIntegerSolver; }
    public int getNumIntVars() { return numIntVars; }
    public int getIntegerVariableStartIndex() { return numIntVars; } // legacy
}
