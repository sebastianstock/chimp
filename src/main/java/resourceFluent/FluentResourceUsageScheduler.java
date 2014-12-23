package resourceFluent;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;

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
		// TODO test if this is fast enough
		updateUsageMap();
		activities = new Vector<Activity>(usageMap.keySet());
		return super.getMetaVariables();
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
		//sum; if sum > cap retrun true;
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
		StringBuilder sb = new StringBuilder("FluentResouceUsageScheduler ");
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

}
