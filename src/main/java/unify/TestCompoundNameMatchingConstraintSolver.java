package unify;

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundNameMatchingConstraint.Type;

public class TestCompoundNameMatchingConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestCompoundNameMatchingConstraintSolver.class);
		CompoundNameMatchingConstraintSolver solver = new CompoundNameMatchingConstraintSolver();
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		CompoundNameVariable[] vars = (CompoundNameVariable[]) solver.createVariables(5);
		
		vars[0].setName("On", "mug1", "table1"); // "On" in world state
		vars[1].setName("On", "?mug", "?table"); // "On" as precondition
		vars[2].setName("get_mug",  "?mug");     // "get_mug" task
		vars[3].setName("get_mug", "?mug");      // "grap_mug" task
		vars[4].setName("Holding", "?mug", "rightarm1"); // "holding" effect
		
		CompoundNameMatchingConstraint con0 = new CompoundNameMatchingConstraint(Type.MATCHES);
		con0.setFrom(vars[0]);
		con0.setTo(vars[1]);
		
		int sleeptime = 2000;
		
		try {
			Thread.sleep(sleeptime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.info("Add con0: " + solver.addConstraint(con0));
		
		NameMatchingConstraintSolver nameSolver = 
				(NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
		ConstraintNetwork.draw(nameSolver.getConstraintNetwork());
		
		try {
			Thread.sleep(sleeptime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		NameMatchingConstraint nc0 = new NameMatchingConstraint();
		nc0.setFrom(vars[1].getInternalVariables()[1]);
		nc0.setTo(vars[2].getInternalVariables()[1]);
		logger.info("Add nc0: " + nameSolver.addConstraint(nc0));
		
		try {
			Thread.sleep(sleeptime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		NameMatchingConstraint nc1 = new NameMatchingConstraint();
		nc1.setFrom(vars[3].getInternalVariables()[1]);
		nc1.setTo(vars[2].getInternalVariables()[1]);
		logger.info("Add nc1: " + nameSolver.addConstraint(nc1));
		
		try {
			Thread.sleep(sleeptime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		NameMatchingConstraint nc2 = new NameMatchingConstraint();
		nc2.setFrom(vars[3].getInternalVariables()[1]);
		nc2.setTo(vars[4].getInternalVariables()[1]);
		logger.info("Add nc2: " + nameSolver.addConstraint(nc2));
		
//		NameMatchingConstraint nc1 = new NameMatchingConstraint();
//		nc1.setFrom(vars[0].getInternalVariables()[2]);
//		nc1.setTo(vars[2].getInternalVariables()[1]);
//		logger.info("Add nc1: " + nameSolver.addConstraint(nc1));

	}

}
