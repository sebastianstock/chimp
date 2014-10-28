package resourceFluent;

import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0Symbolic.Fluent;
import pfd0Symbolic.TaskApplicationMetaConstraint;
import pfd0Symbolic.TaskSelectionMetaConstraint;


// For the moment just look at that like a capacity with associated a set of activities:
// this class comes from Schedulable that implements sophisticated methods to 
// detect peaks in resource consumption
public class SimpleReusableResourceFluent extends SchedulableFluent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7860488618227112837L;
	private int capacity;
	private MetaConstraint mc;
	private String name;

	public SimpleReusableResourceFluent(VariableOrderingH varOH, ValueOrderingH valOH, int capacity, MetaConstraint mc, String name) {
		super(varOH, valOH);
		this.capacity = capacity;
		this.mc = mc;
		this.name = name;
	}

	@Override
	public boolean isConflicting(Fluent[] peak) {
		int sum = 0;

		for (Fluent act : peak) {
			if(this.mc instanceof TaskSelectionMetaConstraint)
				sum += ((TaskSelectionMetaConstraint)mc).getResourceUsageLevel(this, act);
			else if(mc instanceof TaskApplicationMetaConstraint)
				sum += ((TaskApplicationMetaConstraint)mc).getResourceUsageLevel(this, act);

			if (sum > capacity) return true;
		}
		return false;
	}


	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SimpleReusableResource " + name + ", capacity = " + capacity;
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


	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCapacity(){
		return capacity;
	}


	@Override
	public boolean isConflicting(Activity[] arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}

