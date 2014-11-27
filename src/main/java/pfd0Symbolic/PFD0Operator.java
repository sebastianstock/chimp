package pfd0Symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class PFD0Operator extends PlanReportroryItem {

	private Logger logger;
	
	
//	public PFD0Operator(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
//			String[][] negativeEffects, VariablePrototype[] positiveEffects, String[] resources, int[] resourceUsages) {
//		super(taskname, arguments, preconditions);
//		this.positiveEffects = positiveEffects;
//	
//		this.logger = MetaCSPLogging.getLogger(PFD0Operator.class);
//	}

	
	public PFD0Operator(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			EffectTemplate[] effects) {
		
		super(taskname, arguments, preconditions, effects);

		this.logger = MetaCSPLogging.getLogger(PFD0Operator.class);
	}
	
	// Only used by TaskApplicationMetaConstraint when we have three different meta-constraints.
	@Deprecated
	@Override
	public ConstraintNetwork expandOnlyTail(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
		
		// close negative effects
		for (FluentConstraint con : 
				groundSolver.getFluentConstraintsOfTypeTo(taskfluent, FluentConstraint.Type.PRE) ) {
			if (con.isNegativeEffect()) {
				Fluent dummyPre = (Fluent) con.getFrom();
				List<FluentConstraint> matches = 
						groundSolver.getFluentConstraintsOfTypeTo(dummyPre, FluentConstraint.Type.MATCHES);
				if (matches.size() == 1) {
					Fluent matchingFluent = (Fluent) matches.get(0).getFrom();
					FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
					closes.setFrom(taskfluent);
					closes.setTo(matchingFluent);
					newConstraints.add(closes);
				} else if (matches.size() == 0) {
					logger.info("Trying to set negative effect but" +  dummyPre.toString() 
							+ " has no matches.");
				} else if (matches.size() > 1){
					logger.info("Trying to set negative effect but found more than one matching fluents for precondition " + dummyPre.toString());
				}
			}
		}
		
		// add positive effects
		if (effects != null) {
			for(EffectTemplate et : effects) {
				VariablePrototype p = et.getPrototype();
				p.setMarking(markings.OPEN);
				newFluents.add(p);
				String[] arguments = (String[])((VariablePrototype) p).getParameters()[2];
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
		
		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);
		
		return ret;		
	}
	
	@Override
	public List<Constraint> expandEffectsOneShot(Fluent taskfluent, FluentNetworkSolver groundSolver) {
		Vector<Variable> newFluents = new Vector<Variable>();
		List<Constraint> newConstraints = new ArrayList<Constraint>();

		if (effects != null) {
			for(EffectTemplate et : effects) {
				VariablePrototype p = et.getPrototype();
				p.setMarking(markings.OPEN);
				newFluents.add(p);
				String[] arguments = (String[])((VariablePrototype) p).getParameters()[2];
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
