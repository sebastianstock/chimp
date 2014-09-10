package pfd0Symbolic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;


public class PFD0Method extends PlanReportroryItem {
	
	private VariablePrototype[] subtaskprototypes;
	private Constraint[] constraints;

	
	public PFD0Method(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			VariablePrototype[] subtaskprototypes, Constraint[] constraints) {
		super(taskname, arguments, preconditions);
		this.subtaskprototypes = subtaskprototypes;
		this.constraints = constraints;
	}
	
	
	@Override
	@Deprecated
	public ConstraintNetwork expandTail(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);

		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<Constraint> newConstraints = new Vector<Constraint>();

		// create prototypes and decomposition constraints
		if (subtaskprototypes == null)
			return ret;
		for (VariablePrototype sub : subtaskprototypes) {
			sub.setMarking(markings.UNPLANNED);
			newFluents.add(sub);
			// create dc constraint
			String[] arguments = (String[])((VariablePrototype) sub).getParameters()[2];
			FluentConstraint dc = 
					new FluentConstraint(FluentConstraint.Type.DC, createConnections(arguments));
			dc.setFrom(taskfluent);
			dc.setTo(sub);
			newConstraints.add(dc);
		}
		if (constraints != null) {
			for (Constraint c : constraints) {
				newConstraints.add(c);
			}
		}
		

		// TODO add fluents for the preconditions
		// TODO add constraints for the preconditions

		// add constraints to the network
		for (Constraint con : newConstraints)
			ret.addConstraint(con);

		return ret;
	}


	@Override
	public List<Constraint> expandEffects(Fluent taskfluent, FluentNetworkSolver groundSolver) {
		List<Variable> newFluents = new ArrayList<Variable>();
		List<Constraint> newConstraints = new ArrayList<Constraint>();
		
		// before constraints from that goal task to another task at the same level.
		List<FluentConstraint> tasksBeforeConstrs = 
				groundSolver.getFluentConstraintsOfTypeFrom(taskfluent, FluentConstraint.Type.BEFORE);

		Set<Variable> subtasksWithoutSuccessor = new HashSet<Variable>();
		
		// create prototypes and decomposition constraints
		if (subtaskprototypes != null) {
			for (VariablePrototype sub : subtaskprototypes) {
				sub.setMarking(markings.UNPLANNED);
				newFluents.add(sub);
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
