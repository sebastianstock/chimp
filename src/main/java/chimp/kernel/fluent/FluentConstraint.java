package chimp.kernel.fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import chimp.kernel.Constraint;
import chimp.kernel.Variable;
import chimp.kernel.integer.IntegerVariable;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.kernel.temporal.TemporalVariable;
import chimp.kernel.symbolic.CompoundSymbolicConstraint;
import chimp.kernel.symbolic.CompoundSymbolicVariable;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import htn.AdditionalConstraintTemplate;
import htn.PlanReportroryItem;
import resourceFluent.ResourceUsageTemplate;

/**
 * CHIMP-native fluent constraint — a binary constraint between two
 * {@link Fluent}s.
 *
 * <p>Replaces {@code fluentSolver.FluentConstraint} (which extended
 * {@code MultiBinaryConstraint} from Meta-CSP).  The constraint type
 * determines which sub-constraints (temporal, symbolic, integer) are
 * compiled and routed to the corresponding sub-solvers.
 */
public class FluentConstraint extends Constraint {

    public enum Type {
        MATCHES, DC, PRE, OPENS, CLOSES, BEFORE, UNARYAPPLIED, MOVEDURATION,
        AXIOM, RESOURCEUSAGE
    }

    private final Type type;
    private int[] connections;
    private PlanReportroryItem plannedWith;
    private boolean isNegativeEffect;
    private TemporalBounds bounds;
    private String axiom;
    private ResourceUsageTemplate resourceIndicator;
    private Vector<AdditionalConstraintTemplate> additionalConstraints;
    private int depth;
    private boolean depthIsSet;

    public FluentConstraint(Type type) {
        this.type = type;
    }

    public FluentConstraint(Type type, int[] connections) {
        this(type);
        this.connections = connections;
    }

    public FluentConstraint(Type type, PlanReportroryItem plannedWith) {
        this(type);
        this.plannedWith = plannedWith;
    }

    public FluentConstraint(Type type, TemporalBounds bounds) {
        this(type);
        this.bounds = bounds;
    }

    public FluentConstraint(Type type, String axiom) {
        this(type);
        this.axiom = axiom;
    }

    public FluentConstraint(Type type, ResourceUsageTemplate resourceIndicator) {
        this(type);
        this.resourceIndicator = resourceIndicator;
    }

    /**
     * Compiles this fluent constraint into CHIMP-native sub-constraints
     * (temporal, symbolic, integer) that are added to the sub-solvers.
     */
    public Constraint[] compileToSubConstraints(Variable f, Variable t) {
        if (!(f instanceof Fluent) || !(t instanceof Fluent)) return new Constraint[0];

        List<Constraint> retList = new ArrayList<>();

        if (this.type.equals(Type.MATCHES)) {
            // Temporal: equals
            TemporalConstraint eq = new TemporalConstraint(TemporalRelation.Equals);
            eq.setFrom(((Fluent) f).getTemporalVariable());
            eq.setTo(((Fluent) t).getTemporalVariable());
            retList.add(eq);

            // Symbolic: matches
            CompoundSymbolicConstraint ncon =
                    new CompoundSymbolicConstraint(CompoundSymbolicConstraint.Type.MATCHES);
            ncon.setScope(new Variable[] {
                    ((Fluent) f).getCompoundSymbolicVariable(),
                    ((Fluent) t).getCompoundSymbolicVariable() });
            retList.add(ncon);

        } else if (this.type.equals(Type.DC)) {
            if (additionalConstraints != null && !additionalConstraints.isEmpty()) {
                for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
                    TemporalConstraint con = toTemporalConstraint(aConTemplate.getConstraint());
                    if (aConTemplate.startsFromHead()) {
                        con.setFrom(((Fluent) f).getTemporalVariable());
                        con.setTo(((Fluent) t).getTemporalVariable());
                    } else {
                        con.setFrom(((Fluent) t).getTemporalVariable());
                        con.setTo(((Fluent) f).getTemporalVariable());
                    }
                    retList.add(con);
                }
            } else {
                TemporalConstraint desf = new TemporalConstraint(
                        TemporalRelation.DuringOrEqualsOrStartsOrFinishes);
                desf.setFrom(((Fluent) t).getTemporalVariable());
                desf.setTo(((Fluent) f).getTemporalVariable());
                retList.add(desf);
            }

        } else if (this.type.equals(Type.PRE)) {
            if (additionalConstraints != null && !additionalConstraints.isEmpty()) {
                for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
                    TemporalConstraint con = toTemporalConstraint(aConTemplate.getConstraint());
                    if (aConTemplate.startsFromHead()) {
                        con.setFrom(((Fluent) t).getTemporalVariable());
                        con.setTo(((Fluent) f).getTemporalVariable());
                    } else {
                        con.setFrom(((Fluent) f).getTemporalVariable());
                        con.setTo(((Fluent) t).getTemporalVariable());
                    }
                    retList.add(con);
                }
            } else {
                TemporalConstraint preAIC = new TemporalConstraint(
                        TemporalRelation.MeetsOrOverlapsOrFinishedByOrContains);
                preAIC.setFrom(((Fluent) f).getTemporalVariable());
                preAIC.setTo(((Fluent) t).getTemporalVariable());
                retList.add(preAIC);
            }

        } else if (this.type.equals(Type.OPENS)) {
            if (additionalConstraints != null && !additionalConstraints.isEmpty()) {
                for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
                    TemporalConstraint con = toTemporalConstraint(aConTemplate.getConstraint());
                    if (aConTemplate.startsFromHead()) {
                        con.setFrom(((Fluent) f).getTemporalVariable());
                        con.setTo(((Fluent) t).getTemporalVariable());
                    } else {
                        con.setFrom(((Fluent) t).getTemporalVariable());
                        con.setTo(((Fluent) f).getTemporalVariable());
                    }
                    retList.add(con);
                }
            } else {
                TemporalConstraint befCon = new TemporalConstraint(TemporalRelation.Overlaps);
                befCon.setFrom(((Fluent) f).getTemporalVariable());
                befCon.setTo(((Fluent) t).getTemporalVariable());
                retList.add(befCon);
            }

        } else if (this.type.equals(Type.BEFORE)) {
            // Only used for ordering. No sub-constraints to create.

        } else if (this.type.equals(Type.CLOSES)) {
            if (additionalConstraints != null && !additionalConstraints.isEmpty()) {
                // Additional constraints are added in pre at the moment.
            } else {
                TemporalConstraint oiCon = new TemporalConstraint(
                        TemporalRelation.OverlappedBy);
                oiCon.setFrom(((Fluent) f).getTemporalVariable());
                oiCon.setTo(((Fluent) t).getTemporalVariable());
                retList.add(oiCon);
            }

        } else if (this.type.equals(Type.MOVEDURATION)) {
            TemporalConstraint durationCon = new TemporalConstraint(
                    TemporalRelation.Duration, bounds);
            TemporalVariable tv = ((Fluent) f).getTemporalVariable();
            durationCon.setFrom(tv);
            durationCon.setTo(tv);
            retList.add(durationCon);
        }

        if (connections != null && connections.length > 0) {
            retList.add(createSubmatches(f, t));
        }
        return retList.toArray(new Constraint[0]);
    }

    private CompoundSymbolicConstraint createSubmatches(Variable f, Variable t) {
        CompoundSymbolicConstraint ncon = new CompoundSymbolicConstraint(
                CompoundSymbolicConstraint.Type.SUBMATCHES, connections);
        ncon.setScope(new Variable[] {
                ((Fluent) f).getCompoundSymbolicVariable(),
                ((Fluent) t).getCompoundSymbolicVariable() });
        return ncon;
    }

    private static TemporalConstraint toTemporalConstraint(TemporalConstraintTemplate template) {
        return new TemporalConstraint(template.relation(),
                template.bounds().toArray(new TemporalBounds[0]));
    }

    // -- accessors --

    public Type getType() { return type; }

    public TemporalBounds getBounds() {
        if (bounds == null) return null;
        return new TemporalBounds(bounds.minimum(), bounds.maximum());
    }

    public PlanReportroryItem getPlannedWith() { return plannedWith; }

    public boolean isNegativeEffect() { return isNegativeEffect; }
    public void setNegativeEffect(boolean isNegativeEffect) {
        this.isNegativeEffect = isNegativeEffect;
    }

    public String getAxiom() { return axiom; }

    public boolean isUsingResource(String resource) {
        if (resourceIndicator.getResourceName().equals(resource)) {
            CompoundSymbolicVariable cv =
                    ((Fluent) this.getFrom()).getCompoundSymbolicVariable();
            int[] positions = resourceIndicator.getResourceRequirementPositions();
            for (int i = 0; i < positions.length; i++) {
                String[] symbols = cv.getSymbolsAt(positions[i]);
                if (symbols.length != 1
                        || !symbols[0].equals(resourceIndicator.getResourceRequirements()[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String getResourceName() { return resourceIndicator.getResourceName(); }
    public int getResourceUsageLevel() { return resourceIndicator.getResourceUsageLevel(); }

    public void setAdditionalConstraints(Vector<AdditionalConstraintTemplate> additionalConstraints) {
        this.additionalConstraints = additionalConstraints;
    }

    public Vector<AdditionalConstraintTemplate> getAdditionalConstraints() {
        return additionalConstraints;
    }

    public boolean hasAdditionalConstraints() {
        return additionalConstraints != null && !additionalConstraints.isEmpty();
    }

    public int getDepth() throws IllegalAccessException {
        if (depthIsSet) return depth;
        throw new IllegalAccessException("Depth has not been set");
    }

    public void setDepth(int depth) {
        this.depth = depth;
        this.depthIsSet = true;
    }

    // -- Constraint overrides --

    @Override
    public FluentConstraint clone() {
        FluentConstraint ret = new FluentConstraint(type);
        ret.connections = this.connections;
        ret.bounds = this.bounds;
        ret.plannedWith = this.plannedWith;
        ret.setNegativeEffect(isNegativeEffect);
        ret.axiom = this.axiom;
        ret.resourceIndicator = this.resourceIndicator;
        ret.additionalConstraints = this.additionalConstraints;
        ret.depth = this.depth;
        ret.depthIsSet = this.depthIsSet;
        return ret;
    }

    @Override
    public String getEdgeLabel() {
        return this.type.toString();
    }

    @Override
    public boolean isEquivalent(Constraint c) {
        return false;
    }
}
