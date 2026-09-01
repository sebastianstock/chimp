package chimp.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.kernel.temporal.TemporalVariable;
import chimp.model.FluentId;
import chimp.model.TemporalBounds;
import chimp.util.PowerSet;

/**
 * CHIMP-owned replacement for {@code org.metacsp.meta.symbolsAndTime.Schedulable}.
 * Ports the peak-collection and minimal critical set (MCS) resolution logic
 * without inheriting from the Meta-CSP framework.
 *
 * <p>Subclasses implement {@link #isConflicting(Fluent[])} to define what
 * constitutes a resource conflict, and populate the {@link #activities} vector
 * (typically in an overridden {@link #getMetaVariables()}).
 */
public abstract class ChimpSchedulable extends ChimpMetaConstraint {

    public enum PeakCollection { SAMPLING, COMPLETE, BINARY }

    protected long beforeParameter = 0;
    protected Vector<Fluent> activities;
    protected PeakCollection peakCollectionStrategy = PeakCollection.SAMPLING;

	public ChimpSchedulable(Comparator<ChimpFlaw> varOH,
                            Comparator<ConstraintNetwork> valOH) {
		super(varOH, valOH);
	}

    public long getBeforeParameter() { return beforeParameter; }
    public void setBeforeParameter(long beforeParameter) { this.beforeParameter = beforeParameter; }

    public PeakCollection getPeakCollectionStrategy() { return peakCollectionStrategy; }
    public void setPeakCollectionStrategy(PeakCollection strategy) {
        this.peakCollectionStrategy = strategy;
    }

    @Override
    public ChimpFlaw[] getMetaVariables() {
        switch (peakCollectionStrategy) {
            case SAMPLING: return toFlaws(samplingPeakCollection());
            case BINARY:   return toFlaws(binaryPeakCollection());
            default:       return toFlaws(completePeakCollection());
        }
    }

    private static ChimpFlaw[] toFlaws(ConstraintNetwork[] networks) {
        ChimpFlaw[] result = new ChimpFlaw[networks.length];
        for (int i = 0; i < networks.length; i++) {
            Variable[] vars = networks[i].getVariables();
            List<FluentId> ids = new ArrayList<>(vars.length);
            for (Variable v : vars) {
                ids.add(new FluentId(v.getID()));
            }
            result[i] = new ChimpFlaw(ids);
        }
        return result;
    }

    @Override
    public ChimpResolver[] resolvers(ChimpFlaw flaw) {
        ConstraintNetwork conflict = peakForFlaw(flaw);
        MCSData[] mcsinfo = getOrderedMCSs(conflict);

        Vector<ConstraintNetwork> ret = new Vector<>();
        if (mcsinfo == null) {
            return null;
        }

        for (MCSData mcs : mcsinfo) {
            TemporalConstraint before = new TemporalConstraint(
                    chimp.model.TemporalRelation.BeforeOrMeets,
                    new TemporalBounds[] { new TemporalBounds(this.beforeParameter, TemporalBounds.INFINITY) });
            before.setFrom(mcs.mcsActFrom);
            before.setTo(mcs.mcsActTo);
            ConstraintNetwork resolver = new ConstraintNetwork();
            resolver.addVariable(mcs.mcsActFrom);
            resolver.addVariable(mcs.mcsActTo);
            resolver.addConstraint(before);
            ret.add(resolver);
        }

        return ret.stream().map(ChimpResolver::new).toArray(ChimpResolver[]::new);
    }

    /**
     * Reconstructs the peak {@link ConstraintNetwork} for a given flaw
     * by looking up the corresponding activities in the {@link #activities}
     * vector. The peak network is what {@link #getOrderedMCSs} needs as
     * input.
     */
    private ConstraintNetwork peakForFlaw(ChimpFlaw flaw) {
        if (activities == null) {
            return new ConstraintNetwork();
        }
        ConstraintNetwork cn = new ConstraintNetwork();
        for (FluentId id : flaw.taskIds()) {
            for (Fluent act : activities) {
                if (act.getID() == id.value()) {
                    cn.addVariable(act);
                    break;
                }
            }
        }
        return cn;
    }

    // --- Peak collection strategies ---

    protected ConstraintNetwork[] samplingPeakCollection() {
        if (activities != null && !activities.isEmpty()) {
            Fluent[] groundVars = activities.toArray(new Fluent[activities.size()]);
            Arrays.sort(groundVars, new FluentActivityComparator(true));

            Vector<ConstraintNetwork> ret = new Vector<>();
            HashMap<Fluent, ConstraintNetwork> usages = new HashMap<>();
            Vector<Vector<Fluent>> overlappingAll = new Vector<>();

            for (Fluent act : activities) {
                if (isConflicting(new Fluent[] {act})) {
                    ConstraintNetwork temp = new ConstraintNetwork();
                    temp.addVariable(act);
                    ret.add(temp);
                }
            }

            for (int i = 0; i < groundVars.length; i++) {
                Vector<Fluent> overlapping = new Vector<>();
                overlapping.add(groundVars[i]);
                long start = groundVars[i].getTemporalVariable().getEST();
                long end = groundVars[i].getTemporalVariable().getEET();
                TemporalBounds intersection = new TemporalBounds(start, end);
                for (int j = 0; j < groundVars.length; j++) {
                    if (i != j) {
                        start = groundVars[j].getTemporalVariable().getEST();
                        end = groundVars[j].getTemporalVariable().getEET();
                        TemporalBounds nextInterval = new TemporalBounds(start, end);
                        TemporalBounds intersectionNew = intersection.intersectStrict(nextInterval);
                        if (intersectionNew != null) {
                            overlapping.add(groundVars[j]);
                            if (isConflicting(overlapping.toArray(new Fluent[overlapping.size()]))) {
                                overlappingAll.add(overlapping);
                                break;
                            } else {
                                intersection = intersectionNew;
                            }
                        }
                    }
                }
            }

            for (Vector<Fluent> overlapping : overlappingAll) {
                if (overlapping.size() > 1) {
                    Fluent first = overlapping.get(0);
                    ConstraintNetwork temp = new ConstraintNetwork();
                    for (Fluent act : overlapping) temp.addVariable(act);
                    usages.put(first, temp);
                }
            }

            for (Fluent key : usages.keySet()) {
                if (usages.get(key).getVariables().length > 1) ret.add(usages.get(key));
            }

            return ret.toArray(new ConstraintNetwork[ret.size()]);
        }
        return new ConstraintNetwork[0];
    }

    protected ConstraintNetwork[] completePeakCollection() {
        if (activities != null && !activities.isEmpty()) {
            logger.finest("Doing complete peak collection with " + activities.size() + " activities...");
            Fluent[] groundVars = activities.toArray(new Fluent[activities.size()]);
            Vector<Long> discontinuities = new Vector<>();
            for (Fluent a : groundVars) {
                long start = a.getTemporalVariable().getEST();
                long end = a.getTemporalVariable().getEET();
                if (!discontinuities.contains(start)) discontinuities.add(start);
                if (!discontinuities.contains(end)) discontinuities.add(end);
            }

            Long[] discontinuitiesArray = discontinuities.toArray(new Long[discontinuities.size()]);
            Arrays.sort(discontinuitiesArray);

            HashSet<HashSet<Fluent>> superPeaks = new HashSet<>();

            for (int i = 0; i < discontinuitiesArray.length - 1; i++) {
                HashSet<Fluent> onePeak = new HashSet<>();
                superPeaks.add(onePeak);
                TemporalBounds interval = new TemporalBounds(discontinuitiesArray[i], discontinuitiesArray[i + 1]);
                for (Fluent a : groundVars) {
                    TemporalBounds interval1 = new TemporalBounds(
                            a.getTemporalVariable().getEST(),
                            a.getTemporalVariable().getEET());
                    TemporalBounds intersection = interval.intersectStrict(interval1);
                    if (intersection != null && !intersection.isSingleton()) {
                        onePeak.add(a);
                    }
                }
            }

            Vector<ConstraintNetwork> ret = new Vector<>();
            for (HashSet<Fluent> superSet : superPeaks) {
                for (Set<Fluent> s : PowerSet.powerSet(superSet)) {
                    if (!s.isEmpty()) {
                        ConstraintNetwork cn = new ConstraintNetwork();
                        for (Fluent a : s) cn.addVariable(a);
                        if (!ret.contains(cn) && isConflicting(s.toArray(new Fluent[s.size()]))) {
                            ret.add(cn);
                        }
                    }
                }
            }
            logger.finest("Done peak sampling");
            return ret.toArray(new ConstraintNetwork[ret.size()]);
        }
        return new ConstraintNetwork[0];
    }

    protected ConstraintNetwork[] binaryPeakCollection() {
        if (activities != null && !activities.isEmpty()) {
            Vector<ConstraintNetwork> ret = new Vector<>();
            logger.finest("Doing binary peak collection with " + activities.size() + " activities...");
            Fluent[] groundVars = activities.toArray(new Fluent[activities.size()]);
            for (Fluent a : groundVars) {
                if (isConflicting(new Fluent[] {a})) {
                    ConstraintNetwork cn = new ConstraintNetwork();
                    cn.addVariable(a);
                    ret.add(cn);
                }
            }
            if (!ret.isEmpty()) {
                return ret.toArray(new ConstraintNetwork[ret.size()]);
            }
            for (int i = 0; i < groundVars.length - 1; i++) {
                for (int j = i + 1; j < groundVars.length; j++) {
                    TemporalBounds bi = new TemporalBounds(
                            groundVars[i].getTemporalVariable().getEST(),
                            groundVars[i].getTemporalVariable().getEET());
                    TemporalBounds bj = new TemporalBounds(
                            groundVars[j].getTemporalVariable().getEST(),
                            groundVars[j].getTemporalVariable().getEET());
                    if (bi.intersectStrict(bj) != null
                            && isConflicting(new Fluent[] {groundVars[i], groundVars[j]})) {
                        ConstraintNetwork cn = new ConstraintNetwork();
                        cn.addVariable(groundVars[i]);
                        cn.addVariable(groundVars[j]);
                        ret.add(cn);
                    }
                }
            }
            if (!ret.isEmpty()) {
                return ret.toArray(new ConstraintNetwork[ret.size()]);
            }
        }
        return new ConstraintNetwork[0];
    }

    // --- MCS computation (ported from Schedulable.getOrderedMCSs) ---

    public MCSData[] getOrderedMCSs(ConstraintNetwork peak) {
        Vector<Variable[]> mcslist = new Vector<>();
        Variable[] vars = peak.getVariables();
        for (int i = 0; i < vars.length; i++) {
            for (int j = i + 1; j < vars.length; j++) {
                Variable[] oneMcs = new Variable[2];
                oneMcs[0] = vars[i];
                oneMcs[1] = vars[j];
                mcslist.add(oneMcs);
            }
        }

        Vector<MCSData> mcsinfo = new Vector<>();
        int index = 0;
        boolean unresMCSFound = false;

        while ((!unresMCSFound) && (index < mcslist.size())) {
            float pcmin = 1.0f;
            float pcminBad = 1.0f;
            float kReciprocal = 0.0f;
            Variable actFrom = null;
            Variable actTo = null;
            int unresMCS = 0;

            Variable[] currentMcs = mcslist.elementAt(index);
            Vector<Float> pcVector = new Vector<>();
            int mcsSize = currentMcs.length;

            for (int g = 0; g < mcsSize; g++) {
                long est1 = ((Fluent) currentMcs[g]).getTemporalVariable().getEST();
                // Note: reads getEET() as the variable name suggests.  Upstream
                // Meta-CSP (1.3.5 Schedulable.getConflict) has a bug here — it
                // reads getEST() into eet1 — which this port faithfully
                // reproduced until verified plan-neutral on the 93-problem
                // benchmark suite and fixed.
                long eet1 = ((Fluent) currentMcs[g]).getTemporalVariable().getEET();
                long lst1 = ((Fluent) currentMcs[g]).getTemporalVariable().getLST();
                long let1 = ((Fluent) currentMcs[g]).getTemporalVariable().getLET();

                for (int h = g + 1; h < mcsSize; h++) {
                    long est2 = ((Fluent) currentMcs[h]).getTemporalVariable().getEST();
                    long eet2 = ((Fluent) currentMcs[h]).getTemporalVariable().getEET();
                    long lst2 = ((Fluent) currentMcs[h]).getTemporalVariable().getLST();
                    long let2 = ((Fluent) currentMcs[h]).getTemporalVariable().getLET();

                    long dmin = est2 - let1;
                    long dmax = lst2 - eet1;
                    if (dmin > dmax) {
                        logger.severe("Direct pair and dmin > dmax: IMPOSSIBLE");
                    }

                    float pc = 0.0f;
                    if (dmin != dmax) {
                        pc = ((float) (Math.min(dmax, 0) - Math.min(dmin, 0))) / ((float) (dmax - dmin));
                        pcVector.add(pc);
                        if (pc < pcmin) {
                            pcmin = pc;
                            pcminBad = pcmin;
                            actFrom = currentMcs[g];
                            actTo = currentMcs[h];
                        } else {
                            unresMCS++;
                        }
                    } else {
                        unresMCS++;
                    }

                    dmin = est1 - let2;
                    dmax = lst1 - eet2;
                    if (dmin > dmax) {
                        logger.severe("Inverse pair and dmin > dmax: IMPOSSIBLE");
                    }

                    if (dmin != dmax) {
                        pc = ((float) (Math.min(dmax, 0) - Math.min(dmin, 0))) / ((float) (dmax - dmin));
                        pcVector.add(pc);
                        if (pc < pcmin) {
                            pcmin = pc;
                            actFrom = currentMcs[h];
                            actTo = currentMcs[g];
                        } else {
                            unresMCS++;
                        }
                    } else {
                        unresMCS++;
                    }
                }
            }

            if (unresMCS < (mcsSize * (mcsSize - 1))) {
                for (int g = 0; g < pcVector.size(); g++) {
                    kReciprocal += 1.0f / (1.0f + pcVector.elementAt(g) - pcmin);
                }
                mcsinfo.add(new MCSData(pcmin, (Fluent) actFrom, (Fluent) actTo,
                        (kReciprocal == 0.0f) ? 1 : 1.0f / kReciprocal));
                mcsinfo.add(new MCSData(pcminBad, (Fluent) actTo, (Fluent) actFrom,
                        (kReciprocal == 0.0f) ? 1 : 1.0f / kReciprocal));
                index++;
            } else {
                unresMCSFound = true;
            }
        }

        MCSData[] mcsinfoArray = mcsinfo.toArray(new MCSData[mcsinfo.size()]);
        if (!unresMCSFound) {
            Arrays.sort(mcsinfoArray);
        } else {
            mcsinfoArray = null;
        }
        return mcsinfoArray;
    }

    protected boolean temporalOverlap(Fluent a1, Fluent a2) {
        return !(a1.getTemporalVariable().getEET() <= a2.getTemporalVariable().getEST()
                || a2.getTemporalVariable().getEET() <= a1.getTemporalVariable().getEST());
    }

    public abstract boolean isConflicting(Fluent[] peak);

    public void setUsage(Fluent... acts) {
        if (activities == null) activities = new Vector<>();
        for (Fluent act : acts) {
            if (!activities.contains(act)) {
                activities.add(act);
            }
        }
    }

    public void removeUsage(Fluent... acts) {
        if (activities != null) {
            for (Fluent act : acts) activities.removeElement(act);
        }
    }

    public Vector<Fluent> getActivityOnUse() {
        return activities;
    }

    // --- Comparator for Fluent activities (replaces ActivityComparator) ---

    public static final class FluentActivityComparator implements Comparator<Fluent> {
        private final boolean byStart;

        public FluentActivityComparator(boolean byStart) {
            this.byStart = byStart;
        }

        @Override
        public int compare(Fluent a1, Fluent a2) {
            TemporalVariable tv1 = a1.getTemporalVariable();
            TemporalVariable tv2 = a2.getTemporalVariable();
            long v1 = byStart ? tv1.getEST() : tv1.getLET();
            long v2 = byStart ? tv2.getEST() : tv2.getLET();
            return Long.compare(v1, v2);
        }
    }
}
