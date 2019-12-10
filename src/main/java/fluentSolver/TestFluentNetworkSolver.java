package fluentSolver;

import java.util.logging.Logger;

import integers.IntegerConstraint;
import integers.IntegerVariable;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestFluentNetworkSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestFluentNetworkSolver.class);
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
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
		logger.info("Test1");
		FluentNetworkSolver solver = new FluentNetworkSolver(0, 500, symbols, new int[] {1,1,1,1,1}, 0, 1000, 2);
		logger.info("Test2");
		Fluent[] fluents = (Fluent[]) solver.createVariables(3);
		logger.info("Test3");
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		fluents[0].setName("get_mug(mug1 pl1 none none)");
		fluents[1].setName("get_mug(mug1 ?pl1 none none)");
		fluents[2].setName("robotat(none ?pl none none)");
		

//		SimpleBooleanValueConstraint con2 = new SimpleBooleanValueConstraint(Type.UNARYTRUE);
//		con2.setFrom(fluents[1]);
//		con2.setTo(fluents[1]);
//		logger.info("Added con2? " + bsolver.addConstraint(con2));
		
//		try { Thread.sleep(5000); }
//		catch (InterruptedException e) { e.printStackTrace(); }
		
//		SimpleBooleanValueConstraint con1 = new SimpleBooleanValueConstraint(Type.EQUALS);
//		con1.setFrom(fluents[0]);
//		con1.setTo(fluents[1]);
//		logger.info("Added con1? " + bsolver.addConstraint(con1));

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		CompoundSymbolicValueConstraint ncon0 =
				new CompoundSymbolicValueConstraint(CompoundSymbolicValueConstraint.Type.MATCHES);
		ncon0.setFrom(fluents[0].getCompoundSymbolicVariable());
		ncon0.setTo(fluents[1].getCompoundSymbolicVariable());
		CompoundSymbolicVariableConstraintSolver nsolver =
				(CompoundSymbolicVariableConstraintSolver) solver.getConstraintSolvers()[0];
		logger.info("Added ncon0? " + nsolver.addConstraint(ncon0));

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		FluentConstraint fcon01 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon01.setFrom(fluents[0]);
		fcon01.setTo(fluents[1]);
		logger.info("Added fcon01? " + solver.addConstraint(fcon01));

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		FluentConstraint fcon21 = new FluentConstraint(FluentConstraint.Type.PRE, new int[] {1,1, 2, 2});
		fcon21.setFrom(fluents[2]);
		fcon21.setTo(fluents[1]);
		logger.info("Added fcon21? " + solver.addConstraint(fcon21));

		fluents[1].getIntegerVariables()[0].setConstantValue(17);
		fluents[1].getIntegerVariables()[1].setConstantValue(18);
		IntegerConstraint ic01 = new IntegerConstraint(IntegerConstraint.Type.ARITHM,
				new IntegerVariable[]{fluents[0].getIntegerVariables()[0], fluents[1].getIntegerVariables()[0]},
				"=", "+", 23);
		logger.info("Added ic01? " + solver.getConstraintSolvers()[2].addConstraint(ic01));
		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		
//		FluentConstraint fcon11 = new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED, new HTNMethod(null, null, null, null, null, 0));
//		fcon11.setFrom(fluents[1]);
//		fcon11.setTo(fluents[1]);
//		logger.info("Added fcon11? " + solver.addConstraint(fcon11));
//
//		logger.info("Removing fcon11");
//		solver.removeConstraint(fcon11);
//
//		logger.info("Removing fcon21");
//		solver.removeConstraint(fcon21);
		
	}

}
