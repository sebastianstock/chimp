package pfd0Symbolic;

import java.util.ArrayList;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import symbolicUnifyTyped.TypedCompoundSymbolicVariableConstraintSolver;

public class TestPFD0Planner1 {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[] symbolsPredicates = {"on", "robotat", "holding", "get_mug", "!grasp", "!drive"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		
		planner = new PFD0Planner(0,  600,  0, symbols);
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		test();
	}
	
	private static void test() {
		PreconditionMetaConstraint preConstraint = new PreconditionMetaConstraint();
		planner.addMetaConstraint(preConstraint);
		
		TaskSelectionMetaConstraint taskConstraint = new TaskSelectionMetaConstraint();
		TaskApplicationMetaConstraint pfd0Constraint = new TaskApplicationMetaConstraint();
		addMethods(pfd0Constraint, taskConstraint, fluentSolver);
		addOperators(pfd0Constraint, taskConstraint);	
		planner.addMetaConstraint(taskConstraint);
		
		planner.addMetaConstraint(pfd0Constraint);
		
		Fluent getmugFluent = (Fluent) fluentSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(mug1 none none none)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
//		Fluent getmugFluent2 = (Fluent) groundSolver.createVariable("Robot2");
//		getmugFluent2.setName("get_mug mug1");
//		getmugFluent2.setMarking(markings.UNPLANNED);
		
//		Fluent driveFluent = (Fluent) fluentSolver.createVariable("Robot1");
//		driveFluent.setName("!drive(none pl1 none none)");
//		driveFluent.setMarking(markings.UNPLANNED);
		
		createState(fluentSolver);
		
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
//		MetaCSPLogging.setLevel(Level.FINE);
		
		System.out.println("Found a plan? " + planner.backtrack());
		
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
		}

		System.out.println(results);
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), "Constraint Network");
		// TODO following line makes sure that symbolicvariables values are set, but may take to long if we do that always.
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		System.out.println("Finished");
	}
	
	public static void createState(FluentNetworkSolver groundSolver) {
		Fluent robotAt = (Fluent) groundSolver.createVariable("RobotAt");
		robotAt.setName("robotat(none pl1 none none)");
		robotAt.setMarking(markings.OPEN);
		
//		Fluent on0 = (Fluent) groundSolver.createVariable("on");
//		on0.setName("on(mug2 pl7 none none)");
//		on0.setMarking(markings.OPEN);
		
		Fluent on1 = (Fluent) groundSolver.createVariable("on");
		on1.setName("on(mug1 pl2 none none)");
		on1.setMarking(markings.OPEN);
	}
	
	public static void addMethods(TaskApplicationMetaConstraint pfdConstraint, 
			TaskSelectionMetaConstraint taskConstraint,
			FluentNetworkSolver groundSolver) {
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?counter", "none", "none"}, new int[] {0, 0});
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", "!drive(none pl2 none none)");
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", "!grasp(mug1 none none none)");
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(drive);
		before.setTo(grasp);
		PFD0Method getMug1Method = new PFD0Method("get_mug", null, 
				new PFD0Precondition[] {onPre}, 
				new VariablePrototype[] {drive, grasp}, 
				new FluentConstraint[] {before}
		);
		pfdConstraint.addMethod(getMug1Method);
		taskConstraint.addMethod(getMug1Method);
		
		
	}
	
	public static void addOperators(TaskApplicationMetaConstraint pfdConstraint, 
			TaskSelectionMetaConstraint taskConstraint) {
		PFD0Precondition robotatPre = 
				new PFD0Precondition("robotat", new String[] {"none", "?pl", "none", "none"}, null); // TODO Add connections
		PFD0Operator driveCounter1Op = new PFD0Operator("!drive", new String[] {"none", "pl2", "none", "none"}, 
				new PFD0Precondition[]{robotatPre}, 
				null,//new String[] {"robotat(none pl7 none none)"}, 
				null);//new String[] {"robotat(none pl8 none none)"});
		pfdConstraint.addOperator(driveCounter1Op);
		taskConstraint.addOperator(driveCounter1Op);
		
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?pl", "none", "none"}, new int[] {0, 0});
		PFD0Operator graspOp = new PFD0Operator("!grasp", new String[] {"?mug"}, 
				new PFD0Precondition[] {onPre}, 
				null, //new String[] {"on(mug1 pl2 none none)"}, 
				null //new String[] {"holding(mug6 none none none)"}
		);
		pfdConstraint.addOperator(graspOp);
		taskConstraint.addOperator(graspOp);
	}

}
