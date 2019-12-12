package htn;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import java.util.ArrayList;
import java.util.List;

public class HTNOperator extends PlanReportroryItem {

	
	public HTNOperator(String taskname, String[] arguments, IntArg[] intArgs, HTNPrecondition[] preconditions,
			EffectTemplate[] effects, int preferenceWeight) {
		
		super(taskname, arguments, intArgs, preconditions, effects, preferenceWeight);

		this.logger = MetaCSPLogging.getLogger(HTNOperator.class);
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
				p.setMarking(HTNMetaConstraint.markings.OPEN);
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
