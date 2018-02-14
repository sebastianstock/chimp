package resourceFluent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import htn.HTNMetaConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityComparator;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class FluentResourceUsageScheduler extends Schedulable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4496085871883286110L;
	private final int capacity;
	private final String name;
	private final Map<Activity, Integer> usageMap = new HashMap<Activity, Integer>();

	public FluentResourceUsageScheduler(VariableOrderingH varOH, ValueOrderingH valOH, String name, int cap) {
		super(varOH, valOH);
		this.setPeakCollectionStrategy(PEAKCOLLECTION.SAMPLING);
		this.name = name;
		this.capacity = cap;
	}
	
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		updateUsageMap();
		activities = new Vector<Activity>(usageMap.keySet());
		return samplingPeakCollection();
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork[] possiblResolvers = super.getMetaValues(metaVariable);
		if(possiblResolvers == null) {
			return null;
		}
		// We need to filter the resolvers, because we don't want to finish open state fluents.
		List<ConstraintNetwork> filtered = new ArrayList<ConstraintNetwork>();
		for (ConstraintNetwork cn : possiblResolvers) {
			// test if cn has a before constraint from an open state fluent.
			for(Constraint c : cn.getConstraints()) {
				if (c instanceof AllenIntervalConstraint) {
					Fluent from = (Fluent) ((AllenIntervalConstraint) c).getFrom();
					if (from.getMarking() != HTNMetaConstraint.markings.OPEN) {
						filtered.add(cn);
					}
				}
			}
		}
		return filtered.toArray(new ConstraintNetwork[filtered.size()]);
	}

	private void updateUsageMap() {
		usageMap.clear();
		for (Constraint con : this.getGroundSolver().getConstraints()) {
			if (con instanceof FluentConstraint) {
				if(((FluentConstraint) con).getType() == FluentConstraint.Type.RESOURCEUSAGE) {
					if (((FluentConstraint) con).isUsingResource(this.name)) {
						usageMap.put((Fluent) ((FluentConstraint) con).getFrom(), 
								Integer.valueOf(((FluentConstraint) con).getResourceUsageLevel()));
					}
				}
			}
		}
	}

	@Override
	public boolean isConflicting(Activity[] peak) {
//		if (peak.length == 1 && peak[0].getVariable().getUsageAmount() > this.capacity) return true;
		//sum; if sum > cap return true;
		int sum = 0;
		for (Activity act : peak) {
			sum += usageMap.get(act);
			if (sum > capacity) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return this.metaCS.getConstraintSolvers()[0];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FluentResourceUsageScheduler ");
		sb.append(name);
		sb.append(" : ");
		sb.append(capacity);
		return sb.toString();
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// Finds sets of overlapping activities and assesses whether they are conflicting (e.g., over-consuming a resource)
	@Override
	protected ConstraintNetwork[] samplingPeakCollection() {

		long horizon = ((FluentNetworkSolver) this.getGroundSolver()).getHorizon();

		if (activities != null && !activities.isEmpty()) {
			
			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
			
			Arrays.sort(groundVars,new ActivityComparator(true));
			
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			
			HashMap<Activity,ConstraintNetwork> usages = new HashMap<Activity,ConstraintNetwork>();
			
			Vector<Vector<Activity>> overlappingAll = new Vector<Vector<Activity>>();
			
			// this first block checks whether a single activity is over-consuming
			// the resource
			for (Activity act : activities) {
				if (isConflicting(new Activity[] {act})) {
					ConstraintNetwork temp = new ConstraintNetwork(null);
					temp.addVariable(act.getVariable());
					ret.add(temp);
				}
			}
	
			//	groundVars are ordered activities
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Activity> overlapping = new Vector<Activity>();
				overlapping.add(groundVars[i]);
				long start = (groundVars[i]).getTemporalVariable().getEST();
				long end = (groundVars[i]).getTemporalVariable().getEET();
				if ((groundVars[i]).getTemporalVariable().getLET() == horizon) {  // for open fluents we use horizon instead of EET
					end = horizon;
				}
				Bounds intersection = new Bounds(start, end);
				// starting from act[i] all the forthcoming activities are evaluated to see if they temporally
				// overlaps with act[i]
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = (groundVars[j]).getTemporalVariable().getEST();
						end = (groundVars[j]).getTemporalVariable().getEET();
						if ((groundVars[j]).getTemporalVariable().getLET() == horizon) {     // for open fluents we use horizon instead of EET
							end = horizon;
						}
						Bounds nextInterval = new Bounds(start, end);
						Bounds intersectionNew = intersection.intersectStrict(nextInterval);
						// if act[j] overlaps it is added to the temporary (wrt i) set of activities
						if (intersectionNew != null) {
							overlapping.add(groundVars[j]);
							// the current set of overlapping activities is evaluated to see if
							// the resource capacity is exceeded
							if (isConflicting(overlapping.toArray(new Activity[overlapping.size()]))) {
								// if it is exceeded the Vector of activities gathered in this iteration is put
								// in a Vector<Vector<Activity>>
								overlappingAll.add(overlapping);
								break;						
							}
							// if they don't exceed the capacity, just the newIntersection is taken into account...
							else intersection = intersectionNew;
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
		return (new ConstraintNetwork[0]);		
	}

}
