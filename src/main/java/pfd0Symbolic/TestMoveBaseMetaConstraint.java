package pfd0Symbolic;

import java.util.ArrayList;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestMoveBaseMetaConstraint {
	
	private static PFD0Planner planner;
	private static FluentNetworkSolver fluentSolver;

	public static void main(String[] args) {
		String[] symbolsPredicates = {"on", "robotat", "holding", "get_mug", "!grasp", "!move_base"};
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
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint();
		planner.addMetaConstraint(mbConstraint);
		
		Fluent getmugFluent = (Fluent) fluentSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug(?mug ?pl none none)");
		getmugFluent.setMarking(markings.UNPLANNED);
		
		Fluent mbFluent = (Fluent) fluentSolver.createVariable("Robot1");
		mbFluent.setName("!move_base(none none ma1 pma1)");
		mbFluent.setMarking(markings.UNPLANNED);
		
//		AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(40, 50));
//		deadline.setFrom(getmugFluent);
//		deadline.setTo(getmugFluent);
//		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(deadline));
//		
//		AllenIntervalConstraint getRelCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(10, 20));
//		getRelCon.setFrom(getmugFluent);
//		getRelCon.setTo(getmugFluent);
//		System.out.println("Added*? " + planner.getConstraintSolvers()[0].addConstraints(getRelCon));

		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
//		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork());
		
		MetaCSPLogging.setLevel(Level.FINE);
		
//		System.out.println("Found a plan? " + planner.backtrack());
		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("Found a plan? " + planner.backtrack());				
//				tp.publish(false, true);
				((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
				planner.draw();
			}
		};
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork(), cb);
		
		
		System.out.println(planner.getDescription());
//		ConstraintNetwork.draw(((MultiConstraintSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[1].getConstraintNetwork());
		
		Variable[] vars = fluentSolver.getVariables();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < vars.length; i++) {
			results.add(i, ((Fluent) vars[i]).getCompoundSymbolicVariable().getName());
		}
		System.out.println("Finished");
	}
	

}
