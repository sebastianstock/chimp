package pfd0Symbolic;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import symbolicUnifyTyped.TypedCompoundSymbolicVariableConstraintSolver;

public class TestPFD0BacktrackingWorking {
	
	private static final boolean DRAW = false;
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;
	private static Logger logger = MetaCSPLogging.getLogger(TestBacktrackingPFD0Planner.class);

//	public static void main(String[] args) {
//		MetaCSPLogging.setLevel(Level.FINE);
//		String[] symbolsPredicates = {"on", "robotat", "holding", "get_mug", "!grasp", "!drive", "!put"};
//		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
//		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
//		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
//		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
//		String[][] symbols = new String[5][];
//		symbols[0] = symbolsPredicates;
//		symbols[1] = symbolsMugs;
//		symbols[2] = symbolsPlAreas;
//		symbols[3] = symbolsManAreas;
//		symbols[4] = symbolsPreAreas;
//		
//		planner = new PFD0Planner(0,  600,  0, symbols);
//		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
//		
//		test();
//	}
	
	@Before
	public void setUp() throws Exception {
		String[] symbolsPredicates = {"on", "robotat", "holding", "get_mug", "!grasp", "!drive", "!put"};
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
	}
	
	@Test
	public void test() {
		PreconditionMetaConstraint preConstraint = new PreconditionMetaConstraint();
		planner.addMetaConstraint(preConstraint);
		
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		TaskApplicationMetaConstraint applicationConstraint = new TaskApplicationMetaConstraint();
		addMethods(selectionConstraint, fluentSolver);
		addOperators(selectionConstraint, fluentSolver);	
		planner.addMetaConstraint(selectionConstraint);
		
		planner.addMetaConstraint(applicationConstraint);
		
		Fluent getmugFluent = (Fluent) fluentSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(mug1 pl2 none none)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
		createState(fluentSolver);
		
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		logger.info("Starting Planning");
		long startTime = System.nanoTime();
		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		assertTrue("Found a plan? ",  planner.backtrack());
		long endTime = System.nanoTime();
		long time = (endTime - startTime) / 1000000;
		logger.info("Finished Planning after " + time + " ms");
		assertTrue("Planning should take less than to seconds", time < 2000);
		if (DRAW)
			planner.draw();
		
		
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
		}

		System.out.println(results);
		if (DRAW)
			((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		System.out.println("Finished");
	}
	
	public static void createState(FluentNetworkSolver groundSolver) {
		Fluent robotAt = (Fluent) groundSolver.createVariable("RobotAt");
		robotAt.setName("robotat(none pl3 none none)");
		robotAt.setMarking(markings.OPEN);
		
		Fluent on1 = (Fluent) groundSolver.createVariable("on");
		on1.setName("on(mug1 pl2 none none)");
		on1.setMarking(markings.OPEN);
	}
	
	public static void addMethods( 
			TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		
		PFD0Precondition onPre0 = 
				new PFD0Precondition("on", new String[] {"?mug", "?counter", "none", "none"}, new int[] {0, 0, 1, 1});
		VariablePrototype put0 = new VariablePrototype(groundSolver, "Component", "!put", new String[] {"?mug", "?pl", "none", "none"});
		VariablePrototype drive0 = new VariablePrototype(groundSolver, "Component", "!drive", new String[] {"none", "?pl", "none", "none"});
		VariablePrototype grasp0 = new VariablePrototype(groundSolver, "Component", "!grasp", new String[] {"?mug", "?pl", "none", "none"});
		FluentConstraint before0 = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before0.setFrom(drive0);
		before0.setTo(grasp0);
		FluentConstraint before1 = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before1.setFrom(put0);
		before1.setTo(drive0);
		PFD0Method getMug1Method0 = new PFD0Method("get_mug", new String[] {"?mug", "?pl", "none", "none"}, 
				new PFD0Precondition[] {onPre0}, 
				new VariablePrototype[] {put0}, 
				new FluentConstraint[] {}
		);
		selectionConstraint.addMethod(getMug1Method0);
		
		
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?counter", "none", "none"}, new int[] {0, 0, 1, 1});
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", "!drive", new String[] {"none", "?pl", "none", "none"});
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", "!grasp", new String[] {"?mug", "?pl", "none", "none"});
		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
		before.setFrom(drive);
		before.setTo(grasp);
		PFD0Method getMug1Method = new PFD0Method("get_mug", new String[] {"?mug", "?pl", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				new VariablePrototype[] {drive, grasp},
				new FluentConstraint[] {before}
		);
		selectionConstraint.addMethod(getMug1Method);
	}
	
	public static void addOperators(
			TaskSelectionMetaConstraint selectionConstraint,
			FluentNetworkSolver groundSolver) {
		// Operator for driving:
		PFD0Precondition robotatPre = 
				new PFD0Precondition("robotat", new String[] {"none", "?pl", "none", "none"}, new int[] {1,1}); // TODO Add connections
		VariablePrototype robotatPE = new VariablePrototype(groundSolver, "Component", 
				"robotat", new String[] {"none", "?pl" , "none",  "none"});
		PFD0Operator driveOP = new PFD0Operator("!drive", new String[] {"none", "?pl", "none", "none"}, 
				new PFD0Precondition[]{robotatPre}, 
				null,//new String[] {"robotat(none pl7 none none)"}, 
				new VariablePrototype[] {robotatPE});
		selectionConstraint.addOperator(driveOP);
		
	// Operator for driving:
		PFD0Precondition robotatPre1 = 
				new PFD0Precondition("robotat", new String[] {"none", "?pl", "none", "none"}, null); // TODO Add connections
		PFD0Operator driveOP1 = new PFD0Operator("!drive", new String[] {"none", "?pl", "none", "none"}, 
				new PFD0Precondition[]{robotatPre1}, 
				null,//new String[] {"robotat(none pl7 none none)"}, 
				new VariablePrototype[] {robotatPE});
		selectionConstraint.addOperator(driveOP1);
		
		// Operator for grasping:
		PFD0Precondition onPre = 
				new PFD0Precondition("on", new String[] {"?mug", "?pl", "none", "none"}, new int[] {0, 0});
		onPre.setNegativeEffect(true);
		VariablePrototype holdingPE = new VariablePrototype(groundSolver, "Component", "holding", new String[] {"?mug", "none", "none", "none"});
		PFD0Operator graspOp = new PFD0Operator("!grasp", new String[] {"?mug", "none", "none", "none"}, 
				new PFD0Precondition[] {onPre}, 
				null, //new String[] {"on(mug1 pl2 none none)"}, 
				new VariablePrototype[] {holdingPE}
		);
		selectionConstraint.addOperator(graspOp);
	}

}
