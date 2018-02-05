package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

import unify.NameMatchingConstraint.Type;

public class TestNameMatchingConstraintSolver {

	public static void main(String[] args) {
		String[] symbols = new String[] {"mug1", "mug2", "mug3"};
		NameMatchingConstraintSolver solver = new NameMatchingConstraintSolver(symbols);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		NameVariable[] vars = (NameVariable[]) solver.createVariables(5);
		
		
		vars[0].setConstant("mug1");
//		vars[3].setConstant("mug2");
		
		NameMatchingConstraint con01 = new NameMatchingConstraint(Type.EQUALS);
		con01.setFrom(vars[0]);
		con01.setTo(vars[1]);
		

		System.out.println("n0: " + vars[0]);
		System.out.println("n1 ground? " + vars[1]+ vars[1].isGround());
		System.out.println("n2 ground? " + vars[2] + vars[2].isGround());
		
		System.out.println("Add con01: " + solver.addConstraint(con01));
		
		
		NameMatchingConstraint con12 = new NameMatchingConstraint(Type.EQUALS);
		con12.setFrom(vars[1]);
		con12.setTo(vars[2]);
//		NameMatchingConstraint con23Equals = new NameMatchingConstraint(Type.EQUALS);
//		con23Equals.setFrom(vars[2]);
//		con23Equals.setTo(vars[3]);
//		System.out.println("Add con12 + con23?" + solver.addConstraints(new Constraint[] {con12, con23Equals}));

		
		NameMatchingConstraint con23Different = new NameMatchingConstraint(Type.DIFFERENT);
		con23Different.setFrom(vars[2]);
		con23Different.setTo(vars[3]);
		System.out.println("Add con12 + con23?" + solver.addConstraints(con12, con23Different));
		
		// Test domain restriction
		NameMatchingConstraint con44 = new NameMatchingConstraint(Type.UNARYEQUALS);
		con44.setFrom(vars[4]);
		con44.setTo(vars[4]);
		con44.setUnaryValues(new int [] {1, 2});
		System.out.println("vars[4] before: " + vars[4]);
		System.out.println("Add con44?" + solver.addConstraints(con44));
		System.out.println("vars[4] after: " + vars[4]);
	}

}
