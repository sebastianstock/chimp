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
import symbolicUnifyTyped.CompoundSymbolicVariableConstraintSolver;

public class TestTray {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[] symbolsPredicates = {"on", "robotat", "holding", "get_mug", "!grasp", "!drive", "put_mugs_on_tray", "!place_mug_on_tray"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "none"};
		String[] symbolsPlAreas = {"trayArea1", "pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		
		planner = new PFD0Planner(0,  600,  0, symbols, new int[] {1,2,1,1,1});
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		test();
	}
	
	private static void test() {
//		PreconditionMetaConstraint preConstraint = new PreconditionMetaConstraint();
//		planner.addMetaConstraint(preConstraint);
		
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
//		TaskApplicationMetaConstraint applicationConstraint = new TaskApplicationMetaConstraint();
		addMethods(selectionConstraint, fluentSolver);
		addOperators(selectionConstraint, fluentSolver);	
		planner.addMetaConstraint(selectionConstraint);
		
//		planner.addMetaConstraint(applicationConstraint);
		
		Fluent putTrayFluent = (Fluent) fluentSolver.createVariable("Robot1");
		putTrayFluent.setName("put_mugs_on_tray(mug1 mug2 trayArea1 none none)");
		putTrayFluent.setMarking(markings.UNPLANNED);
		
		
		createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		MetaCSPLogging.setLevel(Level.FINE);
		
		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("Found a plan? " + planner.backtrack());				
				planner.draw();
			}
		};
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
		
		System.out.println(planner.getDescription());
//		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[2].getConstraintNetwork());
		
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
		}

		System.out.println(results);
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), "Constraint Network");
//		ConstraintNetwork.draw(((TypedCompoundSymbolicVariableConstraintSolver)fluentSolver.getConstraintSolvers()[0]).getConstraintNetwork(), "Constraint Network");
		// TODO following line makes sure that symbolicvariables values are set, but may take to long if we do that always.
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
//		TypedCompoundSymbolicVariableConstraintSolver compoundS = ((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]);
//		SymbolicVariableConstraintSolver ssolver = (SymbolicVariableConstraintSolver) compoundS.getConstraintSolvers()[2];
//		ConstraintNetwork.draw(ssolver.getConstraintNetwork());
		
		System.out.println("Finished");
	}
	
	public static void createState(FluentNetworkSolver groundSolver) {
		
		Fluent on1 = (Fluent) groundSolver.createVariable("on");
		on1.setName("on(mug1 none pl2 none none)");
		on1.setMarking(markings.OPEN);
		
		Fluent on2 = (Fluent) groundSolver.createVariable("on");
		on2.setName("on(mug2 none pl2 none none)");
		on2.setMarking(markings.OPEN);
		
		
	}
	
	public static void addMethods( 
			TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug1", "none", "?area", "none", "none"}, new int[] {0, 0});
		VariablePrototype place1 = new VariablePrototype(groundSolver, "Component", "!place_mug_on_tray", new String[] {"mug1", "none", "trayArea1", "none", "none"});
		VariablePrototype place2 = new VariablePrototype(groundSolver, "Component", "!place_mug_on_tray", new String[] {"mug2", "none", "trayArea1", "none", "none"});
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(place1);
		before.setTo(place2);
		PFD0Method putMugsOnTrayMethod = new PFD0Method("put_mugs_on_tray", new String[] {"mug1", "mug2", "?pl", "none", "none"}, 
				null,//				new PFD0Precondition[] {onPre}, 
				new VariablePrototype[] {place1, place2}, 
				new FluentConstraint[] {before}
		);
//		getMug1Method.setDurationBounds(new Bounds(10, 40));

		selectionConstraint.addMethod(putMugsOnTrayMethod);
		
		
	}
	
	public static void addOperators(
			TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		// Operator for driving:
//		PFD0Precondition robotatPre = 
//				new PFD0Precondition("robotat", new String[] {"none", "?pl", "none", "none"}, null); // TODO Add connections
//		robotatPre.setNegativeEffect(true);
//		VariablePrototype robotatPE = new VariablePrototype(groundSolver, "Component", 
//				"robotat", new String[] {"none", "?pl" , "none",  "none"});
//		PFD0Operator placeOP = new PFD0Operator("!place_mug_on_tray", new String[] {"?mug", "none", "?pl", "none", "none"}, 
//				new PFD0Precondition[]{robotatPre}, 
//				null,//new String[] {"robotat(none pl7 none none)"}, 
//				new VariablePrototype[] {robotatPE});
//		placeOP.setDurationBounds(new Bounds(10, 100));
//		pfdConstraint.addOperator(driveOP);
//		taskConstraint.addOperator(driveOP);
		
		// Operator for grasping:
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "none", "?pl", "none", "none"}, new int[] {0, 0});
		onPre.setNegativeEffect(true);
		VariablePrototype onPE = new VariablePrototype(groundSolver, "Component", "on", new String[] {"?mug", "none", "?tray", "none", "none"});
		PFD0Operator placeOp = new PFD0Operator("!place_mug_on_tray", new String[] {"?mug", "none", "?tray", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				null, //new String[] {"on(mug1 pl2 none none)"}, 
				new VariablePrototype[] {onPE},
				null, null
		);
		placeOp.setDurationBounds(new Bounds(10, 100));
		selectionConstraint.addOperator(placeOp);
	}

}
