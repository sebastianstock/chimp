package integers;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import java.util.stream.IntStream;

public class TestChoco {

    public static void main(String[] args) {

//        testNQuens(8);

        testArithmetic();
    }

    private static void testArithmetic() {
        Model model = new Model("arithmetic problem");
        IntVar a = model.intVar("a", 1, 1000, false);
        IntVar b = model.intVar("b", 1, 1000, false);
        model.arithm(a, ">", 10).post();
        model.arithm(a, ">", 10).post();
        model.arithm(a, "<", b, "+", 100).post();

        model.arithm(a, "+", b, ">", 30).post();

        Solver solver = model.getSolver();
        solver.showStatistics();
        Solution solution = solver.findSolution();
        System.out.println(solution.toString());
    }

    private static void testNQuens(int n) {
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        IntVar[] diag1 = IntStream.range(0, n).mapToObj(i -> vars[i].sub(i).intVar()).toArray(IntVar[]::new);
        IntVar[] diag2 = IntStream.range(0, n).mapToObj(i -> vars[i].add(i).intVar()).toArray(IntVar[]::new);
        model.post(
                model.allDifferent(vars),
                model.allDifferent(diag1),
                model.allDifferent(diag2)
        );
        Solver solver = model.getSolver();
        solver.showStatistics();
        solver.setSearch(Search.domOverWDegSearch(vars));
        Solution solution = solver.findSolution();
        if (solution != null) {
            System.out.println(solution.toString());
        }

        System.out.println("var0.lb:" + vars[0].getLB());
        System.out.println("var0.ub:" + vars[0].getUB());
        System.out.println("var0:" + vars[0].toString());

        System.out.println("Solution var[0]: " + solution.getIntVal(vars[0]));
        System.out.println("var0.getValue():" + vars[0].getValue());
        vars[0].isInstantiated();

    }
}
