package chimp.adapter.metacsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariableConstraintSolver;

public class MetaCspSymbolicConstraintAdapterTest {

    private final MetaCspSymbolicConstraintAdapter adapter = new MetaCspSymbolicConstraintAdapter();

    private Variable[] newVariables(int n) {
        CompoundSymbolicVariableConstraintSolver solver =
                new CompoundSymbolicVariableConstraintSolver(new String[][] {{"a", "b"}}, new int[] {1, 1});
        return solver.createVariables(n);
    }

    @Test
    public void createsPositiveValueRestriction() {
        Variable[] vars = newVariables(1);
        int[] indices = {0};
        String[][] restrictions = {{"x", "y"}};

        Constraint con = adapter.createPositiveValueRestriction(vars[0], indices, restrictions);

        assertEquals(CompoundSymbolicValueConstraint.Type.POSITIVEVALUERESTRICTION,
                ((CompoundSymbolicValueConstraint) con).getType());
        assertEquals(vars[0], con.getScope()[0]);
    }

    @Test
    public void createsNegativeValueRestriction() {
        Variable[] vars = newVariables(1);
        int[] indices = {0};
        String[][] restrictions = {{"x"}};

        Constraint con = adapter.createNegativeValueRestriction(vars[0], indices, restrictions);

        assertEquals(CompoundSymbolicValueConstraint.Type.NEGATIVEVALUERESTRICTION,
                ((CompoundSymbolicValueConstraint) con).getType());
        assertEquals(vars[0], con.getScope()[0]);
    }

    @Test
    public void createsSubDifferent() {
        Variable[] vars = newVariables(2);
        int[] connections = {0, 0};

        Constraint con = adapter.createSubDifferent(vars[0], vars[1], connections);

        assertEquals(CompoundSymbolicValueConstraint.Type.SUBDIFFERENT,
                ((CompoundSymbolicValueConstraint) con).getType());
        assertEquals(vars[0], con.getScope()[0]);
        assertEquals(vars[1], con.getScope()[1]);
    }

    @Test
    public void createsSubMatches() {
        Variable[] vars = newVariables(2);
        int[] connections = {0, 0};

        Constraint con = adapter.createSubMatches(vars[0], vars[1], connections);

        assertEquals(CompoundSymbolicValueConstraint.Type.SUBMATCHES,
                ((CompoundSymbolicValueConstraint) con).getType());
        assertEquals(vars[0], con.getScope()[0]);
        assertEquals(vars[1], con.getScope()[1]);
    }
}
