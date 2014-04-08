package pfd0;

import java.util.logging.Level;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestTaskNetworkSolver {

	public static void main(String[] args) {
		MetaCSPLogging.setLevel(Level.FINEST);
		TaskNetworkSolver solver = new TaskNetworkSolver(0, 500);
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C", "D");
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		
		Constraint[] cons = new Constraint[]{con1};
		solver.addConstraints(cons);

	}

}
