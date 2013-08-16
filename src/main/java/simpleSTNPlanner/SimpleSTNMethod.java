package simpleSTNPlanner;

import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import simpleSTNPlanner.SimpleSTNDomain.markings;

public class SimpleSTNMethod extends PlanReportroryItem {

	
	protected String[] decompositionActivities;
	//TODO add Constraints for decompositions
	
	public SimpleSTNMethod(String head, 
			AllenIntervalConstraint[] requirementConstraints, 
			String[] requirementActivities,
			String[] decompositionActivities) {
		super(head, requirementConstraints, requirementActivities);
		this.decompositionActivities = decompositionActivities;
	}

	public String[] getDecompositionActivities() {
		return decompositionActivities;
	}

	@Override
	public ConstraintNetwork expand(Activity problematicActivity,
			ActivityNetworkSolver groundSolver,
			Variable[] activeVariables) {
		ConstraintNetwork activityNetworkToReturn = new ConstraintNetwork(null);

		Vector<Variable> operatorTailActivitiesToInsert = new Vector<Variable>();
		Vector<AllenIntervalConstraint> allenIntervalConstraintsToAdd = new Vector<AllenIntervalConstraint>();
		// add positive effects
		if(decompositionActivities != null) {
			for (String effectTail : decompositionActivities) {
				String effectTailComponent = effectTail.substring(0, effectTail.indexOf("::"));
				String effectTailSymbol = effectTail.substring(effectTail.indexOf("::")+2, effectTail.length());

				VariablePrototype tailActivity = new VariablePrototype(groundSolver, effectTailComponent, effectTailSymbol);
				tailActivity.setMarking(markings.NEW);
				operatorTailActivitiesToInsert.add(tailActivity);

				// add constraints
				AllenIntervalConstraint desf = new AllenIntervalConstraint(AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes, AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes.getDefaultBounds());
				desf.setFrom(tailActivity);
				desf.setTo(problematicActivity);
				allenIntervalConstraintsToAdd.add(desf);
			}
		}

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


		return activityNetworkToReturn;
	}
}
