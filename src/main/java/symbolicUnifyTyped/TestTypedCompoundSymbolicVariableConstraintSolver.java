package symbolicUnifyTyped;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import symbolicUnifyTyped.TypedCompoundSymbolicValueConstraint.Type;


public class TestTypedCompoundSymbolicVariableConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestTypedCompoundSymbolicVariableConstraintSolver.class);
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
		TypedCompoundSymbolicVariableConstraintSolver solver = 
				new TypedCompoundSymbolicVariableConstraintSolver(symbols);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		logger.setLevel(Level.FINEST);
		
		MetaCSPLogging.setLevel(Level.FINEST);
		logger.info("Start");
		
		TypedCompoundSymbolicVariable var0 = (TypedCompoundSymbolicVariable) solver.createVariable();
		var0.setDomainAtPosition(0,  new String[] {"on", "robotat"});
		var0.setDomainAtPosition(1,  new String[] {"mug1", "mug2"});
    var0.setDomainAtPosition(2,  new String[] {"pl1"});
    var0.setDomainAtPosition(3,  new String[] {"none"});
    var0.setDomainAtPosition(4,  new String[] {"none"});
		logger.info("Created var0");
		TypedCompoundSymbolicVariable var1 = (TypedCompoundSymbolicVariable) solver.createVariable();
    var1.setName(new String[] {"on", "mug1", "pl1", "none", "none"});
		logger.info("Created var1");
		
		TypedCompoundSymbolicVariable var2 = (TypedCompoundSymbolicVariable) solver.createVariable();
		var2.setDomainAtPosition(0,  new String[] {"on", "robotat"});
		var2.setDomainAtPosition(1,  new String[] {"mug1", "mug2"});
		var2.setDomainAtPosition(2,  new String[] {"none"});
    var2.setDomainAtPosition(3,  new String[] {"none"});
    var2.setDomainAtPosition(4,  new String[] {"none"});
		logger.info("Created var2");
		TypedCompoundSymbolicVariable var3 = (TypedCompoundSymbolicVariable) solver.createVariable();
		var3.setName(new String[] {"on", "mug2", "none", "none", "none"});
		logger.info("Created var3");
		
		logger.info("Created internal variables");
		
		
		SymbolicVariableConstraintSolver symbolicSolver = 
				(SymbolicVariableConstraintSolver) solver.getConstraintSolvers()[0];
		ConstraintNetwork.draw(symbolicSolver.getConstraintNetwork());
		
		TypedCompoundSymbolicValueConstraint con01 = new TypedCompoundSymbolicValueConstraint(Type.MATCHES);
		con01.setFrom(var0);
		con01.setTo(var1);
//		solver.addConstraint(con01);
		
	}

}
