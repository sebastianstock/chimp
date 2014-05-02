package pfd0;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0.PFD0MetaConstraint.markings;

public class PFD0Operator extends PlanReportroryItem {
	
	private String[] positiveEffects;

	public PFD0Operator(String taskname, String[] positiveEffects) {
		super(taskname);
		this.positiveEffects = positiveEffects;
	}

	
	@Override
	public ConstraintNetwork expand(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
		
		// add positive effects
		if (positiveEffects != null) {
			for(String e : positiveEffects) {
				String composnent = "Component"; // TODO use real component
				VariablePrototype newFluent = new VariablePrototype(groundSolver, composnent, e);
				newFluent.setMarking(markings.OPEN);
				newFluents.add(newFluent);
				FluentConstraint eff = new FluentConstraint(FluentConstraint.Type.EFF);
				eff.setFrom(taskfluent);
				eff.setTo(newFluent);
				newConstraints.add(eff);
			}
		}
		
		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);
		
		return ret;		
	}

}
