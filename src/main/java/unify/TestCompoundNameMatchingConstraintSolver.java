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
		
		CompoundNameVariable[] vars = (CompoundNameVariable[]) solver.createVariables(3);
		
		vars[0].setName("On", "mug1", "counter1");
		vars[1].setName("On", "mug1", "counter1");
		vars[2].setName("get_mug",  "mug1");
		
		CompoundNameMatchingConstraint con0 = new CompoundNameMatchingConstraint(Type.MATCHES);
		con0.setFrom(vars[0]);
		con0.setTo(vars[1]);
		
		logger.info("Add con0: " + solver.addConstraint(con0));
		
		NameMatchingConstraintSolver nameSolver = 
				(NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
		ConstraintNetwork.draw(nameSolver.getConstraintNetwork());
		
		NameMatchingConstraint nc0 = new NameMatchingConstraint();
		nc0.setFrom(vars[0].getInternalVariables()[1]);
		nc0.setTo(vars[2].getInternalVariables()[1]);
		logger.info("Add nc0: " + nameSolver.addConstraint(nc0));
		
		NameMatchingConstraint nc1 = new NameMatchingConstraint();
		nc1.setFrom(vars[0].getInternalVariables()[2]);
		nc1.setTo(vars[2].getInternalVariables()[1]);
		logger.info("Add nc1: " + nameSolver.addConstraint(nc1));

	}

}
