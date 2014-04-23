package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;

public class TestNameMatchingConstraintSolver {

	public static void main(String[] args) {
		NameMatchingConstraintSolver solver = new NameMatchingConstraintSolver();
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		NameVariable[] vars = (NameVariable[]) solver.createVariables(3);
		
		
		vars[0].setName("On mug1 counter1");
		vars[1].setName("get_mug mug1");
//		vars[1].setName("On mug1 counter1");
		vars[2].setName("On ?mug ?counter");
		
		
		
		NameMatchingConstraint con0 = new NameMatchingConstraint();
		con0.setFrom(vars[0]);
		con0.setTo(vars[1]);
		
		System.out.println("Scope: " + con0.toString());
		
		
		System.out.println("n0: " + vars[0]);
		System.out.println("n1: " + vars[1]+ ((NameDomain)vars[1].getDomain()).isGrounded());
		System.out.println("n2: " + vars[2] + ((NameDomain)vars[2].getDomain()).isGrounded());
		
		Constraint[] cons = new Constraint[]{con0};
//		System.out.println("Cons: " + cons + " Length: " + cons.length);
		System.out.println("Add con0: " + solver.addConstraints(cons));
		
//		System.out.println("Cons: " + solver.getConstraints().length);

	}

}
