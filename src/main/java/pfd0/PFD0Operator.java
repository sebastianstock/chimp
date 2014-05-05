package pfd0;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0.PFD0MetaConstraint.markings;

public class PFD0Operator extends PlanReportroryItem {
	
	private String[] positiveEffects;
	private String[] negativeEffects;

	public PFD0Operator(String taskname, String[] preconditions, String[] negativeEffects, String[] positiveEffects) {
		super(taskname, preconditions);
		this.negativeEffects = negativeEffects;
		this.positiveEffects = positiveEffects;
	}

	
	@Override
	public ConstraintNetwork expand(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
		
		// close negative effects
		Fluent[] openFluents = groundSolver.getOpenFluents();
		if (negativeEffects != null) {
			for (String e : negativeEffects) {
				for (Fluent fl : openFluents) {
					if (fl.getNameVariable().getName().equals(e)) {
						fl.setMarking(markings.CLOSED);
						FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
						closes.setFrom(taskfluent);
						closes.setTo(fl);
						newConstraints.add(closes);
					}
				}
			}
		}
		
		// add positive effects
		if (positiveEffects != null) {
			for(String e : positiveEffects) {
				String composnent = "Component"; // TODO use real component
				VariablePrototype newFluent = new VariablePrototype(groundSolver, composnent, e);
				newFluent.setMarking(markings.OPEN);
				newFluents.add(newFluent);
				FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS);
				opens.setFrom(taskfluent);
				opens.setTo(newFluent);
				newConstraints.add(opens);
			}
		}
		
		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);
		
		return ret;		
	}

}
