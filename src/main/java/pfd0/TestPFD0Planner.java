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
		
		Fluent getmugFluent = (Fluent) groundSolver.createVariable("Robot1");
		getmugFluent.setName("get_mug mug1");
		getmugFluent.setMarking(markings.UNPLANNED);
		
		planner.backtrack();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");

	}

}
