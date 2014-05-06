package pfd0;

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0.SimpleBooleanValueConstraint.Type;
import unify.NameMatchingConstraint;
import unify.NameMatchingConstraintSolver;

public class TestFluentNetworkSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestFluentNetworkSolver.class);
		FluentNetworkSolver solver = new FluentNetworkSolver(0, 500);
		Fluent[] fluents = (Fluent[]) solver.createVariables(2);
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		
		SimpleBooleanValueConstraint con0 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con0.setFrom(fluents[0].getSimpleBooleanValueVariable());
		con0.setTo(fluents[0].getSimpleBooleanValueVariable());
		
		SimpleBooleanValueConstraintSolver bsolver = 
				(SimpleBooleanValueConstraintSolver) solver.getConstraintSolvers()[1];
//		ConstraintNetwork.draw(bsolver.getConstraintNetwork());
		logger.info("Added con0? " + bsolver.addConstraint(con0));
		
		fluents[0].setName("On mug1 counter1");
		fluents[1].setName("On mug1 counter1");
		

		SimpleBooleanValueConstraint con2 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con2.setFrom(fluents[1]);
		con2.setTo(fluents[1]);
//		logger.info("Added con2? " + bsolver.addConstraint(con2));
		
//		try { Thread.sleep(5000); }
//		catch (InterruptedException e) { e.printStackTrace(); }
		
		SimpleBooleanValueConstraint con1 = new SimpleBooleanValueConstraint(Type.EQUALS);
		con1.setFrom(fluents[0]);
		con1.setTo(fluents[1]);
//		logger.info("Added con1? " + bsolver.addConstraint(con1));
		
		NameMatchingConstraint ncon0 = new NameMatchingConstraint();
		ncon0.setFrom(fluents[0].getNameVariable());
		ncon0.setTo(fluents[1].getNameVariable());
		NameMatchingConstraintSolver nsolver = 
				(NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
//		logger.info("Added ncon0? " + nsolver.addConstraint(ncon0));
		
		FluentConstraint fcon = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon.setFrom(fluents[0]);
		fcon.setTo(fluents[1]);
		logger.info("Added fcon? " + solver.addConstraint(fcon));

	}

}
