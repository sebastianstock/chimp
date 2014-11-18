package domains;

import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.Fluent;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.HybridDomain;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import resourceFluent.FluentScheduler;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestHybridDomainParser {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[][] symbols = AAAIDomainSingle.createSymbols();
		int[] ingredients = AAAIDomainSingle.createIngredients();
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);

		HybridDomain dom = new HybridDomain(planner, "domains/testPrimitiveHybridPlanningDomain.ddl");
		
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		selectionConstraint.setOperators(dom.getOperators());
//		Vector<PlanReportroryItem> methods = AAAIDomainSingle.createMethods(fluentSolver);
//		selectionConstraint.setMethods(methods);
		planner.addMetaConstraint(selectionConstraint);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
//		createProblemMoveBase(fluentSolver);
		createProblemPickUpObject(fluentSolver);
//		AAAIProblemsSingle.createProblemMoveTorso(fluentSolver);
		AAAIProblemsSingle.createProblemTuckArms(fluentSolver);
		
		test();
	}
	
	private static void test() {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		MetaCSPLogging.setLevel(Level.FINEST);
		
		long startTime = System.nanoTime();
		System.out.println("Found a plan? " + planner.backtrack());
		long endTime = System.nanoTime();
		
//		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
//		Callback cb = new Callback() {
//			@Override
//			public void performOperation() {
//				long startTime = System.nanoTime();
//				System.out.println("Found a plan? " + planner.backtrack());
//				
//				((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSubFull();
//				long endTime = System.nanoTime();
//				System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
//				planner.draw();
//			}
//		};
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
		
		System.out.println(planner.getDescription());
//		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[2].getConstraintNetwork());
		
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		
		
//		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
//		Variable[] vars = fluentSolver.getVariables();
//		ArrayList<String> results = new ArrayList<String>();
//		for (int i = 0; i < vars.length; i++) {
//			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
//		}
//
//		System.out.println(results);
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), "Constraint Network");
//		ConstraintNetwork.draw(((TypedCompoundSymbolicVariableConstraintSolver)fluentSolver.getConstraintSolvers()[0]).getConstraintNetwork(), "Constraint Network");
		// TODO following line makes sure that symbolicvariables values are set, but may take to long if we do that always.
		
//		((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();

		//		TypedCompoundSymbolicVariableConstraintSolver compoundS = ((TypedCompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]);
//		SymbolicVariableConstraintSolver ssolver = (SymbolicVariableConstraintSolver) compoundS.getConstraintSolvers()[2];
//		ConstraintNetwork.draw(ssolver.getConstraintNetwork());
		
		System.out.println("Finished");
	}
	
	public static void createProblemMoveBase(FluentNetworkSolver fluentSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = fluentSolver.createVariables(1);
		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaEastCounter1)");
//		((Fluent) stateVars[0]).setName("RobotAt(preManipulationAreaSouthTable1)");
		stateVars[0].setMarking(markings.OPEN);
		
		// task
		Fluent mbFluent = (Fluent) fluentSolver.createVariable("Task1");
		mbFluent.setName("!move_base(preManipulationAreaSouthTable1)");
		mbFluent.setMarking(markings.UNPLANNED);
		
		// task
//		Fluent mbbFluent = (Fluent) fluentSolver.createVariable("Task2");
//		mbbFluent.setName("!move_base_blind(manipulationAreaSouthTable1)");
//		mbbFluent.setMarking(markings.UNPLANNED);
		
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1,1));
		release.setFrom(mbFluent);
		release.setTo(mbFluent);
		fluentSolver.addConstraint(release);
		AllenIntervalConstraint mbDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,20));
		mbDuration.setFrom(mbFluent);
		mbDuration.setTo(mbFluent);
		fluentSolver.addConstraint(mbDuration);
//		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(100,100));
//		deadline.setFrom(mbFluent);
//		deadline.setTo(mbFluent);
//		fluentSolver.addConstraint(deadline);
		
		
//		AllenIntervalConstraint mbbDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,10));
//		mbbDuration.setFrom(mbbFluent);
//		mbbDuration.setTo(mbbFluent);
//		fluentSolver.addConstraint(mbbDuration);
		
//		FluentConstraint before = new FluentConstraint(FluentConstraint.Type.BEFORE);
//		before.setFrom(mbFluent);
//		before.setTo(mbbFluent);
//		fluentSolver.addConstraint(before);
	}
	
	public static void createProblemPickUpObject(FluentNetworkSolver groundSolver) {
		// State
		// 0:Predicate 1:Mug 2:Mug 3:PlArea 4:MArea 5:MArea 6:Furniture 7:Guest 8:Arm 9:Arm 10:Posture 11:Posture
		Variable[] stateVars = groundSolver.createVariables(11);
		((Fluent) stateVars[0]).setName("On(mug1 placingAreaWestRightTable1)");
		
		((Fluent) stateVars[1]).setName("RobotAt(manipulationAreaSouthTable1)");
		
		((Fluent) stateVars[2]).setName("Connected(placingAreaEastRightCounter1 manipulationAreaEastCounter1 preManipulationAreaEastCounter1)");
		((Fluent) stateVars[3]).setName("Connected(placingAreaWestLeftTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[4]).setName("Connected(placingAreaEastLeftTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[5]).setName("Connected(placingAreaWestRightTable1 manipulationAreaSouthTable1 preManipulationAreaSouthTable1)");
		((Fluent) stateVars[6]).setName("Connected(placingAreaEastRightTable1 manipulationAreaNorthTable1 preManipulationAreaNorthTable1)");
		((Fluent) stateVars[7]).setName("Connected(placingAreaNorthLeftTable2 manipulationAreaEastTable2  preManipulationAreaEastTable2)");
		((Fluent) stateVars[8]).setName("Connected(placingAreaNorthRightTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[9]).setName("Connected(placingAreaSouthLeftTable2 manipulationAreaWestTable2 preManipulationAreaWestTable2)");
		((Fluent) stateVars[10]).setName("Connected(placingAreaSouthRightTable2 manipulationAreaEastTable2 preManipulationAreaEastTable2)");
		for(Variable v : stateVars) {v.setMarking(markings.OPEN);}
		
		// task
		Fluent taskFluent = (Fluent) groundSolver.createVariable("Task1");
		taskFluent.setName("!pick_up_object(mug1 ?area ?manArea leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}

}
