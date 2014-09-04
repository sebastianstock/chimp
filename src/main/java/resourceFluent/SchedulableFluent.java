package resourceFluent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.meta.symbolsAndTime.MCSData;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityComparator;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.PowerSet;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0Symbolic.Fluent;
import pfd0Symbolic.FluentComparator;

public abstract class SchedulableFluent  extends Schedulable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5719994497319584156L;
	protected transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	private Vector<Fluent> fluents;
	
	public static enum PEAKCOLLECTION {SAMPLING, COMPLETE, BINARY};
	
	
	public SchedulableFluent(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
	}
	
	
	// Finds sets of overlapping fluents and assesses whether they are conflicting (e.g., over-consuming a resource)
	protected ConstraintNetwork[] samplingPeakCollection() {

		if (fluents != null && !fluents.isEmpty()) {
			
			Fluent[] groundVars = fluents.toArray(new Fluent[fluents.size()]);
			
			Arrays.sort(groundVars,new FluentComparator(true));
			
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			
			HashMap<Fluent,ConstraintNetwork> usages = new HashMap<Fluent,ConstraintNetwork>();
			
			Vector<Vector<Fluent>> overlappingAll = new Vector<Vector<Fluent>>();
			
			// this first block checks whether a single Fluent is overconsuming 
			// the resource
			for (Fluent act : fluents) {
				if (isConflicting(new Fluent[] {act})) {
					ConstraintNetwork temp = new ConstraintNetwork(null);
					temp.addVariable(act);
					ret.add(temp);
				}
			}
	
			//	groundVars are ordered fluents
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Fluent> overlapping = new Vector<Fluent>();
				overlapping.add(groundVars[i]);
				long start = (groundVars[i]).getAllenInterval().getEST();
				long end = (groundVars[i]).getAllenInterval().getEET();
				Bounds intersection = new Bounds(start, end);
				// starting from act[i] all the forthcoming fluents are evaluated to see if they temporally
				// overlaps with act[i]
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = (groundVars[j]).getAllenInterval().getEST();
						end = (groundVars[j]).getAllenInterval().getEET();
						Bounds nextInterval = new Bounds(start, end);
						Bounds intersectionNew = intersection.intersectStrict(nextInterval);
						// if act[j] overlaps it is added to the temporary (wrt i) set of fluents
						if (intersectionNew != null) {
							overlapping.add(groundVars[j]);
							// the current set of overlapping fluents is evaluated to see if
							// the resource capacity is exceeded
							if (isConflicting(overlapping.toArray(new Fluent[overlapping.size()]))) {
								// if it is exceeded the Vector of fluents gathered in this iteration is put
								// in a Vector<Vector<Fluent>>
								overlappingAll.add(overlapping);
								break;						
							}
							// if they don't exceed the capacity, just the newIntersection is taken into account...
							else intersection = intersectionNew;
						}
					}
				}
			}
	
			for (Vector<Fluent> overlapping : overlappingAll) {
				if (overlapping.size() > 1) {
					Fluent first = overlapping.get(0);
					ConstraintNetwork temp = new ConstraintNetwork(null);
					for (Fluent act : overlapping) temp.addVariable(act);
					usages.put(first, temp);
				}
			}
			
			for (Fluent key : usages.keySet()) {
				if (usages.get(key).getVariables().length > 1) ret.add(usages.get(key));
			}
			
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		}
		return (new ConstraintNetwork[0]);		
	}

	
	protected ConstraintNetwork[] completePeakCollection() {
		if (fluents != null && !fluents.isEmpty()) {
			logger.finest("Doing complete peak collection with " + fluents.size() + " fluents...");
			Fluent[] groundVars = fluents.toArray(new Fluent[fluents.size()]);
			Vector<Long> discontinuities = new Vector<Long>();
			for (Fluent a : groundVars) {
				long start = a.getAllenInterval().getEST();
				long end = a.getAllenInterval().getEET();
				if (!discontinuities.contains(start)) discontinuities.add(start);
				if (!discontinuities.contains(end)) discontinuities.add(end);
			}
			
			Long[] discontinuitiesArray = discontinuities.toArray(new Long[discontinuities.size()]);
			Arrays.sort(discontinuitiesArray);
			
			HashSet<HashSet<Fluent>> superPeaks = new HashSet<HashSet<Fluent>>();

			for (int i = 0; i < discontinuitiesArray.length-1; i++) {
				HashSet<Fluent> onePeak = new HashSet<Fluent>();
				superPeaks.add(onePeak);
				Bounds interval = new Bounds(discontinuitiesArray[i], discontinuitiesArray[i+1]);
				for (Fluent a : groundVars) {
					Bounds interval1 = new Bounds(a.getAllenInterval().getEST(), a.getAllenInterval().getEET());
					Bounds intersection = interval.intersectStrict(interval1);
					if (intersection != null && !intersection.isSingleton()) {
						onePeak.add(a);
					}
				}
			}

			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			for (HashSet<Fluent> superSet : superPeaks) {
				for (Set<Fluent> s : PowerSet.powerSet(superSet)) {
					if (!s.isEmpty()) {
						ConstraintNetwork cn = new ConstraintNetwork(null);
						for (Fluent a : s) cn.addVariable(a); 
						if (!ret.contains(cn) && isConflicting(s.toArray(new Fluent[s.size()]))) ret.add(cn);
					}
				}
			}
			logger.finest("Done peak sampling");
			return ret.toArray(new ConstraintNetwork[ret.size()]);			
		} 
		
		return (new ConstraintNetwork[0]);
	}

	
	protected ConstraintNetwork[] binaryPeakCollection() {
		if (fluents != null && !fluents.isEmpty()) {
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			logger.finest("Doing binary peak collection with " + fluents.size() + " fluents...");
			Fluent[] groundVars = fluents.toArray(new Fluent[fluents.size()]);
			for (Fluent a : groundVars) {
				if (isConflicting(new Fluent[] {a})) {
					ConstraintNetwork cn = new ConstraintNetwork(null);
					cn.addVariable(a);
					ret.add(cn);
				}
			}
			if (!ret.isEmpty()) {
				return ret.toArray(new ConstraintNetwork[ret.size()]);
			}
			for (int i = 0; i < groundVars.length-1; i++) {
				for (int j = i+1; j < groundVars.length; j++) {
					Bounds bi = new Bounds(groundVars[i].getAllenInterval().getEST(), groundVars[i].getAllenInterval().getEET());
					Bounds bj = new Bounds(groundVars[j].getAllenInterval().getEST(), groundVars[j].getAllenInterval().getEET());
					if (bi.intersectStrict(bj) != null && isConflicting(new Fluent[] {groundVars[i], groundVars[j]})) {
						ConstraintNetwork cn = new ConstraintNetwork(null);
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
		return (new ConstraintNetwork[0]);
	}
	
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {	
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		MCSData[] mcsinfo = getOrderedMCSs(conflict);
		
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		if(mcsinfo == null) //unresolvabe MCS: no solution can be found
		{				
			//System.out.println("ESTA Fails: unresolvable MCS.");
			return null;
		}
		
		for (MCSData mcs : mcsinfo) {
			AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.BeforeOrMeets, new Bounds(this.beforeParameter, APSPSolver.INF));
			before.setFrom(mcs.mcsActFrom);			
			before.setTo(mcs.mcsActTo);
			ConstraintNetwork resolver = new ConstraintNetwork(mcs.mcsActFrom.getConstraintSolver());
			resolver.addVariable(mcs.mcsActFrom);
			resolver.addVariable(mcs.mcsActTo);
			resolver.addConstraint(before);
			ret.add(resolver);
		}

		return ret.toArray(new ConstraintNetwork[ret.size()]);

	}
	

	protected boolean temporalOverlap(Fluent a1, Fluent a2) {
		return !(
				a1.getAllenInterval().getEET() <= a2.getAllenInterval().getEST() ||
				a2.getAllenInterval().getEET() <= a1.getAllenInterval().getEST()
				);
	}

	public abstract boolean isConflicting(Fluent[] peak);
	
	public void setUsage(Fluent... acts) {
		if (fluents == null) fluents = new Vector<Fluent>();
		for (Fluent act : acts) 
			if (!fluents.contains(act)) 
				fluents.add(act);
		//System.out.println("-->" + fluents.size());
	}

	public void removeUsage(Fluent... acts) {
		for (Fluent act : acts) fluents.removeElement(act);
		//System.out.println("-->" + fluents.size());
	}
	
		
	public Vector<Fluent> getFluentOnUse(){
		return fluents;
	}
}
