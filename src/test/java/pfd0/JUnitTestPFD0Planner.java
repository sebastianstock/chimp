package pfd0;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;

import pfd0.PFD0MetaConstraint.markings;

public class JUnitTestPFD0Planner {
	
	private PFD0Planner planner;
	private FluentNetworkSolver fluentSolver;

	@Before
	public void setUp() throws Exception {
		planner = new PFD0Planner(0,  600,  0);
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void test() {
		PFD0MetaConstraint metaConstraint = new PFD0MetaConstraint();
		
		addMethods(metaConstraint, fluentSolver);
		addOperators(metaConstraint);
		
		planner.addMetaConstraint(metaConstraint);
		
		Fluent getmugFluent = (Fluent) fluentSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(mug1)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
//		Fluent getmugFluent2 = (Fluent) groundSolver.createVariable("Robot2");
//		getmugFluent2.setName("get_mug mug1");
//		getmugFluent2.setMarking(markings.UNPLANNED);
		
		createState(fluentSolver);
		
		assertTrue("Backtracking should generate a plan.", planner.backtrack());
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundNameVariable().getName());
		}
		assertTrue(results.contains("Holding(mug1)"));
		assertTrue(results.contains("get_mug(mug1)"));
		assertTrue(results.contains("On(mug1 counter1)"));
		System.out.println(results);
		for (int i = 0; i < results.size(); i++) {
			if (results.get(i).equals("Holding(mug1)")) {
				assertTrue(vars[i].getMarking() == markings.OPEN);
			}
			if (results.get(i).equals("get_mug(mug1)")) {
				assertTrue(vars[i].getMarking() == markings.PLANNED);
			}
			if (results.get(i).equals("On(mug1 counter1)")) {
				assertTrue(vars[i].getMarking() == markings.CLOSED);
			}
		}
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
		VariablePrototype drive = new VariablePrototype(groundSolver, "Component", "!drive(counter1)");
		VariablePrototype grasp = new VariablePrototype(groundSolver, "Component", "!grasp(mug1)");
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
				new PFD0Precondition[]{robotatPre}, 
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
