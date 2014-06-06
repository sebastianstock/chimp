package pfd0;

import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0.PFD0MetaConstraint.markings;

public class TestPFD0Planner {

	public static void main(String[] args) {
		
		PFD0Planner planner = new PFD0Planner(0,  600,  0);
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
		
		PFD0MetaConstraint metaConstraint = new PFD0MetaConstraint();
		
		addMethods(metaConstraint, groundSolver);
		addOperators(metaConstraint);
		
		planner.addMetaConstraint(metaConstraint);
		
		Fluent getmugFluent = (Fluent) groundSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(mug1)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
//		Fluent getmugFluent2 = (Fluent) groundSolver.createVariable("Robot2");
//		getmugFluent2.setName("get_mug mug1");
//		getmugFluent2.setMarking(markings.UNPLANNED);
		
		createState(groundSolver);
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		System.out.println("Backtrack: " + planner.backtrack());
	}
	
	public static void createState(FluentNetworkSolver groundSolver) {
		Fluent robotAt = (Fluent) groundSolver.createVariable("RobotAt");
		robotAt.setName("RobotAt(table1)");
		robotAt.setMarking(markings.OPEN);
		
		Fluent on = (Fluent) groundSolver.createVariable("On");
		on.setName("On(mug1 counter1)");
		on.setMarking(markings.OPEN);
	}
	
	public static void addMethods(PFD0MetaConstraint metaConstraint, 
			FluentNetworkSolver groundSolver) {
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", 
				"!drive", new String[] {"counter1"});
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", 
				"!grasp", new String[] {"mug1"});
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(drive);
		before.setTo(grasp);
		PFD0Method getMug1Method = new PFD0Method("get_mug", new String[] {"mug1"}, 
				null, 
				new VariablePrototype[] {drive, grasp}, 
				new FluentConstraint[] {before});
//		PFD0Method getMug1Method = new PFD0Method("get_mug mug1", null, new String[] {"!drive counter1", "grasp mug1"});
		metaConstraint.addMethod(getMug1Method);
		
//		PFD0Method graspMug1Method = new PFD0Method("grasp mug1", null, new String[] {}); //new String[] {"fail"});
//		metaConstraint.addMethod(graspMug1Method);
		
	}
	
	public static void addOperators(PFD0MetaConstraint metaConstraint) {
		PFD0Precondition robotatPre = new PFD0Precondition("RobotAt", new String[] {"table1"});
		PFD0Operator driveCounter1Op = new PFD0Operator("!drive", new String[] {"counter1"}, 
				new PFD0Precondition[] {robotatPre}, 
				new String[] {"RobotAt(table1)"}, 
				new String[] {"RobotAt(counter1)"});
		metaConstraint.addOperator(driveCounter1Op);
		
		PFD0Precondition onPre = new PFD0Precondition("On", new String[] {"mug1", "counter1"});
		PFD0Operator graspOp = new PFD0Operator("!grasp", new String[] {"mug1"}, 
				new PFD0Precondition[] {onPre}, 
				new String[] {"On(mug1 counter1)"}, 
				new String[] {"Holding(mug1)"});
		metaConstraint.addOperator(graspOp);
	}

}
