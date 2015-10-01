package unify;

import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicValueConstraint.Type;

public class TestCompoundNameMatchingConstraintSolver {
	private static Logger logger = MetaCSPLogging.getLogger(TestCompoundNameMatchingConstraintSolver.class);
	
	private static String[][] symbols;

	public static void main(String[] args) {
		logger = MetaCSPLogging.getLogger(TestCompoundNameMatchingConstraintSolver.class);
		
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		
		logger.info("Start");
//		testRuntime();
		testRuntimeSingle();
		
	}
	
	public static void testRuntime() {
		CompoundSymbolicVariableConstraintSolver solver = new CompoundSymbolicVariableConstraintSolver(symbols, new int[] {1,1,1,1,1});
		long startTimeFull = System.nanoTime();
		Vector<CompoundSymbolicValueConstraint> cons = new Vector<CompoundSymbolicValueConstraint>();
		
		CompoundSymbolicVariable varFrom = (CompoundSymbolicVariable) solver.createVariable();
		varFrom.setName("on", "mug2", "none", "none", "none");
//		CompoundNameVariable varFrom = (CompoundNameVariable) solver.createVariable();
		for (int i = 0; i < 200; i++) {
			CompoundSymbolicVariable varTo = (CompoundSymbolicVariable) solver.createVariable();
//			varTo.setName("on", new String[] {"mug2", "none", "none", "none"});
			CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(Type.MATCHES);
			con.setFrom(varFrom);
			con.setTo(varTo);
			cons.add(con);
			varFrom = varTo;
		}
		
		long startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundSymbolicValueConstraint[cons.size()]));
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Everything Took "+((endTime - startTimeFull) / 1000000) + " ms"); 
		
		cons.clear();
		
//		CompoundNameMatchingConstraint con02 = new CompoundNameMatchingConstraint(Type.MATCHES);
//		con02.setFrom(var0);
//		con02.setTo(var2);
//		cons.add(con02);
		startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundSymbolicValueConstraint[cons.size()]));
		endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		
		logger.info("Finished");
		
		CompoundSymbolicVariable varA = (CompoundSymbolicVariable) solver.createVariable();
//		varA.setName("on", "mug2", "none", "?none", "?none");
		CompoundSymbolicVariable varB = (CompoundSymbolicVariable) solver.createVariable();
//		varB.setName("on", "mug2", "none", "?none", "?none");
		CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(Type.MATCHES);
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
	
	
	public static void testRuntimeSingle() {
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsargs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10",
				"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10",
				"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10",
				"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[2][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsargs;
		
		CompoundSymbolicVariableConstraintSolver solver = new CompoundSymbolicVariableConstraintSolver(symbols, new int[] {1, 4});
		long startTimeFull = System.nanoTime();
		Vector<CompoundSymbolicValueConstraint> cons = new Vector<CompoundSymbolicValueConstraint>();
		
		CompoundSymbolicVariable varFrom = (CompoundSymbolicVariable) solver.createVariable();
		varFrom.setName("on", "mug2", "none", "none", "none");
//		CompoundNameVariable varFrom = (CompoundNameVariable) solver.createVariable();
		for (int i = 0; i < 200; i++) {
			CompoundSymbolicVariable varTo = (CompoundSymbolicVariable) solver.createVariable();
//			varTo.setName("on", new String[] {"mug2", "none", "none", "none"});
			CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(Type.MATCHES);
			con.setFrom(varFrom);
			con.setTo(varTo);
			cons.add(con);
			varFrom = varTo;
		}
		
		long startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundSymbolicValueConstraint[cons.size()]));
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Everything Took "+((endTime - startTimeFull) / 1000000) + " ms"); 
		
		cons.clear();
		
//		CompoundNameMatchingConstraint con02 = new CompoundNameMatchingConstraint(Type.MATCHES);
//		con02.setFrom(var0);
//		con02.setTo(var2);
//		cons.add(con02);
		startTime = System.nanoTime();
		solver.addConstraints(cons.toArray(new CompoundSymbolicValueConstraint[cons.size()]));
		endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		
		logger.info("Finished");
		
		CompoundSymbolicVariable varA = (CompoundSymbolicVariable) solver.createVariable();
//		varA.setName("on", "mug2", "none", "?none", "?none");
		CompoundSymbolicVariable varB = (CompoundSymbolicVariable) solver.createVariable();
//		varB.setName("on", "mug2", "none", "?none", "?none");
		CompoundSymbolicValueConstraint con = new CompoundSymbolicValueConstraint(Type.MATCHES);
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

	
	public static void testMatches() {
		CompoundSymbolicVariableConstraintSolver solver = new CompoundSymbolicVariableConstraintSolver(symbols, new int[] {1,1,1,1,1});
		
		CompoundSymbolicVariable var0 = (CompoundSymbolicVariable) solver.createVariable();
		var0.setName("on",  "mug1", "pl1", "none", "none");
		logger.info("Created var0");
		CompoundSymbolicVariable var1 = (CompoundSymbolicVariable) solver.createVariable();
    var1.setName("on", "mug1", "pl1", "none", "none");
		logger.info("Created var1");
		
		CompoundSymbolicVariable var3 = (CompoundSymbolicVariable) solver.createVariable();
		var3.setName("on", "mug2", "none", "none", "none");
		logger.info("Created var3");
		
		logger.info("Created internal variables");
		
		
		CompoundSymbolicVariable var4 = (CompoundSymbolicVariable) solver.createVariable();
		
		CompoundSymbolicVariable var5 = (CompoundSymbolicVariable) solver.createVariable();
		
//		NameMatchingConstraintSolver symbolicSolver = 
//				(NameMatchingConstraintSolver) solver.getConstraintSolvers()[0];
//		ConstraintNetwork.draw(symbolicSolver.getConstraintNetwork());
		
		CompoundSymbolicValueConstraint con01 = new CompoundSymbolicValueConstraint(Type.MATCHES);
		con01.setFrom(var0);
		con01.setTo(var1);
		
		CompoundSymbolicValueConstraint con34 = new CompoundSymbolicValueConstraint(Type.MATCHES);
		con34.setFrom(var3);
		con34.setTo(var4);
		
		CompoundSymbolicValueConstraint con45 = new CompoundSymbolicValueConstraint(Type.MATCHES);
		con45.setFrom(var4);
		con45.setTo(var5);
		
		Vector<CompoundSymbolicValueConstraint> cons = new Vector<CompoundSymbolicValueConstraint>();
		cons.add(con01);
		cons.add(con34);
		cons.add(con45);
		solver.addConstraints(cons.toArray(new CompoundSymbolicValueConstraint[cons.size()]));
	}
}
