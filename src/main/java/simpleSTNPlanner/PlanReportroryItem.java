package simpleSTNPlanner;

import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.simplePlanner.InvalidActivityException;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

public abstract class PlanReportroryItem {

	protected String head;
	protected AllenIntervalConstraint[] requirementConstraints;
	protected String[] requirementActivities;
	
	public PlanReportroryItem(String head,
			AllenIntervalConstraint[] requirementConstraints, 
			String[] requirementActivities) {
		this.head = head;
		if (requirementActivities != null) {
			for (String a : requirementActivities) {
				if (a.equals(head)) throw new InvalidActivityException(a);
			}
		}
		this.requirementConstraints = requirementConstraints;
		this.requirementActivities = requirementActivities;
	}

	public String getHead() {
		return head;
	}

	public AllenIntervalConstraint[] getRequirementConstraints() {
		return requirementConstraints;
	}

	public String[] getRequirementActivities() {
		return requirementActivities;
	}

	public String toString() {
		String ret = "";
		if (requirementActivities != null) {
			for (int i = 0; i < requirementActivities.length; i++) {
				ret += head + " " + Arrays.toString(requirementConstraints[i].getTypes()) + " " + Arrays.toString(requirementConstraints[i].getBounds()) + " " + requirementActivities[i];
				if (i != requirementActivities.length-1) ret += "\n";
			}
		}
		return ret;
	}
	
	public String getHeadComponent() {
		return head.substring(0, head.indexOf("::"));
	}
	
	public String getHeadSymbol() {
		return head.substring(head.indexOf("::")+2, head.length());
	}
	
	/**
	 * Checks if an Operator can be applied.
	 * It checks the operator's name and its preconditions.
	 * @return
	 */
	public boolean checkApplicability(Activity currentActivity, Variable[] activeVariables) {
		String currentActivitySymbolicDomain = currentActivity.getSymbolicVariable().getSymbols()[0];
		String opHeadComponent = getHeadComponent();
		String opHeadSymbol = getHeadSymbol();
		// check if operator matches the activity
		if (opHeadComponent.equals(currentActivity.getComponent())) {
			if (currentActivitySymbolicDomain.contains(opHeadSymbol)) {
				return checkRequirements(activeVariables);
			}
		}
		return false;
	}
	
	/**
	 * Goes through all the requirementActivities and checks whether they exist in the network and have the marking ACTIVE.
	 */
	public boolean checkRequirements(Variable[] activeVariables) {
		if (this.requirementActivities != null) {
			for (String opTail : this.requirementActivities) {
				if (searchActivity(opTail, activeVariables) < 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Searches for an Activity in an Array of Variables by its String.
	 * @param activityString 
	 * @param vars Array of Variables to search in.
	 * @return Index of the matching Variable.
	 */
	public static int searchActivity(String activityString, Variable[] vars) {
		if (vars != null) {
			String tailComponent = activityString.substring(0, activityString.indexOf("::"));
			String tailSymbol = activityString.substring(activityString.indexOf("::")+2, activityString.length());
			for (int i = 0; i < vars.length; i++) {
				Activity var = (Activity)vars[i];
				if (tailComponent.equals(var.getComponent())) {
					String varSymbolicDomain = var.getSymbolicVariable().getSymbols()[0];
					if (varSymbolicDomain.contains(tailSymbol)) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public abstract ConstraintNetwork expand(Activity problematicActivity,
			ActivityNetworkSolver groundSolver,
			Variable[] activeVariables);
}
