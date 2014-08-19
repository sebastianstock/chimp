package pfd0Symbolic;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.sat4j.core.Vec;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;

public class PFD0Operator extends PlanReportroryItem {
	
	private String[][] positiveEffects;
	private String[][] negativeEffects;

	public PFD0Operator(String taskname, String[] arguments, PFD0Precondition[] preconditions, 
			String[][] negativeEffects, String[][] positiveEffects) {
		super(taskname, arguments, preconditions);
		this.negativeEffects = negativeEffects;
		this.positiveEffects = positiveEffects;
	}

	
	@Override
	public ConstraintNetwork expandTail(Fluent taskfluent,
			FluentNetworkSolver groundSolver) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		
		Vector<Variable> newFluents = new Vector<Variable>();
		Vector<FluentConstraint> newConstraints = new Vector<FluentConstraint>();
//		Vector<FluentConstraint> newConstraints = addPreconditionPrototypes(taskfluent, groundSolver); // This is now done in expandPreconditions
		
		// close negative effects
		Fluent[] openFluents = groundSolver.getOpenFluents();
		if (negativeEffects != null) {
			for (String[] e : negativeEffects) {  // TODO ensure that exactly one fluent is closed
				for (Fluent fl : openFluents) {
					if (fl.getCompoundSymbolicVariable().getName().equals(e)) {
						fl.setMarking(markings.CLOSED);
						FluentConstraint closes = new FluentConstraint(FluentConstraint.Type.CLOSES);
						closes.setFrom(taskfluent);
						closes.setTo(fl);
						newConstraints.add(closes);
					}
				}
			}
		}
		
		// add positive effects
		if (positiveEffects != null) {
			for(String[] e : positiveEffects) {
				String component = "Component"; // TODO use real component
				VariablePrototype newFluent = new VariablePrototype(groundSolver, 
						component, 
						createEffectString(e));
				newFluent.setMarking(markings.OPEN);
				newFluents.add(newFluent);
				System.out.println("");
				FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS, 
						createConnections(e));
				opens.setFrom(taskfluent);
				opens.setTo(newFluent);
				newConstraints.add(opens);
			}
		}
		
		// add constraints to the network
		for (FluentConstraint con : newConstraints)
			ret.addConstraint(con);
		
		return ret;		
	}
	
	/**
	 * Creates the connections array for the opens constraint.
	 * 
	 * Looks at the arguments of effects and operator to see if they should have an equal constraint.
	 * @param e Arguments of the operator.
	 * @return Array representing the connections.
	 */
	private int[] createConnections(String[] e) {
		boolean[] matches = new boolean[e.length-1];
		int matchesCnt = 0;
		for (int i = 0; i < e.length-1; i++) {
			if (e[i+1].startsWith("?") && e[i+1].equals(this.arguments[i])) {
				matches[i] = true;
				matchesCnt++;
			}
		}
		if (matchesCnt > 0) {
			int[] connections = new int[matchesCnt * 2];
			int j = 0;
			for (int i = 0; i < matches.length; i++) {
				if (matches[i])  {
					connections[j] = i;
					connections[j+1] = i;
					j += 2;
				}
			}
			return connections;
		}
		else 
			return null;
	}
	
	private String createEffectString(String[] e) {
		StringBuilder effectSB = new StringBuilder(e[0]);
		effectSB.append('(');
		for (int i = 1; i < e.length; i++) {
			effectSB.append(e[i]);
			effectSB.append(' ');
		}
		effectSB.append(')');
		return effectSB.toString();
	}

}
