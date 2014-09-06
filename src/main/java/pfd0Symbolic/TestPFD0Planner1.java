package pfd0Symbolic;

import java.util.ArrayList;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.time.Bounds;
import org.metacsp.time.TimePoint;
import org.metacsp.utility.UI.Callback;
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
		
		planner = new PFD0Planner(0,  600,  0, symbols, new int[] {1,1,1,1,1});
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		test();
	}
	
	private static void test() {
		PreconditionMetaConstraint preConstraint = new PreconditionMetaConstraint();
		planner.addMetaConstraint(preConstraint);
		
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		TaskApplicationMetaConstraint applicationConstraint = new TaskApplicationMetaConstraint();
		addMethods(selectionConstraint, fluentSolver);
		addOperators(selectionConstraint, fluentSolver);	
		planner.addMetaConstraint(selectionConstraint);
		
		planner.addMetaConstraint(applicationConstraint);
		
		Fluent getmugFluent = (Fluent) fluentSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(?mug ?pl none none)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(40, 50));
		deadline.setFrom(getmugFluent);
		deadline.setTo(getmugFluent);
		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(deadline));
		
		AllenIntervalConstraint getRelCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(10, 20));
		getRelCon.setFrom(getmugFluent);
		getRelCon.setTo(getmugFluent);
		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(getRelCon));
		
		
//		Fluent driveFluent = (Fluent) fluentSolver.createVariable("Robot1");
//		driveFluent.setName("!drive(none pl2 none none)");
//		driveFluent.setMarking(markings.UNPLANNED);
		
		createState(fluentSolver);
		
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		MetaCSPLogging.setLevel(Level.FINE);
		
//		System.out.println("Found a plan? " + planner.backtrack());
		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("Found a plan? " + planner.backtrack());				
//				tp.publish(false, true);
				planner.draw();
			}
		};
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
//		planner.draw();
		
//		Fluent f1 = (Fluent)planner.getConstraintSolvers()[0].getVariable(0);
//		Fluent f2 = (Fluent)planner.getConstraintSolvers()[0].getVariable(1);
//		Fluent f3 = (Fluent)planner.getConstraintSolvers()[0].getVariable(2);
//		
//
//		//Says that end time of from (and to) is at least 10 and at most 12
//		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(10,12));
//
//		//Says that start time of from (and to) is at least 10 and at most 12
//		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(10,12));
//		release.setFrom(f1);
//		release.setTo(f1);
//
//		
//		AllenIntervalConstraint allenCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
//		allenCon.setFrom(f1);
//		allenCon.setTo(f2);
//
//		AllenIntervalConstraint allenCon1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
//		allenCon1.setFrom(f2);
//		allenCon1.setTo(f3);
//
//		System.out.println("Added? " + planner.getConstraintSolvers()[0].addConstraints(allenCon, allenCon1, release));
//
//		AllenIntervalConstraint allenCon2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
//		allenCon2.setFrom(f3);
//		allenCon2.setTo(f1);
//
////		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(allenCon2));
//		
//		
//		AllenIntervalConstraint preconstr = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring, AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring.getDefaultBounds());
//		preconstr.setFrom(f2);
//		preconstr.setTo(f1);
//		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(preconstr));
		
		System.out.println(planner.getDescription());
		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[2].getConstraintNetwork());
		
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
		}

		System.out.println(results);
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), "Constraint Network");
//		ConstraintNetwork.draw(((TypedCompoundSymbolicVariableConstraintSolver)fluentSolver.getConstraintSolvers()[0]).getConstraintNetwork(), "Constraint Network");
		// TODO following line makes sure that symbolicvariables values are set, but may take to long if we do that always.
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
//		TypedCompoundSymbolicVariableConstraintSolver compoundS = ((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]);
//		SymbolicVariableConstraintSolver ssolver = (SymbolicVariableConstraintSolver) compoundS.getConstraintSolvers()[2];
//		ConstraintNetwork.draw(ssolver.getConstraintNetwork());
		
		System.out.println("Finished");
	}
	
	public static void createState(FluentNetworkSolver groundSolver) {
		Fluent robotAt = (Fluent) groundSolver.createVariable("RobotAt");
		robotAt.setName("robotat(none pl3 none none)");
		robotAt.setMarking(markings.OPEN);
		
//		Fluent on0 = (Fluent) groundSolver.createVariable("on");
//		on0.setName("on(mug2 pl7 none none)");
//		on0.setMarking(markings.OPEN);
		
		Fluent on1 = (Fluent) groundSolver.createVariable("on");
		on1.setName("on(mug1 pl2 none none)");
		on1.setMarking(markings.OPEN);
	}
	
	public static void addMethods(TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?counter", "none", "none"}, new int[] {0, 0, 1, 1});
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", "!drive", new String[] {"none", "?pl", "none", "none"});
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", "!grasp", new String[] {"?mug", "?pl", "none", "none"});
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(drive);
		before.setTo(grasp);
		PFD0Method getMug1Method = new PFD0Method("get_mug", new String[] {"?mug", "?pl", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				new VariablePrototype[] {grasp, drive}, 
				new FluentConstraint[] {before}
		);
//		getMug1Method.setDurationBounds(new Bounds(10, 40));
		
		selectionConstraint.addMethod(getMug1Method);
		
		
	}
	
	public static void addOperators(TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		// Operator for driving:
		PFD0Precondition robotatPre = 
				new PFD0Precondition("robotat", new String[] {"none", "?pl", "none", "none"}, null); // TODO Add connections
		robotatPre.setNegativeEffect(true);
		VariablePrototype robotatPE = new VariablePrototype(groundSolver, "Component", 
				"robotat", new String[] {"none", "?pl" , "none",  "none"});
		PFD0Operator driveOP = new PFD0Operator("!drive", new String[] {"none", "?pl", "none", "none"}, 
				new PFD0Precondition[]{robotatPre}, 
				null,//new String[] {"robotat(none pl7 none none)"}, 
				new VariablePrototype[] {robotatPE},
				new String[]{},new int[]{}
		);
		driveOP.setDurationBounds(new Bounds(10, 100));
		selectionConstraint.addOperator(driveOP);
		
		// Operator for grasping:
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?pl", "none", "none"}, new int[] {0, 0});
		onPre.setNegativeEffect(true);
		VariablePrototype holdingPE = new VariablePrototype(groundSolver, "Component", "holding", new String[] {"?mug", "none", "none", "none"});
		PFD0Operator graspOp = new PFD0Operator("!grasp", new String[] {"?mug", "none", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				null, //new String[] {"on(mug1 pl2 none none)"}, 
				new VariablePrototype[] {holdingPE},
				new String[]{},new int[]{}
		);
		graspOp.setDurationBounds(new Bounds(10, 100));
		selectionConstraint.addOperator(graspOp);
	}

}
