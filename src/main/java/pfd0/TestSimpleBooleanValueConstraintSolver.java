package pfd0;

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0.SimpleBooleanValueConstraint.Type;

public class TestSimpleBooleanValueConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestSimpleBooleanValueConstraintSolver.class);
		SimpleBooleanValueConstraintSolver solver = new SimpleBooleanValueConstraintSolver(0, 500);
		Variable[] vars = solver.createVariables(2);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SimpleBooleanValueConstraint con0 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con0.setFrom(vars[0]);
		con0.setTo(vars[0]);
		//logger.info("Added con0? " + solver.addConstraint(con0));
		solver.addConstraint(con0);
		
		SimpleBooleanValueConstraint con2 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con2.setFrom(vars[1]);
		con2.setTo(vars[1]);
		logger.info("Added con2? " + solver.addConstraint(con2));
		
		SimpleBooleanValueConstraint con1 = new SimpleBooleanValueConstraint(Type.EQUALS);
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);
		logger.info("Added con1? " + solver.addConstraint(con1));

	}

}
