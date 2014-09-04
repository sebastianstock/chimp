package pfd0Symbolic;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class PFD0Operator extends PlanReportroryItem {
	
	private VariablePrototype[] positiveEffects;
	private Logger logger;
	
	private String[] resources;
	private int[] resourceUsages;
	private HashMap<String, Integer> resourceUsageMap = new HashMap<String, Integer>();
	
	public PFD0Operator(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			String[][] negativeEffects, VariablePrototype[] positiveEffects, String[] resources, int[] resourceUsages) {
		super(taskname, arguments, preconditions);
		this.positiveEffects = positiveEffects;
		this.resources = resources;
		this.resourceUsages = resourceUsages;
		
		if (resourceUsages != null) {
			for (int i = 0; i < resourceUsages.length; i++) {
				resourceUsageMap.put(resources[i], resourceUsages[i]);
			}
		}
		
		this.logger = MetaCSPLogging.getLogger(PFD0Operator.class);
	}

	
	public HashMap<String, Integer> getResourceUsage(){
		return resourceUsageMap;
	}
	
	@Override
	public ConstraintNetwork expandTail(Fluent taskfluent,
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
		if (positiveEffects != null) {
			for(VariablePrototype p : positiveEffects) {
				p.setMarking(markings.OPEN);
				newFluents.add(p);
				String[] arguments = (String[])((VariablePrototype) p).getParameters()[2];
				FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS, 
						createConnections(arguments));
				opens.setFrom(taskfluent);
				opens.setTo(p);
				newConstraints.add(opens);
			}
		}
		
		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);
		
		return ret;		
	}
	

}
