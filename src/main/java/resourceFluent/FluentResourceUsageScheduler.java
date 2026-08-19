package resourceFluent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.kernel.temporal.TemporalConstraint;
import chimp.model.FluentMarking;
import chimp.model.TemporalBounds;
import chimp.search.ChimpFlaw;
import chimp.search.ChimpResolver;
import chimp.search.ChimpSchedulable;
import htn.GroundSolverHolder;

public class FluentResourceUsageScheduler extends ChimpSchedulable implements GroundSolverHolder {

	private static final long serialVersionUID = 4496085871883286110L;
	private final int capacity;
	private final String name;
	private final Map<Fluent, Integer> usageMap = new HashMap<Fluent, Integer>();
	private FluentNetworkSolver groundSolver;

	public FluentResourceUsageScheduler(Comparator<ChimpFlaw> varOH, Comparator<ConstraintNetwork> valOH, String name, int cap) {
		super(varOH, valOH);
		this.setPeakCollectionStrategy(PeakCollection.SAMPLING);
		this.name = name;
		this.capacity = cap;
	}

	public void setGroundSolver(FluentNetworkSolver groundSolver) {
		this.groundSolver = groundSolver;
	}

	@Override
	public ChimpFlaw[] getMetaVariables() {
		updateUsageMap();
		activities = new Vector<Fluent>(usageMap.keySet());
		return super.getMetaVariables();
	}

	@Override
	public ChimpResolver[] resolvers(ChimpFlaw flaw) {
		ChimpResolver[] possiblResolvers = super.resolvers(flaw);
		if(possiblResolvers == null) {
			return null;
		}
		// We need to filter the resolvers, because we don't want to finish open state fluents.
		List<ChimpResolver> filtered = new ArrayList<ChimpResolver>();
		for (ChimpResolver resolver : possiblResolvers) {
			// test if resolver has a before constraint from an open state fluent.
			for(Constraint c : resolver.delegate.getConstraints()) {
				if (c instanceof TemporalConstraint) {
					Fluent from = (Fluent) c.getFrom();
					if (from.getMarking() != FluentMarking.OPEN.ordinal()) {
						filtered.add(resolver);
					}
				}
			}
		}
		return filtered.toArray(new ChimpResolver[filtered.size()]);
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
	public boolean isConflicting(Fluent[] peak) {
		int sum = 0;
		for (Fluent act : peak) {
			sum += usageMap.get(act);
			if (sum > capacity) {
				return true;
			}
		}

		return false;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return groundSolver;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FluentResourceUsageScheduler ");
		sb.append(name);
		sb.append(" : ");
		sb.append(capacity);
		return sb.toString();
	}

	// Finds sets of overlapping activities and assesses whether they are conflicting (e.g., over-consuming a resource)
	@Override
	protected ConstraintNetwork[] samplingPeakCollection() {

		long horizon = ((FluentNetworkSolver) this.getGroundSolver()).getHorizon();

		if (activities != null && !activities.isEmpty()) {

			Fluent[] groundVars = activities.toArray(new Fluent[activities.size()]);

			Arrays.sort(groundVars, new ChimpSchedulable.FluentActivityComparator(true));

			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();

			HashMap<Fluent,ConstraintNetwork> usages = new HashMap<Fluent,ConstraintNetwork>();

			Vector<Vector<Fluent>> overlappingAll = new Vector<Vector<Fluent>>();

			// this first block checks whether a single activity is over-consuming
			// the resource
			for (Fluent act : activities) {
				if (isConflicting(new Fluent[] {act})) {
					ConstraintNetwork temp = new ConstraintNetwork();
					temp.addVariable(act);
					ret.add(temp);
				}
			}

			//	groundVars are ordered activities
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Fluent> overlapping = new Vector<Fluent>();
				overlapping.add(groundVars[i]);
				long start = (groundVars[i]).getTemporalVariable().getEST();
				long end = (groundVars[i]).getTemporalVariable().getEET();
				if ((groundVars[i]).getTemporalVariable().getLET() == horizon) {  // for open fluents we use horizon instead of EET
					end = horizon;
				}
				TemporalBounds intersection = new TemporalBounds(start, end);
				// starting from act[i] all the forthcoming activities are evaluated to see if they temporally
				// overlaps with act[i]
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = (groundVars[j]).getTemporalVariable().getEST();
						end = (groundVars[j]).getTemporalVariable().getEET();
						if ((groundVars[j]).getTemporalVariable().getLET() == horizon) {     // for open fluents we use horizon instead of EET
							end = horizon;
						}
						TemporalBounds nextInterval = new TemporalBounds(start, end);
						TemporalBounds intersectionNew = intersection.intersectStrict(nextInterval);
						// if act[j] overlaps it is added to the temporary (wrt i) set of activities
						if (intersectionNew != null) {
							overlapping.add(groundVars[j]);
							// the current set of overlapping activities is evaluated to see if
							// the resource capacity is exceeded
							if (isConflicting(overlapping.toArray(new Fluent[overlapping.size()]))) {
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
		return (new ConstraintNetwork[0]);
	}

}
