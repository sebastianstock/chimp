package domains;

import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.Fluent;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import externalPahtPlanning.LookUpTableDurationEstimator;
import externalPahtPlanning.MoveBaseDurationEstimator;
import externalPahtPlanning.MoveBaseMetaConstraint;

public class TestRACEProblems {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[][] symbols = RACEProblemsSingle.createSymbols();
		int[] ingredients = RACEProblemsSingle.createIngredients();
		planner = new PFD0Planner(0,  600,  0, symbols, ingredients);

		HybridDomain dom;
		try {
			dom = new HybridDomain(planner, "domains/race_domain.ddl");
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = dom.getFluentResourceUsages();
		selectionConstraint.setResourceUsages(fluentResourceUsages);
		planner.addMetaConstraint(selectionConstraint);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
		
//		RACEProblemsSingle.createProblemMoveBase(fluentSolver);
//		RACEProblemsSingle.createProblemMoveBaseBlind(fluentSolver);
		RACEProblemsSingle.createProblemTuckArms(fluentSolver);
		
//		createProblemMoveBase(fluentSolver);
//		createProblemPickUpObject(fluentSolver);
//		AAAIProblemsSingle.createProblemMoveTorso(fluentSolver);
//		AAAIProblemsSingle.createProblemTuckArms(fluentSolver);
//		AAAIProblemsSingle.createProblemAssumeDefaultDrivingPoseM(fluentSolver);
//		AAAIProblemsSingle.createProblemDriveM(fluentSolver);
		
		MetaCSPLogging.setLevel(Level.FINE);
		
		test();
		
		extractPlan();
	}
	
	private static void extractPlan() {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	private static void test() {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		System.out.println("Found a plan? " + planner.backtrack());
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
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
		taskFluent.setName("!pick_up_object(mug1 leftArm1)");
		taskFluent.setMarking(markings.UNPLANNED);
	}

}
