package chimp.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

/**
 * Unit tests for {@link ChimpSearchEngine} using stub {@link FlawProvider}
 * and {@link SearchHooks}. Exercises the core search loop: finding flaws,
 * trying resolvers, backtracking, and succeeding.
 *
 * <p>The stubs use empty {@link ConstraintNetwork}s (no constraints), so
 * {@code addResolver}/{@code retractResolver} succeed trivially. The
 * tests verify the search-loop control flow, not ground-solver
 * propagation.
 */
public class ChimpSearchEngineTest {

    /**
     * A simple scenario: one provider returns one flaw with two resolvers.
     * The first resolver resolves the flaw (no further flaws). Search
     * succeeds immediately.
     */
    @Test
    public void findsSolutionWithFirstResolver() {
        RecordingHooks hooks = new RecordingHooks();
        ConstraintNetwork flaw = new ConstraintNetwork(null);
        ConstraintNetwork resolver = new ConstraintNetwork(null);
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of(resolver)));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(1, hooks.addResolverSubCalls);
        assertEquals(0, hooks.retractResolverSubCalls);
        assertEquals(1, engine.getCounterMoves());
    }

    /**
     * Two resolvers: the first creates a new flaw that has no resolvers
     * (dead end), so the engine backtracks and tries the second, which
     * resolves cleanly.
     */
    @Test
    public void backtracksWhenFirstResolverLeadsToDeadEnd() {
        RecordingHooks hooks = new RecordingHooks();
        ConstraintNetwork flaw1 = distinctNetwork(100);
        ConstraintNetwork resolver1 = distinctNetwork(1);
        ConstraintNetwork resolver2 = distinctNetwork(2);
        ConstraintNetwork flaw2 = distinctNetwork(101);

        // flaw1 has two resolvers; applying resolver1 produces flaw2,
        // which has no resolvers (dead end). resolver2 resolves cleanly.
        StubProvider provider = new StubProvider(
                List.of(flaw1, flaw2),
                List.of(List.of(resolver1, resolver2), List.of()));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(2, hooks.addResolverSubCalls);   // resolver1 + resolver2
        assertEquals(1, hooks.retractResolverSubCalls); // retract resolver1
    }

    /**
     * A flaw with no resolvers at all: search fails.
     */
    @Test
    public void failsWhenNoResolvers() {
        RecordingHooks hooks = new RecordingHooks();
        ConstraintNetwork flaw = new ConstraintNetwork(null);
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of()));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertFalse(engine.backtrack());
        assertEquals(0, hooks.addResolverSubCalls);
    }

    /**
     * No flaws at all: search succeeds trivially.
     */
    @Test
    public void succeedsWhenNoFlaws() {
        RecordingHooks hooks = new RecordingHooks();
        StubProvider provider = new StubProvider(List.of(), List.of());
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(0, hooks.addResolverSubCalls);
    }

    /**
     * addResolverSub returning false causes the engine to try the next
     * resolver instead of accepting the current one.
     */
    @Test
    public void rejectedResolverFallsThroughToNext() {
        ConstraintNetwork flaw = distinctNetwork(100);
        ConstraintNetwork rejected = distinctNetwork(1);
        ConstraintNetwork accepted = distinctNetwork(2);
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of(rejected, accepted)));
        final int[] callCount = {0};
        RecordingHooks hooks = new RecordingHooks() {
            @Override
            public boolean addResolverSub(ConstraintNetwork f, ConstraintNetwork r) {
                addResolverSubCalls++;
                callCount[0]++;
                return callCount[0] != 1; // reject first, accept second
            }
        };
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(2, hooks.addResolverSubCalls);
    }

    // --- Stubs ---

    /**
     * A stub flaw provider with a scripted sequence of flaws and
     * resolvers. Each call to {@link #currentFlaw()} returns the next
     * flaw in the list (cycling). Each call to {@link #resolvers}
     * returns the resolvers for the corresponding flaw index.
     */
    private static final class StubProvider implements FlawProvider {
        private final List<ConstraintNetwork> flaws;
        private final List<List<ConstraintNetwork>> resolversPerFlaw;
        private int flawIndex = 0;

        StubProvider(List<ConstraintNetwork> flaws, List<List<ConstraintNetwork>> resolversPerFlaw) {
            this.flaws = flaws;
            this.resolversPerFlaw = resolversPerFlaw;
        }

        @Override
        public ConstraintNetwork currentFlaw() {
            if (flawIndex >= flaws.size()) {
                return null;
            }
            return flaws.get(flawIndex);
        }

        @Override
        public ConstraintNetwork[] resolvers(ConstraintNetwork flaw) {
            int idx = flaws.indexOf(flaw);
            if (idx < 0 || idx >= resolversPerFlaw.size()) {
                return new ConstraintNetwork[0];
            }
            List<ConstraintNetwork> resolvers = resolversPerFlaw.get(idx);
            flawIndex++; // advance so the next currentFlaw() call returns the next flaw
            return resolvers.toArray(new ConstraintNetwork[0]);
        }

        @Override
        public void markResolved(ConstraintNetwork flaw, ConstraintNetwork resolver) {
            // no-op
        }

        @Override
        public Comparator<ConstraintNetwork> resolverOrdering() {
            return null;
        }
    }

    /** Recording hooks that count calls and accept all resolvers. */
    private static class RecordingHooks implements SearchHooks {
        int addResolverSubCalls = 0;
        int retractResolverSubCalls = 0;
        int preBacktrackCalls = 0;
        int postBacktrackCalls = 0;

        @Override
        public void preBacktrack() { preBacktrackCalls++; }

        @Override
        public void postBacktrack(ConstraintNetwork flawNetwork) { postBacktrackCalls++; }

        @Override
        public boolean addResolverSub(ConstraintNetwork flaw, ConstraintNetwork resolver) {
            addResolverSubCalls++;
            return true;
        }

        @Override
        public void retractResolverSub(ConstraintNetwork flaw, ConstraintNetwork resolver) {
            retractResolverSubCalls++;
        }
    }

    // --- Helpers ---

    /**
     * Creates a {@link ConstraintNetwork} with a single unique dummy
     * variable, so that {@link ConstraintNetwork#equals} distinguishes
     * networks by content. (Two empty networks are equal, which breaks
     * JUNG edge identity.)
     */
    private static ConstraintNetwork distinctNetwork(int id) {
        ConstraintNetwork cn = new ConstraintNetwork(null);
        cn.addVariable(new DummyVariable(id));
        return cn;
    }

    /** Minimal concrete Variable for test networks. */
    private static final class DummyVariable extends Variable {
        private final int id;

        DummyVariable(int id) {
            super(null, id);
            this.id = id;
        }

        @Override
        public Domain getDomain() { return null; }

        @Override
        public void setDomain(Domain d) { }

        @Override
        public int compareTo(Variable o) { return Integer.compare(this.id, o.getID()); }

        @Override
        public String toString() { return "DummyVariable(" + id + ")"; }
    }
}
