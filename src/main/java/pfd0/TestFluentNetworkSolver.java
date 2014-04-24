package pfd0;

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0.SimpleBooleanValueConstraint.Type;
import unify.NameMatchingConstraint;
import unify.NameVariable;

public class TestFluentNetworkSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestFluentNetworkSolver.class);
		FluentNetworkSolver solver = new FluentNetworkSolver(0, 500);
		Fluent[] fluents = (Fluent[]) solver.createVariables(2);
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		
		SimpleBooleanValueConstraint con0 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con0.setFrom(fluents[0]);
		con0.setTo(fluents[0]);
		logger.info("Added con0? " + solver.addConstraint(con0));
		
		((NameVariable) fluents[0].getNameVariable()).setName("On mug1 counter1");
		((NameVariable) fluents[1].getNameVariable()).setName("On mug1 counter1");
		

		SimpleBooleanValueConstraint con2 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
		con2.setFrom(fluents[1]);
		con2.setTo(fluents[1]);
		logger.info("Added con2? " + solver.addConstraint(con2));
		
//		try { Thread.sleep(5000); }
//		catch (InterruptedException e) { e.printStackTrace(); }
		
		SimpleBooleanValueConstraint con1 = new SimpleBooleanValueConstraint(Type.EQUALS);
		con1.setFrom(fluents[0]);
		con1.setTo(fluents[1]);
		logger.info("Added con1? " + solver.addConstraint(con1));
		
		NameMatchingConstraint ncon0 = new NameMatchingConstraint();
		ncon0.setFrom(fluents[0]);
		ncon0.setTo(fluents[1]);
		logger.info("Added ncon0? " + solver.addConstraint(ncon0));
		
		FluentConstraint fcon = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon.setFrom(fluents[0]);
		fcon.setTo(fluents[1]);
//		logger.info("Added fcon? " + solver.addConstraint(fcon));

	}

}
