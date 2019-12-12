package htn;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HTNMethod extends PlanReportroryItem {
	
	private OrderingConstraintTemplate[] orderings;

	
	public HTNMethod(String taskName, String[] arguments, IntArg[] intArgs, HTNPrecondition[] preconditions,
			EffectTemplate[] effects, OrderingConstraintTemplate[] orderings, int preferenceWeight) {
		
		super(taskName, arguments, intArgs, preconditions, effects, preferenceWeight);
		this.orderings = orderings;
	}

	public HTNMethod(String taskName, String[] arguments, HTNPrecondition[] preconditions,
					 EffectTemplate[] effects, OrderingConstraintTemplate[] orderings, int preferenceWeight) {
		this(taskName, arguments, new IntArg[0], preconditions, effects, orderings, preferenceWeight);
	}


	@Override
	public List<Constraint> expandEffects(Fluent taskFluent, FluentNetworkSolver groundSolver) {
		List<Constraint> newConstraints = new ArrayList<Constraint>();
		
		// before constraints from that goal task to another task at the same level.
		List<FluentConstraint> tasksBeforeConstrs = 
				groundSolver.getFluentConstraintsOfTypeFrom(taskFluent, FluentConstraint.Type.BEFORE);
		Set<Variable> subtasksWithoutSuccessor = new HashSet<Variable>();
		
		// create prototypes and decomposition constraints
		if (effects != null) {
			for (EffectTemplate et : effects) {
				VariablePrototype subPrototype = et.getPrototype(groundSolver);
				subPrototype.setMarking(HTNMetaConstraint.markings.UNPLANNED);
				// create dc constraint
				String[] arguments = (String[])subPrototype.getParameters()[2];
				FluentConstraint dc = 
						new FluentConstraint(FluentConstraint.Type.DC, createConnections(arguments));
				dc.setFrom(taskFluent);
				dc.setTo(subPrototype);
				if (et.hasAdditionalConstraints()) {
					dc.setAdditionalConstraints(et.getAdditionalConstraints());
				}
				newConstraints.add(dc);
				
				subtasksWithoutSuccessor.add(subPrototype);
			}
		}
		if (orderings != null) {
			for (OrderingConstraintTemplate ordering : orderings) {
				FluentConstraint fc = ordering.getConstraint(groundSolver);
				newConstraints.add(fc);
				subtasksWithoutSuccessor.remove(fc.getScope()[0]);
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
