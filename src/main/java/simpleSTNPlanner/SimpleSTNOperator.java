/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package simpleSTNPlanner;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.meta.simplePlanner.InvalidActivityException;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import simpleSTNPlanner.SimpleSTNDomain.markings;

public class SimpleSTNOperator extends PlanReportroryItem{

	protected AllenIntervalConstraint[] positiveEffectConstraints;
	protected String[] positiveEffectActivities;
	protected AllenIntervalConstraint[] negativeEffectConstraints;
	protected String[] negativeEffectActivities;
	protected AllenIntervalConstraint[][] extraConstraints;

	public SimpleSTNOperator(String head, AllenIntervalConstraint[] requirementConstraints, String[] requirementActivities,
			AllenIntervalConstraint[] negativeEffectConstraints, String[] negativeEffectActivities,
			AllenIntervalConstraint[] positiveEffectConstraints, String[] positiveEffectActivities) {
		super(head, requirementConstraints, requirementActivities);

		if (positiveEffectActivities != null)  {
			for (String a : positiveEffectActivities) {
				if (a.equals(head)) throw new InvalidActivityException(a);
			}
		}
		this.positiveEffectActivities = positiveEffectActivities;
		this.positiveEffectConstraints = positiveEffectConstraints;
		this.negativeEffectActivities = negativeEffectActivities;
		this.negativeEffectConstraints = negativeEffectConstraints;
		if (requirementConstraints != null) this.extraConstraints = new AllenIntervalConstraint[requirementActivities.length+1][requirementActivities.length+1];
		else this.extraConstraints = new AllenIntervalConstraint[1][1];
	}
	
	public void addConstraint(AllenIntervalConstraint c, int from, int to) {
		extraConstraints[from][to] = c;
	}
	
	public AllenIntervalConstraint[][] getExtraConstraints() {
		return this.extraConstraints;
	}

	public AllenIntervalConstraint[] getPositiveEffectConstraints() {
		return positiveEffectConstraints;
	}

	public String[] getPositiveEffectActivities() {
		return positiveEffectActivities;
	}

	public AllenIntervalConstraint[] getNegativeEffectConstraints() {
		return negativeEffectConstraints;
	}

	public String[] getNegativeEffectActivities() {
		return negativeEffectActivities;
	}
	
	@Override
	public ConstraintNetwork expand(Activity problematicActivity,
			ActivityNetworkSolver groundSolver,
			Variable[] activeVariables) {		
		ConstraintNetwork activityNetworkToReturn = new ConstraintNetwork(null);
//		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		
		Vector<Variable> operatorTailActivitiesToInsert = new Vector<Variable>();
		
		if (positiveEffectActivities != null ||
				negativeEffectActivities != null ||
				requirementConstraints != null) {
			Vector<AllenIntervalConstraint> allenIntervalConstraintsToAdd = new Vector<AllenIntervalConstraint>();
			// add positive effects
			if(getPositiveEffectActivities() != null) {
				for (String effectTail : getPositiveEffectActivities()) {
					String effectTailComponent = effectTail.substring(0, effectTail.indexOf("::"));
					String effectTailSymbol = effectTail.substring(effectTail.indexOf("::")+2, effectTail.length());

					VariablePrototype tailActivity = new VariablePrototype(groundSolver, effectTailComponent, effectTailSymbol);
					tailActivity.setMarking(markings.ACTIVE);
					operatorTailActivitiesToInsert.add(tailActivity);
				}
			}
            // add constraints for the positive effects
			if (positiveEffectConstraints != null) {
				for (int i = 0; i < positiveEffectConstraints.length; i++) {
					AllenIntervalConstraint con = (AllenIntervalConstraint) positiveEffectConstraints[i].clone();
					con.setFrom(problematicActivity);
					con.setTo(operatorTailActivitiesToInsert.elementAt(i));
					allenIntervalConstraintsToAdd.add(con);
				}
			}
			
			// add negative effects (closes the activities)
			if (negativeEffectActivities != null) {
				for (String effectTail : negativeEffectActivities) {
					int position = searchActivity(effectTail, activeVariables);
					if (position > -1) {
						activeVariables[position].setMarking(markings.CLOSED);
					}
				}
			}
			// add constraints for the negative effects
			// TODO set the constraints
//			if (operator.getPositiveEffectConstraints() != null) {
//				for (int i = 0; i < operator.getPositiveEffectConstraints().length; i++) {
//					AllenIntervalConstraint con = (AllenIntervalConstraint)operator.getPositiveEffectConstraints()[i].clone();
//					con.setFrom(problematicActivity);
//					con.setTo(operatorTailActivitiesToInsert.elementAt(i));
//					allenIntervalConstraintsToAdd.add(con);
//				}
//			}
			
			// add constraints for the preconditions
			if (requirementConstraints != null) {
				for (int i = 0; i < requirementConstraints.length; i++) {
					AllenIntervalConstraint con = (AllenIntervalConstraint)requirementConstraints[i].clone();
					int position = searchActivity(requirementActivities[i], activeVariables);
					con.setFrom(activeVariables[position]);
					con.setTo(problematicActivity);
					allenIntervalConstraintsToAdd.add(con);
				}
			}
			// add constraints to the network
			for (AllenIntervalConstraint con : allenIntervalConstraintsToAdd) activityNetworkToReturn.addConstraint(con);
			
			// TODO is this needed???
			Vector<AllenIntervalConstraint> toAddExtra = new Vector<AllenIntervalConstraint>();
			for (int i = 0; i < operatorTailActivitiesToInsert.size(); i++) {
				AllenIntervalConstraint[][] ec = extraConstraints;
				if (ec != null) {
					AllenIntervalConstraint[] con = ec[i];
					for (int j = 0; j < con.length; j++) {
						if (con[j] != null) {
							AllenIntervalConstraint newCon = (AllenIntervalConstraint) con[j].clone();
							if (i == 0) newCon.setFrom(problematicActivity);
							else newCon.setFrom(operatorTailActivitiesToInsert.elementAt(i-1));
							if (j == 0) newCon.setTo(problematicActivity);
							else newCon.setTo(operatorTailActivitiesToInsert.elementAt(j-1));
							toAddExtra.add(newCon);
						}
					}
				}
			}

			if (!toAddExtra.isEmpty()) {
				for (AllenIntervalConstraint con : toAddExtra) activityNetworkToReturn.addConstraint(con);
			}
		}
		else if (extraConstraints[0][0] != null) { // TODO what is this for?
			AllenIntervalConstraint ec = extraConstraints[0][0];
			AllenIntervalConstraint newCon = (AllenIntervalConstraint) ec.clone();
			newCon.setFrom(problematicActivity);
			newCon.setTo(problematicActivity);
			activityNetworkToReturn.addConstraint(newCon);
		}

		// Sebastian: Commmented because of "usages"
//		if (possibleOperator.getUsages() != null) {
//			String resource = possibleOperatorSymbol.substring(possibleOperatorSymbol.indexOf("(")+1,possibleOperatorSymbol.indexOf(")"));
//			String[] resourceArray = resource.split(",");
//			if (!resource.equals("")) {
//				for (int i = 0; i < resourceArray.length; i++) {
//					String oneResource = resourceArray[i];
////					HashMap<Activity, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(oneResource));
////					utilizers.put(problematicActivity, possibleOperator.getUsages()[i]);
//					activityNetworkToReturn.addVariable(problematicActivity);
//				}
//			}
//		}
		return activityNetworkToReturn;						
	}

}
