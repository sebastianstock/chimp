package chimp.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.symbolsAndTime.MCSData;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityComparator;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.PowerSet;

import java.util.Comparator;

/**
 * CHIMP-owned replacement for {@code org.metacsp.meta.symbolsAndTime.Schedulable}.
 * Ports the peak-collection and minimal critical set (MCS) resolution logic
 * without inheriting from the Meta-CSP framework.
 *
 * <p>Subclasses implement {@link #isConflicting(Activity[])} to define what
 * constitutes a resource conflict, and populate the {@link #activities} vector
 * (typically in an overridden {@link #getMetaVariables()}).
 */
public abstract class ChimpSchedulable extends ChimpMetaConstraint {

    public enum PeakCollection { SAMPLING, COMPLETE, BINARY }

    protected long beforeParameter = 0;
    protected Vector<Activity> activities;
    protected PeakCollection peakCollectionStrategy = PeakCollection.SAMPLING;

    public ChimpSchedulable(Comparator<ConstraintNetwork> varOH,
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
    public ConstraintNetwork[] getMetaVariables() {
        switch (peakCollectionStrategy) {
            case SAMPLING: return samplingPeakCollection();
            case BINARY:   return binaryPeakCollection();
            default:       return completePeakCollection();
        }
    }

    @Override
    public ConstraintNetwork[] resolvers(ConstraintNetwork flawNetwork) {
        ConstraintNetwork conflict = flawNetwork;
        MCSData[] mcsinfo = getOrderedMCSs(conflict);

        Vector<ConstraintNetwork> ret = new Vector<>();
        if (mcsinfo == null) {
            return null;
        }

        for (MCSData mcs : mcsinfo) {
            AllenIntervalConstraint before = new AllenIntervalConstraint(
                    AllenIntervalConstraint.Type.BeforeOrMeets,
                    new Bounds(this.beforeParameter, APSPSolver.INF));
            before.setFrom(mcs.mcsActFrom.getVariable());
            before.setTo(mcs.mcsActTo.getVariable());
            ConstraintNetwork resolver = new ConstraintNetwork(
                    mcs.mcsActFrom.getVariable().getConstraintSolver());
            resolver.addVariable(mcs.mcsActFrom.getVariable());
            resolver.addVariable(mcs.mcsActTo.getVariable());
            resolver.addConstraint(before);
            ret.add(resolver);
        }

        return ret.toArray(new ConstraintNetwork[ret.size()]);
    }

    // --- Peak collection strategies ---

    protected ConstraintNetwork[] samplingPeakCollection() {
        if (activities != null && !activities.isEmpty()) {
            Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
            Arrays.sort(groundVars, new ActivityComparator(true));

            Vector<ConstraintNetwork> ret = new Vector<>();
            HashMap<Activity, ConstraintNetwork> usages = new HashMap<>();
            Vector<Vector<Activity>> overlappingAll = new Vector<>();

            for (Activity act : activities) {
                if (isConflicting(new Activity[] {act})) {
                    ConstraintNetwork temp = new ConstraintNetwork(null);
                    temp.addVariable(act.getVariable());
                    ret.add(temp);
                }
            }

            for (int i = 0; i < groundVars.length; i++) {
                Vector<Activity> overlapping = new Vector<>();
                overlapping.add(groundVars[i]);
                long start = groundVars[i].getTemporalVariable().getEST();
                long end = groundVars[i].getTemporalVariable().getEET();
                Bounds intersection = new Bounds(start, end);
                for (int j = 0; j < groundVars.length; j++) {
                    if (i != j) {
                        start = groundVars[j].getTemporalVariable().getEST();
                        end = groundVars[j].getTemporalVariable().getEET();
                        Bounds nextInterval = new Bounds(start, end);
                        Bounds intersectionNew = intersection.intersectStrict(nextInterval);
                        if (intersectionNew != null) {
                            overlapping.add(groundVars[j]);
                            if (isConflicting(overlapping.toArray(new Activity[overlapping.size()]))) {
                                overlappingAll.add(overlapping);
                                break;
                            } else {
                                intersection = intersectionNew;
                            }
                        }
                    }
                }
            }

            for (Vector<Activity> overlapping : overlappingAll) {
                if (overlapping.size() > 1) {
                    Activity first = overlapping.get(0);
                    ConstraintNetwork temp = new ConstraintNetwork(null);
                    for (Activity act : overlapping) temp.addVariable(act.getVariable());
                    usages.put(first, temp);
                }
            }

            for (Activity key : usages.keySet()) {
                if (usages.get(key).getVariables().length > 1) ret.add(usages.get(key));
            }

            return ret.toArray(new ConstraintNetwork[ret.size()]);
        }
        return new ConstraintNetwork[0];
    }

    protected ConstraintNetwork[] completePeakCollection() {
        if (activities != null && !activities.isEmpty()) {
            logger.finest("Doing complete peak collection with " + activities.size() + " activities...");
            Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
            Vector<Long> discontinuities = new Vector<>();
            for (Activity a : groundVars) {
                long start = a.getTemporalVariable().getEST();
                long end = a.getTemporalVariable().getEET();
                if (!discontinuities.contains(start)) discontinuities.add(start);
                if (!discontinuities.contains(end)) discontinuities.add(end);
            }

            Long[] discontinuitiesArray = discontinuities.toArray(new Long[discontinuities.size()]);
            Arrays.sort(discontinuitiesArray);

            HashSet<HashSet<Activity>> superPeaks = new HashSet<>();

            for (int i = 0; i < discontinuitiesArray.length - 1; i++) {
                HashSet<Activity> onePeak = new HashSet<>();
                superPeaks.add(onePeak);
                Bounds interval = new Bounds(discontinuitiesArray[i], discontinuitiesArray[i + 1]);
                for (Activity a : groundVars) {
                    Bounds interval1 = new Bounds(
                            a.getTemporalVariable().getEST(),
                            a.getTemporalVariable().getEET());
                    Bounds intersection = interval.intersectStrict(interval1);
                    if (intersection != null && !intersection.isSingleton()) {
                        onePeak.add(a);
                    }
                }
            }

            Vector<ConstraintNetwork> ret = new Vector<>();
            for (HashSet<Activity> superSet : superPeaks) {
                for (Set<Activity> s : PowerSet.powerSet(superSet)) {
                    if (!s.isEmpty()) {
                        ConstraintNetwork cn = new ConstraintNetwork(null);
                        for (Activity a : s) cn.addVariable(a.getVariable());
                        if (!ret.contains(cn) && isConflicting(s.toArray(new Activity[s.size()]))) {
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
            Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
            for (Activity a : groundVars) {
                if (isConflicting(new Activity[] {a})) {
                    ConstraintNetwork cn = new ConstraintNetwork(null);
                    cn.addVariable(a.getVariable());
                    ret.add(cn);
                }
            }
            if (!ret.isEmpty()) {
                return ret.toArray(new ConstraintNetwork[ret.size()]);
            }
            for (int i = 0; i < groundVars.length - 1; i++) {
                for (int j = i + 1; j < groundVars.length; j++) {
                    Bounds bi = new Bounds(
                            groundVars[i].getTemporalVariable().getEST(),
                            groundVars[i].getTemporalVariable().getEET());
                    Bounds bj = new Bounds(
                            groundVars[j].getTemporalVariable().getEST(),
                            groundVars[j].getTemporalVariable().getEET());
                    if (bi.intersectStrict(bj) != null
                            && isConflicting(new Activity[] {groundVars[i], groundVars[j]})) {
                        ConstraintNetwork cn = new ConstraintNetwork(null);
                        cn.addVariable(groundVars[i].getVariable());
                        cn.addVariable(groundVars[j].getVariable());
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
                long est1 = ((Activity) currentMcs[g]).getTemporalVariable().getEST();
                long eet1 = ((Activity) currentMcs[g]).getTemporalVariable().getEST();
                long lst1 = ((Activity) currentMcs[g]).getTemporalVariable().getLST();
                long let1 = ((Activity) currentMcs[g]).getTemporalVariable().getLET();

                for (int h = g + 1; h < mcsSize; h++) {
                    long est2 = ((Activity) currentMcs[h]).getTemporalVariable().getEST();
                    long eet2 = ((Activity) currentMcs[h]).getTemporalVariable().getEET();
                    long lst2 = ((Activity) currentMcs[h]).getTemporalVariable().getLST();
                    long let2 = ((Activity) currentMcs[h]).getTemporalVariable().getLET();

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
                mcsinfo.add(new MCSData(pcmin, (Activity) actFrom, (Activity) actTo,
                        (kReciprocal == 0.0f) ? 1 : 1.0f / kReciprocal));
                mcsinfo.add(new MCSData(pcminBad, (Activity) actTo, (Activity) actFrom,
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

    protected boolean temporalOverlap(Activity a1, Activity a2) {
        return !(a1.getTemporalVariable().getEET() <= a2.getTemporalVariable().getEST()
                || a2.getTemporalVariable().getEET() <= a1.getTemporalVariable().getEST());
    }

    public abstract boolean isConflicting(Activity[] peak);

    public void setUsage(Activity... acts) {
        if (activities == null) activities = new Vector<>();
        for (Activity act : acts) {
            if (!activities.contains(act)) {
                activities.add(act);
            }
        }
    }

    public void removeUsage(Activity... acts) {
        if (activities != null) {
            for (Activity act : acts) activities.removeElement(act);
        }
    }

    public Vector<Activity> getActivityOnUse() {
        return activities;
    }
}
