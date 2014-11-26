package pfd0Symbolic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;


public class PFD0Method extends PlanReportroryItem {
	
	private VariablePrototype[] subtaskprototypes;
	private Constraint[] constraints;

	
	public PFD0Method(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			Map<String, VariablePrototype> effectsMap, Constraint[] constraints) {
		
		super(taskname, arguments, preconditions, effectsMap);
		this.subtaskprototypes = effectsMap.values().toArray(new VariablePrototype[effectsMap.size()]);
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
		if (subtaskprototypes != null) {
			for (VariablePrototype sub : subtaskprototypes) {
				sub.setMarking(markings.UNPLANNED);
				// create dc constraint
				String[] arguments = (String[])((VariablePrototype) sub).getParameters()[2];
				FluentConstraint dc = 
						new FluentConstraint(FluentConstraint.Type.DC, createConnections(arguments));
				dc.setFrom(taskfluent);
				dc.setTo(sub);
				newConstraints.add(dc);
				
				subtasksWithoutSuccessor.add(sub);
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
