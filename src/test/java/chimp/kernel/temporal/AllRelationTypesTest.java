package chimp.kernel.temporal;

import static org.junit.Assert.*;

import chimp.model.TemporalBounds;
import chimp.model.TemporalRelation;
import org.junit.Test;

/**
 * Smoke-test: compiles and adds every TemporalRelation type to ensure
 * all ~35 types compile without error and produce a consistent network.
 */
public class AllRelationTypesTest {

    private static final long HORIZON = 1000;

    @Test public void before()        { smoke(TemporalRelation.Before, 1, 0); }
    @Test public void after()         { smoke(TemporalRelation.After, 1, 0); }
    @Test public void meets()         { smoke(TemporalRelation.Meets, 1, 0); }
    @Test public void metBy()         { smoke(TemporalRelation.MetBy, 1, 0); }
    @Test public void overlappedBy()  { smoke(TemporalRelation.OverlappedBy, 3, 0); }
    @Test public void overlaps()      { smoke(TemporalRelation.Overlaps, 3, 0); }
    @Test public void during()        { smoke(TemporalRelation.During, 2, 0); }
    @Test public void contains()      { smoke(TemporalRelation.Contains, 2, 0); }
    @Test public void starts()        { smoke(TemporalRelation.Starts, 2, 0); }
    @Test public void startedBy()     { smoke(TemporalRelation.StartedBy, 2, 0); }
    @Test public void finishes()      { smoke(TemporalRelation.Finishes, 2, 0); }
    @Test public void finishedBy()    { smoke(TemporalRelation.FinishedBy, 2, 0); }
    @Test public void equals_()       { smoke(TemporalRelation.Equals, 2, 0); }
    @Test public void duration()      { smokeSelf(TemporalRelation.Duration, 1, 0); }
    @Test public void release()       { smoke(TemporalRelation.Release, 1, 0); }
    @Test public void deadline()      { smoke(TemporalRelation.Deadline, 1, 0); }

    @Test public void beforeOrMeets()                              { smoke(TemporalRelation.BeforeOrMeets, 1, 0); }
    @Test public void metByOrAfter()                               { smoke(TemporalRelation.MetByOrAfter, 1, 0); }
    @Test public void metByOrOverlappedBy()                        { smoke(TemporalRelation.MetByOrOverlappedBy, 3, 0); }
    @Test public void metByOrOverlappedByOrAfter()                 { smoke(TemporalRelation.MetByOrOverlappedByOrAfter, 2, 0); }
    @Test public void metByOrOverlappedByOrIsFinishedByOrDuring()  { smoke(TemporalRelation.MetByOrOverlappedByOrIsFinishedByOrDuring, 2, 0); }
    @Test public void meetsOrOverlapsOrBefore()                    { smoke(TemporalRelation.MeetsOrOverlapsOrBefore, 2, 0); }
    @Test public void duringOrEquals()                             { smoke(TemporalRelation.DuringOrEquals, 2, 0); }
    @Test public void duringOrEqualsOrStartsOrFinishes()           { smoke(TemporalRelation.DuringOrEqualsOrStartsOrFinishes, 2, 0); }
    @Test public void meetsOrOverlapsOrFinishedByOrContains()      { smoke(TemporalRelation.MeetsOrOverlapsOrFinishedByOrContains, 2, 0); }
    @Test public void containsOrStartedByOrOverlappedByOrMetBy()   { smoke(TemporalRelation.ContainsOrStartedByOrOverlappedByOrMetBy, 2, 0); }
    @Test public void endEnd()                                     { smoke(TemporalRelation.EndEnd, 1, 0); }
    @Test public void endsOrEndedBy()                              { smoke(TemporalRelation.EndsOrEndedBy, 1, 0); }
    @Test public void startStart()                                 { smoke(TemporalRelation.StartStart, 1, 0); }
    @Test public void startsOrStartedBy()                          { smoke(TemporalRelation.StartsOrStartedBy, 1, 0); }
    @Test public void notBeforeAndNotAfter()                       { smoke(TemporalRelation.NotBeforeAndNotAfter, 2, 0); }
    @Test public void at_()                                        { smokeSelf(TemporalRelation.At, 2, 1); }

    // No-edge types
    @Test public void forever()         { smoke(TemporalRelation.Forever, 0, 0); }
    @Test public void endsDuring()      { smoke(TemporalRelation.EndsDuring, 0, 0); }
    @Test public void disjunction()     { smoke(TemporalRelation.DisjunctionRelation, 0, 0); }

    // --- helpers ---

    /**
     * Adds a binary constraint between two variables and asserts it compiles
     * to {@code expectedEdgeCount} edges and propagates without error.
     */
    private void smoke(TemporalRelation type, int expectedEdgeCount, int flags) {
        TemporalSolver solver = new TemporalSolver(0, HORIZON, 100);
        TemporalVariable[] vars = solver.createVariables(2);
        // Give the variables some non-zero duration so there's overlap
        TemporalConstraint dur = new TemporalConstraint(
                TemporalRelation.Duration, new TemporalBounds(0, HORIZON));
        dur.setFrom(vars[0]);
        dur.setTo(vars[0]);
        solver.addConstraint(dur);

        TemporalConstraint c = new TemporalConstraint(type);
        c.setFrom(vars[0]);
        c.setTo((flags & 1) != 0 ? vars[0] : vars[1]); // self-reference for At/Duration
        java.util.List<DistanceEdge> edges = c.compileToDistanceConstraints();
        assertEquals(type + " edge count mismatch", expectedEdgeCount, edges.size());

        boolean ok = solver.addConstraint(c);
        assertTrue(type + " propagation failed", ok);
    }

    /** Same as smoke but sets both from and to to the same variable. */
    private void smokeSelf(TemporalRelation type, int expectedEdgeCount, int flags) {
        smoke(type, expectedEdgeCount, flags | 1);
    }
}
