package htn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.TaskApplicationMetaConstraint.markings;


public class HTNMethod extends PlanReportroryItem {
	
	private Constraint[] constraints;

	
	public HTNMethod(String taskname, String[] arguments, HTNPrecondition[] preconditions, 
			EffectTemplate[] effects, Constraint[] constraints) {
		
		super(taskname, arguments, preconditions, effects);
		this.constraints = constraints;
	}
	
	
	@Override
	public ConstraintNetwork expandOnlyTail(Fluent taskfluent, FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		for (Constraint con : this.expandEffectsOneShot(taskfluent, groundSolver))
			ret.addConstraint(con);

		return ret;
	}


	@Override
	public List<Constraint> expandEffectsOneShot(Fluent taskfluent, FluentNetworkSolver groundSolver) {
		List<Constraint> newConstraints = new ArrayList<Constraint>();
		
		// before constraints from that goal task to another task at the same level.
		List<FluentConstraint> tasksBeforeConstrs = 
				groundSolver.getFluentConstraintsOfTypeFrom(taskfluent, FluentConstraint.Type.BEFORE);
		Set<Variable> subtasksWithoutSuccessor = new HashSet<Variable>();
		
		// create prototypes and decomposition constraints
		if (effects != null) {
			for (EffectTemplate et : effects) {
				VariablePrototype subPrototype = et.getPrototype();
				subPrototype.setMarking(markings.UNPLANNED);
				// create dc constraint
				String[] arguments = (String[])subPrototype.getParameters()[2];
				FluentConstraint dc = 
						new FluentConstraint(FluentConstraint.Type.DC, createConnections(arguments));
				dc.setFrom(taskfluent);
				dc.setTo(subPrototype);
				if (et.hasAdditionalConstraints()) {
					dc.setAdditionalConstraints(et.getAdditionalConstraints());
				}
				newConstraints.add(dc);
				
				subtasksWithoutSuccessor.add(subPrototype);
			}
		}
		if (constraints != null) {
			for (Constraint c : constraints) {
				newConstraints.add(c);
				
				if(c instanceof FluentConstraint 
						&& ((FluentConstraint) c).getType() == FluentConstraint.Type.BEFORE) {
					subtasksWithoutSuccessor.remove(c.getScope()[0]);
				}
			}
		}
		
		for (Variable v : subtasksWithoutSuccessor) {
			for (FluentConstraint c : tasksBeforeConstrs) {
				FluentConstraint bc = 
						new FluentConstraint(FluentConstraint.Type.BEFORE);
				bc.setFrom(v);
				bc.setTo(c.getScope()[1]);
				newConstraints.add(bc);
			}
		}

		return newConstraints;
	}
	

}
