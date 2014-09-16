package unify;

import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundNameMatchingConstraint.Type;

public class TestCompoundNameMatchingConstraintSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestCompoundNameMatchingConstraintSolver.class);
		
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
		CompoundNameMatchingConstraintSolver solver = new CompoundNameMatchingConstraintSolver(symbols, new int[] {1,1,1,1,1});
		
		
		logger.info("Start");
		
		long startTimeFull = System.nanoTime();
		
		CompoundNameVariable var0 = (CompoundNameVariable) solver.createVariable();
		var0.setName("on",  "mug1", "pl1", "none", "none");
//		var0.setDomainAtPosition(0,  new String[] {"on", "robotat"});
//		var0.setDomainAtPosition(1,  new String[] {"mug1", "mug2"});
//    var0.setDomainAtPosition(2,  new String[] {"pl1"});
//    var0.setDomainAtPosition(3,  new String[] {"none"});
//    var0.setDomainAtPosition(4,  new String[] {"none"});
		logger.info("Created var0");
		CompoundNameVariable var1 = (CompoundNameVariable) solver.createVariable();
    var1.setName("on", "mug1", "pl1", "none", "none");
		logger.info("Created var1");
		
//		CompoundNameVariable var2 = (CompoundNameVariable) solver.createVariable();
//		var2.setDomainAtPosition(0,  new String[] {"on", "robotat"});
//		var2.setDomainAtPosition(1,  new String[] {"mug1", "mug2"});
//		var2.setDomainAtPosition(2,  new String[] {"pl1"});
//    var2.setDomainAtPosition(3,  new String[] {"none"});
//    var2.setDomainAtPosition(4,  new String[] {"none"});
//		logger.info("Created var2");
		CompoundNameVariable var3 = (CompoundNameVariable) solver.createVariable();
		var3.setName("on", "mug2", "none", "none", "none");
		logger.info("Created var3");
		
		logger.info("Created internal variables");
		
		
		CompoundNameVariable var4 = (CompoundNameVariable) solver.createVariable();
		
		CompoundNameVariable var5 = (CompoundNameVariable) solver.createVariable();
		
		NameMatchingConstraintSolver symbolicSolver = 
				(NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
//		ConstraintNetwork.draw(symbolicSolver.getConstraintNetwork());
		
		CompoundNameMatchingConstraint con01 = new CompoundNameMatchingConstraint(Type.MATCHES);
		con01.setFrom(var0);
		con01.setTo(var1);
		
		CompoundNameMatchingConstraint con34 = new CompoundNameMatchingConstraint(Type.MATCHES);
		con34.setFrom(var3);
		con34.setTo(var4);
		
		CompoundNameMatchingConstraint con45 = new CompoundNameMatchingConstraint(Type.MATCHES);
		con45.setFrom(var4);
		con45.setTo(var5);
		
		Vector<CompoundNameMatchingConstraint> cons = new Vector<CompoundNameMatchingConstraint>();
		cons.add(con01);
		cons.add(con34);
		cons.add(con45);
		
		CompoundNameVariable varFrom = var5;
//		CompoundNameVariable varFrom = (CompoundNameVariable) solver.createVariable();
		for (int i = 0; i < 200; i++) {
			CompoundNameVariable varTo = (CompoundNameVariable) solver.createVariable();
//			varTo.setName(new String[] {"on", "mug2", "none", "none", "none"});
			CompoundNameMatchingConstraint con = new CompoundNameMatchingConstraint(Type.MATCHES);
			con.setFrom(varFrom);
			con.setTo(varTo);
			cons.add(con);
			varFrom = varTo;
		}
		
		long startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundNameMatchingConstraint[cons.size()]));
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Everything Took "+((endTime - startTimeFull) / 1000000) + " ms"); 
		
		
		cons.clear();
		
//		CompoundNameMatchingConstraint con02 = new CompoundNameMatchingConstraint(Type.MATCHES);
//		con02.setFrom(var0);
//		con02.setTo(var2);
//		cons.add(con02);
		startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundNameMatchingConstraint[cons.size()]));
		endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		
		logger.info("Finished");
		
		CompoundNameVariable varA = (CompoundNameVariable) solver.createVariable();
//		varA.setName("on", "mug2", "none", "?none", "?none");
		CompoundNameVariable varB = (CompoundNameVariable) solver.createVariable();
//		varB.setName("on", "mug2", "none", "?none", "?none");
		CompoundNameMatchingConstraint con = new CompoundNameMatchingConstraint(Type.MATCHES);
		con.setFrom(varA);
		con.setTo(varB);
		startTime = System.nanoTime();
		System.out.println("Add last constraint? " + solver.addConstraint(con));
		endTime = System.nanoTime();
		System.out.println("Adding last constraint took "+((endTime - startTime) / 1000000) + " ms"); 
		logger.info("DONE");
		System.out.println("VarA: ");
		System.out.println(varA);
		System.out.println("VarB: ");
		System.out.println(varB);
		

	}

}
