package chimp.kernel;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Basic tests for the CHIMP-native kernel types from M15 Step 1.
 */
public class KernelBaseTypesTest {

    // -- Variable --

    @Test
    public void variableHasId() {
        Variable v = new Variable(null, 42);
        assertEquals(42, v.id());
        assertEquals(42, v.getID());
    }

    @Test
    public void variableReferencesSolver() {
        StubSolver solver = new StubSolver();
        Variable v = new Variable(solver, 1);
        assertSame(solver, v.solver());
        assertSame(solver, v.getConstraintSolver());
    }

    @Test
    public void variableMarking() {
        Variable v = new Variable(null, 1);
        assertEquals(0, v.marking());
        v.setMarking(5);
        assertEquals(5, v.marking());
        assertEquals(5, v.getMarking());
    }

    @Test
    public void variableComponent() {
        Variable v = new Variable(null, 1);
        assertNull(v.component());
        v.setComponent("Activity");
        assertEquals("Activity", v.component());
        assertEquals("Activity", v.getComponent());
    }

    @Test
    public void variableReferenceEquality() {
        Variable a = new Variable(null, 1);
        Variable b = new Variable(null, 1); // same id, different object
        assertNotEquals(a, b);
        assertEquals(a, a);
    }

    @Test
    public void variableToStringContainsId() {
        Variable v = new Variable(null, 7);
        assertTrue(v.toString().contains("7"));
    }

    // -- ConstraintNetwork --

    @Test
    public void networkStartsEmpty() {
        ConstraintNetwork net = new ConstraintNetwork();
        assertEquals(0, net.getVariables().length);
        assertEquals(0, net.getConstraints().length);
    }

    @Test
    public void networkAddVariable() {
        ConstraintNetwork net = new ConstraintNetwork();
        Variable v = new Variable(null, 1);
        net.addVariable(v);
        assertEquals(1, net.getVariables().length);
        assertSame(v, net.getVariables()[0]);
    }

    @Test
    public void networkAddAndRemoveConstraint() {
        ConstraintNetwork net = new ConstraintNetwork();
        StubConstraint c = new StubConstraint();
        net.addConstraint(c);
        assertEquals(1, net.getConstraints().length);
        net.removeConstraint(c);
        assertEquals(0, net.getConstraints().length);
    }

    @Test
    public void networkSubstitution() {
        ConstraintNetwork net = new ConstraintNetwork();
        VariablePrototype proto = VariablePrototype.forFluent(
                null, 0, "Task", "!move", new String[] { "r1" });
        Variable real = new Variable(null, 10);
        net.addSubstitution(proto, real);
        assertSame(real, net.getSubstitution(proto));
        assertNull(net.getSubstitution(
                VariablePrototype.forFluent(null, 1, "Task", "!move", new String[]{"r1"})));
    }

    @Test
    public void networkAnnotation() {
        ConstraintNetwork net = new ConstraintNetwork();
        assertNull(net.getAnnotation());
        Object tag = "my-tag";
        net.setAnnotation(tag);
        assertSame(tag, net.getAnnotation());
    }

    @Test
    public void networkReferenceEquality() {
        ConstraintNetwork a = new ConstraintNetwork();
        ConstraintNetwork b = new ConstraintNetwork();
        a.addVariable(new Variable(null, 1));
        b.addVariable(new Variable(null, 1)); // same id
        assertNotEquals(a, b); // not equal despite same content
        assertEquals(a, a);
    }

    // -- VariablePrototype --

    @Test
    public void fluentPrototype() {
        VariablePrototype vp = VariablePrototype.forFluent(
                null, 5, "Activity", "!move_base",
                new String[] { "lab1", "lab1area" });
        assertEquals(VariablePrototype.Kind.FLUENT, vp.prototypeKind());
        assertEquals("Activity", vp.component());
        assertEquals("!move_base", vp.symbol());
        assertArrayEquals(new String[] { "lab1", "lab1area" }, vp.arguments());
        assertEquals(5, vp.id());
    }

    @Test
    public void integerPrototype() {
        VariablePrototype parent = VariablePrototype.forFluent(
                null, 1, "Activity", "!move", new String[] { "r1" });
        VariablePrototype intVp = VariablePrototype.forInteger(null, 2, parent, 3);
        assertEquals(VariablePrototype.Kind.INTEGER, intVp.prototypeKind());
        assertSame(parent, intVp.parentPrototype());
        assertEquals(3, intVp.subIndex());
    }

    @Test
    public void legacyParametersFluent() {
        VariablePrototype vp = VariablePrototype.forFluent(
                null, 0, "Activity", "!move", new String[] { "r1" });
        Object[] params = vp.getParameters();
        assertEquals("Activity", params[0]);
        assertEquals("!move", params[1]);
        assertArrayEquals(new String[] { "r1" }, (String[]) params[2]);
    }

    @Test
    public void legacyParametersInteger() {
        VariablePrototype parent = VariablePrototype.forFluent(
                null, 1, "Activity", "!move", new String[] { "r1" });
        VariablePrototype intVp = VariablePrototype.forInteger(null, 2, parent, 3);
        Object[] params = intVp.getParameters();
        assertSame(parent, params[0]);
        assertEquals(Integer.valueOf(3), params[1]);
    }

    // -- ConstraintSolver --

    @Test
    public void solverVariableManagement() {
        StubSolver solver = new StubSolver();
        // createVariable is solver-subclass-specific; base provides registration
        TestVariable v = new TestVariable(solver, solver.nextVariableId());
        solver.registerVariable(v);
        assertEquals(1, solver.getVariables().length);
        assertSame(v, solver.getVariable(v.id()));
    }

    @Test
    public void solverConstraintManagement() {
        StubSolver solver = new StubSolver();
        StubConstraint c = new StubConstraint();
        assertTrue(solver.addConstraint(c));
        assertEquals(1, solver.getConstraints().length);
        solver.removeConstraint(c);
        assertEquals(0, solver.getConstraints().length);
    }

    @Test
    public void solverAddConstraintsArray() {
        StubSolver solver = new StubSolver();
        StubConstraint c1 = new StubConstraint();
        StubConstraint c2 = new StubConstraint();
        assertTrue(solver.addConstraints(new Constraint[] { c1, c2 }));
        assertEquals(2, solver.getConstraints().length);
    }

    // -- helpers --

    private static class StubSolver extends ConstraintSolver {
        @Override
        public boolean propagate() { return true; }
    }

    private static class StubConstraint extends Constraint {
        @Override
        public Constraint clone() { return new StubConstraint(); }
        @Override
        public boolean isEquivalent(Constraint other) { return false; }
        @Override
        public String getEdgeLabel() { return "stub"; }
    }

    private static class TestVariable extends Variable {
        TestVariable(ConstraintSolver solver, int id) { super(solver, id); }
    }
}
