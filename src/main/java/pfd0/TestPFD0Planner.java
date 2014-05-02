package pfd0;

import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0.PFD0MetaConstraint.markings;

public class TestPFD0Planner {

	public static void main(String[] args) {
		
		PFD0Planner planner = new PFD0Planner(0,  600,  0);
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
		
		PFD0MetaConstraint metaConstraint = new PFD0MetaConstraint();
		
		addMethods(metaConstraint);
		addOperators(metaConstraint);
		
		planner.addMetaConstraint(metaConstraint);
		
		Fluent getmugFluent = (Fluent) groundSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug mug1");
		getmugFluent.setMarking(markings.UNPLANNED);
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		System.out.println("Backtrack: " + planner.backtrack());
	}
	
	public static void addMethods(PFD0MetaConstraint metaConstraint) {
		PFD0Method getMug1Method = new PFD0Method("get_mug mug1", new String[] {"!drive counter1", "grasp mug1"});
		metaConstraint.addMethod(getMug1Method);
		
		PFD0Method graspMug1Method = new PFD0Method("grasp mug1", null);
		metaConstraint.addMethod(graspMug1Method);
	}
	
	public static void addOperators(PFD0MetaConstraint metaConstraint) {
		PFD0Operator driveCounter1Op = new PFD0Operator("!drive counter1", new String[] {"RobotAt(counter1"});
		metaConstraint.addOperator(driveCounter1Op);
	}

}
