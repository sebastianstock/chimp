package chimp.kernel.fluent;

import java.util.ArrayList;
import java.util.List;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
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
    private final ConstraintNetwork network;
    private final ConstraintSolver[] subSolvers;

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
        this.network = new ConstraintNetwork();
        this.subSolvers = buildSubSolverArray();
    }

    private ConstraintSolver[] buildSubSolverArray() {
        List<ConstraintSolver> list = new ArrayList<>();
        list.add(symbolicSolver);
        list.add(temporalSolver);
        if (hasIntegerSolver) list.add(integerSolver);
        return list.toArray(new ConstraintSolver[0]);
    }

    // -- fluent creation --

    /** Creates a single fluent with the default component (State). */
    public Fluent createVariable() {
        return createVariable(Fluent.STATE_TYPE_STR);
    }

    /** Creates a single fluent with the given component type. */
    public Fluent createVariable(String component) {
        Fluent f = createVariables(1)[0];
        f.setComponent(component);
        return f;
    }

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

    /** Overridden to also add to the constraint network. */
    @Override
    protected void registerVariable(Variable v) {
        super.registerVariable(v);
        network.addVariable(v);
    }

    // -- constraint management --

    @Override
    public boolean addConstraints(Constraint[] cs) {
        List<Constraint> added = new ArrayList<>();
        for (Constraint c : cs) {
            if (!addConstraint(c)) {
                for (Constraint a : added) {
                    removeConstraint(a);
                }
                return false;
            }
            added.add(c);
        }
        return true;
    }

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
        List<Constraint> addedSubs = new ArrayList<>();
        for (Constraint sub : subs) {
            if (!dispatch(sub)) {
                // Roll back the subs added earlier in this batch
                for (Constraint addedSub : addedSubs) {
                    dispatchRemove(addedSub);
                }
                return false;
            }
            addedSubs.add(sub);
        }
        constraintList().add(c);
        network.addConstraint(c);
        return true;
    }

    // -- variable removal --

    /** Overridden to also remove compiled sub-constraints from sub-solvers
     *  and from the live constraint network. Batches sub-solver removals
     *  to avoid redundant STN rebuilds. */
    @Override
    public void removeConstraints(Constraint[] cs) {
        // Collect sub-constraints per sub-solver for batch removal
        List<Constraint> temporalToRemove = new ArrayList<>();
        List<Constraint> symbolicToRemove = new ArrayList<>();
        List<Constraint> integerToRemove = new ArrayList<>();
        for (Constraint c : cs) {
            if (c instanceof FluentConstraint fc) {
                Constraint[] subs = fc.getCompiledSubConstraints();
                if (subs != null) {
                    for (Constraint sub : subs) {
                        if (sub instanceof TemporalConstraint) temporalToRemove.add(sub);
                        else if (sub instanceof CompoundSymbolicConstraint) symbolicToRemove.add(sub);
                        else if (integerSolver != null && sub instanceof IntegerConstraint) integerToRemove.add(sub);
                    }
                }
            }
            super.removeConstraint(c);
            network.removeConstraint(c);
        }
        // Batch-remove from sub-solvers (TemporalSolver.removeConstraints rebuilds STN once)
        if (!temporalToRemove.isEmpty())
            temporalSolver.removeConstraints(temporalToRemove.toArray(new Constraint[0]));
        if (!symbolicToRemove.isEmpty())
            symbolicSolver.removeConstraints(symbolicToRemove.toArray(new Constraint[0]));
        if (integerSolver != null && !integerToRemove.isEmpty())
            integerSolver.removeConstraints(integerToRemove.toArray(new Constraint[0]));
    }

    /** Overridden to also remove compiled sub-constraints from sub-solvers
     *  and from the live constraint network. */
    @Override
    public void removeConstraint(Constraint c) {
        if (c instanceof FluentConstraint fc) {
            Constraint[] subs = fc.getCompiledSubConstraints();
            if (subs != null) {
                for (Constraint sub : subs) {
                    dispatchRemove(sub);
                }
            }
        }
        super.removeConstraint(c);
        network.removeConstraint(c);
    }

    /** Removes a sub-constraint from the appropriate sub-solver. */
    private void dispatchRemove(Constraint sub) {
        if (sub instanceof TemporalConstraint tc) {
            temporalSolver.removeConstraint(tc);
        } else if (sub instanceof CompoundSymbolicConstraint csc) {
            symbolicSolver.removeConstraint(csc);
        } else if (integerSolver != null && sub instanceof IntegerConstraint ic) {
            integerSolver.removeConstraint(ic);
        }
    }

    /** Removes variables and all their associated constraints,
     *  including sub-variables from sub-solvers. Batches sub-solver
     *  removals to avoid redundant STN rebuilds. */
    public void removeVariables(Variable... vars) {
        // Collect sub-constraints per sub-solver for batch removal
        List<Constraint> temporalToRemove = new ArrayList<>();
        List<Constraint> symbolicToRemove = new ArrayList<>();
        List<Constraint> integerToRemove = new ArrayList<>();

        for (Variable v : vars) {
            // Remove FluentConstraints involving this variable
            List<Constraint> toRemove = new ArrayList<>();
            for (Constraint c : getConstraints()) {
                for (Variable scopeVar : c.scope()) {
                    if (scopeVar.equals(v)) {
                        toRemove.add(c);
                        break;
                    }
                }
            }
            for (Constraint c : toRemove) {
                if (c instanceof FluentConstraint fc) {
                    Constraint[] subs = fc.getCompiledSubConstraints();
                    if (subs != null) {
                        for (Constraint sub : subs) {
                            if (sub instanceof TemporalConstraint) temporalToRemove.add(sub);
                            else if (sub instanceof CompoundSymbolicConstraint) symbolicToRemove.add(sub);
                            else if (integerSolver != null && sub instanceof IntegerConstraint) integerToRemove.add(sub);
                        }
                    }
                }
                super.removeConstraint(c);
                network.removeConstraint(c);
            }
            // Also remove sub-variables from sub-solvers
            if (v instanceof Fluent f) {
                temporalSolver.removeVariable(f.getTemporalVariable());
                symbolicSolver.removeVariable(f.getCompoundSymbolicVariable());
                if (integerSolver != null) {
                    for (IntegerVariable iv : f.getIntegerVariables()) {
                        integerSolver.removeVariable(iv);
                    }
                }
            }
            unregisterVariable(v);
        }

        // Batch-remove sub-constraints from sub-solvers (rebuilds STN once)
        if (!temporalToRemove.isEmpty())
            temporalSolver.removeConstraints(temporalToRemove.toArray(new Constraint[0]));
        if (!symbolicToRemove.isEmpty())
            symbolicSolver.removeConstraints(symbolicToRemove.toArray(new Constraint[0]));
        if (integerSolver != null && !integerToRemove.isEmpty())
            integerSolver.removeConstraints(integerToRemove.toArray(new Constraint[0]));
    }

    // -- variable access --

    /** Returns variables whose {@link Variable#component()} matches. */
    public Variable[] getVariables(String component) {
        List<Variable> result = new ArrayList<>();
        for (Variable v : getVariables()) {
            if (component.equals(v.component())) {
                result.add(v);
            }
        }
        return result.toArray(new Variable[0]);
    }

    // -- network access --

    /** Returns the constraint network representing this solver's state. */
    public ConstraintNetwork getConstraintNetwork() {
        return network;
    }

    /** Returns the sub-solvers (symbolic, temporal, integer). */
    public ConstraintSolver[] getConstraintSolvers() {
        return subSolvers;
    }

    /** Propagates all sub-solvers (delegated strategy: already done on add). */
    public void propagateAllSub() {
        // Delegated strategy: sub-solvers propagate on addConstraint.
        // This method exists for Meta-CSP compatibility.
    }

    /** Returns a human-readable description of this solver. */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("FluentNetworkSolver[fluents=").append(getVariables().length);
        sb.append(", constrs=").append(getConstraints().length);
        sb.append(", origin=").append(origin).append(", horizon=").append(horizon);
        sb.append("]");
        return sb.toString();
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
