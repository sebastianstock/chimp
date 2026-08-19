package chimp.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import chimp.kernel.ConstraintNetwork;
import chimp.kernel.Variable;
import chimp.model.FluentId;

public class ChimpSearchEngineTest {

    @Test
    public void findsSolutionWithFirstResolver() {
        RecordingHooks hooks = new RecordingHooks();
        ChimpFlaw flaw = new ChimpFlaw(List.of(new FluentId(1)));
        ChimpResolver resolver = new ChimpResolver(new ConstraintNetwork());
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of(resolver)));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(1, hooks.addResolverSubCalls);
        assertEquals(0, hooks.retractResolverSubCalls);
        assertEquals(1, engine.getCounterMoves());
    }

    @Test
    public void backtracksWhenFirstResolverLeadsToDeadEnd() {
        RecordingHooks hooks = new RecordingHooks();
        ChimpFlaw flaw1 = new ChimpFlaw(List.of(new FluentId(100)));
        ChimpFlaw flaw2 = new ChimpFlaw(List.of(new FluentId(101)));
        ChimpResolver resolver1 = new ChimpResolver(distinctNetwork(1));
        ChimpResolver resolver2 = new ChimpResolver(distinctNetwork(2));

        StubProvider provider = new StubProvider(
                List.of(flaw1, flaw2),
                List.of(List.of(resolver1, resolver2), List.of()));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(2, hooks.addResolverSubCalls);
        assertEquals(1, hooks.retractResolverSubCalls);
    }

    @Test
    public void failsWhenNoResolvers() {
        RecordingHooks hooks = new RecordingHooks();
        ChimpFlaw flaw = new ChimpFlaw(List.of(new FluentId(1)));
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of()));
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertFalse(engine.backtrack());
        assertEquals(0, hooks.addResolverSubCalls);
    }

    @Test
    public void succeedsWhenNoFlaws() {
        RecordingHooks hooks = new RecordingHooks();
        StubProvider provider = new StubProvider(List.of(), List.of());
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(0, hooks.addResolverSubCalls);
    }

    @Test
    public void rejectedResolverFallsThroughToNext() {
        ChimpFlaw flaw = new ChimpFlaw(List.of(new FluentId(100)));
        ChimpResolver rejected = new ChimpResolver(distinctNetwork(1));
        ChimpResolver accepted = new ChimpResolver(distinctNetwork(2));
        StubProvider provider = new StubProvider(List.of(flaw), List.of(List.of(rejected, accepted)));
        final int[] callCount = {0};
        RecordingHooks hooks = new RecordingHooks() {
            @Override
            public boolean addResolverSub(ChimpFlaw f, ChimpResolver r) {
                addResolverSubCalls++;
                callCount[0]++;
                return callCount[0] != 1;
            }
        };
        ChimpSearchEngine engine = new ChimpSearchEngine(List.of(provider), hooks, 0);

        assertTrue(engine.backtrack());
        assertEquals(2, hooks.addResolverSubCalls);
    }

    // --- Stubs ---

    private static final class StubProvider implements FlawProvider {
        private final List<ChimpFlaw> flaws;
        private final List<List<ChimpResolver>> resolversPerFlaw;
        private int flawIndex = 0;

        StubProvider(List<ChimpFlaw> flaws, List<List<ChimpResolver>> resolversPerFlaw) {
            this.flaws = flaws;
            this.resolversPerFlaw = resolversPerFlaw;
        }

        @Override
        public ChimpFlaw currentFlaw() {
            if (flawIndex >= flaws.size()) return null;
            return flaws.get(flawIndex);
        }

        @Override
        public ChimpResolver[] resolvers(ChimpFlaw flaw) {
            int idx = flaws.indexOf(flaw);
            if (idx < 0 || idx >= resolversPerFlaw.size()) return new ChimpResolver[0];
            List<ChimpResolver> resList = resolversPerFlaw.get(idx);
            flawIndex++;
            return resList.toArray(new ChimpResolver[0]);
        }

        @Override
        public void markResolved(ChimpFlaw flaw, ChimpResolver resolver) { }

        @Override
        public Comparator<ChimpResolver> resolverOrdering() { return null; }
    }

    private static class RecordingHooks implements SearchHooks {
        int addResolverSubCalls = 0;
        int retractResolverSubCalls = 0;
        int preBacktrackCalls = 0;
        int postBacktrackCalls = 0;

        @Override
        public void preBacktrack() { preBacktrackCalls++; }

        @Override
        public void postBacktrack(ChimpFlaw flaw) { postBacktrackCalls++; }

        @Override
        public boolean addResolverSub(ChimpFlaw flaw, ChimpResolver resolver) {
            addResolverSubCalls++;
            return true;
        }

        @Override
        public void retractResolverSub(ChimpFlaw flaw, ChimpResolver resolver) {
            retractResolverSubCalls++;
        }
    }

    private static ConstraintNetwork distinctNetwork(int id) {
        ConstraintNetwork cn = new ConstraintNetwork();
        cn.addVariable(new DummyVariable(id));
        return cn;
    }

    private static final class DummyVariable extends Variable {
        private final int id;
        DummyVariable(int id) { super(null, id); this.id = id; }
        @Override public String toString() { return "DummyVariable(" + id + ")"; }
    }
}
