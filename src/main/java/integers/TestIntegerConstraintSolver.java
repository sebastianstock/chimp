package integers;

public class TestIntegerConstraintSolver {

	public static void main(String[] args) {
		try {
			test();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	public static void test() {
		int minIntValue = 0;
		int maxIntValue = 1000;

		IntegerConstraintSolver solver = new IntegerConstraintSolver(minIntValue, maxIntValue);

		IntegerVariable[] vars = (IntegerVariable[]) solver.createVariables(4);


//		vars[0].setValue(); // TODO how to deal with constants
//		vars[0].setConstant("mug1");

		vars[1].setConstantValue(80);

		IntegerConstraint ic01 = new IntegerConstraint(IntegerConstraint.Type.ARITHM, new IntegerVariable[] {vars[0], vars[1]},
				"+", ">=", 100);
		System.out.println("Add ic01: " + solver.addConstraint(ic01));

		IntegerConstraint ic01b = new IntegerConstraint(IntegerConstraint.Type.ARITHM, new IntegerVariable[] {vars[0], vars[1]},
				"=", "+", 105);
		System.out.println("Add ic01b: " + solver.addConstraint(ic01b));

		IntegerConstraint ic123 = new IntegerConstraint(IntegerConstraint.Type.ARITHM, new IntegerVariable[] {vars[1], vars[2], vars[3]},
				"+", "<");
		System.out.println("Add ic123: " + solver.addConstraint(ic123));

		IntegerConstraint allDifferent = new IntegerConstraint(IntegerConstraint.Type.ALLDIFFERENT, vars);
		System.out.println("Add allDifferent: " + solver.addConstraint(allDifferent));
//
		System.out.println("Remove allDifferent");
		solver.removeConstraint(allDifferent);

//		IntegerConstraint allEqual = new IntegerConstraint(IntegerConstraint.Type.ALLEQUAL, vars);
//		System.out.println("Add allEqual: " + solver.addConstraint(allEqual));

		for (int i = 0; i < vars.length; i++) {
			System.out.println("var[" + i + "] : " + vars[i]);
		}

	}

}
