package htn;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class HTNOperator extends PlanReportroryItem {

	private Logger logger;
	
	
//	public PFD0Operator(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
//			String[][] negativeEffects, VariablePrototype[] positiveEffects, String[] resources, int[] resourceUsages) {
//		super(taskname, arguments, preconditions);
//		this.positiveEffects = positiveEffects;
//	
//		this.logger = MetaCSPLogging.getLogger(PFD0Operator.class);
//	}

	
	public HTNOperator(String taskname, String[] arguments, HTNPrecondition[] preconditions, 
			EffectTemplate[] effects, int preferenceWeight) {
		
		super(taskname, arguments, preconditions, effects, preferenceWeight);

		this.logger = MetaCSPLogging.getLogger(HTNOperator.class);
	}

	
	@Override
	public List<Constraint> expandEffects(Fluent taskfluent, FluentNetworkSolver groundSolver) {
		Vector<Variable> newFluents = new Vector<Variable>();
		List<Constraint> newConstraints = new ArrayList<Constraint>();

		if (effects != null) {
			for(EffectTemplate et : effects) {
				VariablePrototype p = et.getPrototype();
				p.setMarking(HTNMetaConstraint.markings.OPEN);
				newFluents.add(p);
				String[] arguments = (String[])p.getParameters()[2];
				FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS, 
						createConnections(arguments));
				opens.setFrom(taskfluent);
				opens.setTo(p);
				if (et.hasAdditionalConstraints()) {
					opens.setAdditionalConstraints(et.getAdditionalConstraints());
				}
				newConstraints.add(opens);
			}
		}
		return newConstraints;		
	}
	

}
