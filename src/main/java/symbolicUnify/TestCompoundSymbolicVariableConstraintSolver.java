package symbolicUnify;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import symbolicUnify.CompoundSymbolicValueConstraint.Type;


public class TestCompoundSymbolicVariableConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestCompoundSymbolicVariableConstraintSolver.class);
		String[] symbols0 = {"on", "robotat", "mug1", "mug2", "pla1", "pla2", "man1", "mas1", "pmas1", "pman1"};
		String[] symbols1 = {"on", "mug1", "mug2"};
		String[] symbols2 = {"on", "mug1", "mug2", "A", "B", "C", "D"};
		CompoundSymbolicVariableConstraintSolver solver = 
				new CompoundSymbolicVariableConstraintSolver(symbols2, 2);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		logger.setLevel(Level.FINEST);
		
		logger.info("Start");
		
		
		CompoundSymbolicVariable var0 = (CompoundSymbolicVariable) solver.createVariable();
		var0.setDomainAtPosition(0,  new String[] {"on"});
		var0.setDomainAtPosition(1,  new String[] {"mug1", "mug2"});
//		var0.setDomainAtPosition(2,  new String[] {"pla1", "pla2"});
		logger.info("Created var0");
		CompoundSymbolicVariable var1 = (CompoundSymbolicVariable) solver.createVariable();
		var1.setDomainAtPosition(0,  new String[] {"on"});
		var1.setDomainAtPosition(1,  new String[] {"mug1"});
//		var1.setDomainAtPosition(2,  new String[] {"pla2"});
		logger.info("Created var1");

		logger.info("Created internal variables");

		
		SymbolicVariableConstraintSolver symbolicSolver = 
				(SymbolicVariableConstraintSolver) solver.getConstraintSolvers()[0];
		ConstraintNetwork.draw(symbolicSolver.getConstraintNetwork());
		


	}

}
