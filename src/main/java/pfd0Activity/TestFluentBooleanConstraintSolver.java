package pfd0Activity;

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Activity.FluentBooleanValueConstraint.Type;

public class TestFluentBooleanConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestFluentBooleanConstraintSolver.class);
		FluentBooleanConstraintSolver solver = new FluentBooleanConstraintSolver(0, 500);
		Variable[] vars = solver.createVariables(2);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		FluentBooleanValueConstraint con0 = new FluentBooleanValueConstraint(Type.UNARYTRUE);
		con0.setFrom(vars[0]);
		con0.setTo(vars[0]);
		//logger.info("Added con0? " + solver.addConstraint(con0));
		solver.addConstraint(con0);
		
		FluentBooleanValueConstraint con2 = new FluentBooleanValueConstraint(Type.UNARYFALSE);
		con2.setFrom(vars[1]);
		con2.setTo(vars[1]);
		logger.info("Added con2? " + solver.addConstraint(con2));
		
		FluentBooleanValueConstraint con1 = new FluentBooleanValueConstraint(Type.EQUALS);
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);
		logger.info("Added con1? " + solver.addConstraint(con1));

	}

}
