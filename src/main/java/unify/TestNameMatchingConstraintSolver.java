package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

public class TestNameMatchingConstraintSolver {

	public static void main(String[] args) {
		NameMatchingConstraintSolver solver = new NameMatchingConstraintSolver();
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		NameVariable[] vars = (NameVariable[]) solver.createVariables(4);
		
		
		vars[0].setName("mug1");
		vars[1].setName("?m1");
//		vars[1].setName("On mug1 counter1");
		vars[2].setName("?m2");
		vars[3].setName("mug2");
		
		
		
		NameMatchingConstraint con0 = new NameMatchingConstraint();
		con0.setFrom(vars[0]);
		con0.setTo(vars[1]);
		
		NameMatchingConstraint con1 = new NameMatchingConstraint();
		con1.setFrom(vars[1]);
		con1.setTo(vars[2]);
		
		NameMatchingConstraint con2 = new NameMatchingConstraint();
		con2.setFrom(vars[2]);
		con2.setTo(vars[3]);
		
		System.out.println("Scope: " + con0.toString());
		
		
		System.out.println("n0: " + vars[0]);
		System.out.println("n1 ground? " + vars[1]+ vars[1].isGround());
		System.out.println("n2 ground? " + vars[2] + vars[2].isGround());
		
		Constraint[] cons = new Constraint[]{con0, con1};
//		System.out.println("Cons: " + cons + " Length: " + cons.length);
		System.out.println("Add con0 + con1: " + solver.addConstraints(cons));
		
//		System.out.println("Add con1: " + solver.addConstraint(con1));
		
		System.out.println("Add con2: " + solver.addConstraint(con2));
		
//		System.out.println("Cons: " + solver.getConstraints().length);

	}

}
