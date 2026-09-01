package htn;

import chimp.kernel.Constraint;
import chimp.kernel.VariablePrototype;
import chimp.kernel.fluent.Fluent;
import chimp.kernel.fluent.FluentConstraint;
import chimp.kernel.fluent.FluentNetworkSolver;
import chimp.logging.ChimpLogging;
import chimp.model.FluentMarking;

import java.util.ArrayList;
import java.util.List;

public class HTNOperator extends PlanReportroryItem {

	
	public HTNOperator(String taskname, String[] arguments, IntArg[] intArgs, HTNPrecondition[] preconditions,
			EffectTemplate[] effects, int preferenceWeight) {
		
		super(taskname, arguments, intArgs, preconditions, effects, preferenceWeight);

		this.logger = ChimpLogging.getLogger(HTNOperator.class);
	}

	public HTNOperator(String taskname, String[] arguments, HTNPrecondition[] preconditions,
					   EffectTemplate[] effects, int preferenceWeight) {
		this(taskname, arguments, new IntArg[0], preconditions, effects, preferenceWeight);
	}

	
	@Override
	public List<Constraint> expandEffects(Fluent taskFluent, FluentNetworkSolver groundSolver) {
		List<Constraint> newConstraints = new ArrayList<Constraint>();

		if (effects != null) {
			for(EffectTemplate et : effects) {
				VariablePrototype p = et.getPrototype(groundSolver);
				p.setMarking(FluentMarking.OPEN.ordinal());
				String[] arguments = (String[])p.getParameters()[2];
				FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS, 
						createConnections(arguments));
				opens.setFrom(taskFluent);
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
