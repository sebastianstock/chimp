package pfd0;

import java.util.logging.Level;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint.Type;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestTaskNetworkSolver {

	public static void main(String[] args) {
		MetaCSPLogging.setLevel(Level.FINEST);
		String[] symbols = new String[] {"A","B","C"};
		TaskNetworkSolver solver = new TaskNetworkSolver(0, 500, symbols);
		Activity act1 = (Activity)solver.createVariable();
		//act1.setSymbolicDomain("A", "B", "C");
//		Activity act2 = (Activity)solver.createVariable();
//		act2.setSymbolicDomain("B", "C", "D");
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con0 = new SymbolicValueConstraint(Type.UNARYEQUALS);
		con0.setUnaryValue(new boolean[] {true, false, false});
		con0.setFrom(act1);
		con0.setTo(act1);
		
		SymbolicValueConstraint con2 = new SymbolicValueConstraint(Type.UNARYEQUALS);
		con2.setUnaryValue(new boolean[] {false,true,false});
		con2.setFrom(act1);
		con2.setTo(act1);
		
		
//		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
//		con1.setFrom(act1);
//		con1.setTo(act2);
//		
//		
//		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
//		con2.setFrom(act1);
//		con2.setTo(act2);
		
		Constraint[] cons = new Constraint[]{con2};
		solver.addConstraints(cons);

	}

}
