package chimp.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.framework.meta.NullConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.UI.SearchTreeFrame;

import edu.uci.ics.jung.graph.DelegateForest;

/**
 * CHIMP-owned backtracking search engine, ported from
 * {@code MetaConstraintSolver}. Owns the recursive backtracking loop,
 * flaw enumeration, resolver application/retraction, and the JUNG
 * search-tree visualization.
 *
 * <p>The engine operates against {@link FlawProvider}s (which replace
 * {@code MetaConstraint}s) and {@link SearchHooks} (which replace the
 * {@code MetaConstraintSolver} abstract hooks). The flaw and resolver
 * representations remain Meta-CSP {@link ConstraintNetwork}s for this
 * milestone; the storage layer is not yet replaced.
 *
 * <p>Branch-and-bound, serialization-based backup, and other unused
 * {@code MetaConstraintSolver} features are omitted.
 */
public final class ChimpSearchEngine {

    private static final Logger logger = MetaCSPLogging.getLogger(ChimpSearchEngine.class);

    private final List<FlawProvider> flawProviders;
    private final SearchHooks hooks;
    private final long animationTime;

    private DelegateForest<MetaVariable, ConstraintNetwork> searchTree;
    private MetaVariable currentVertex;

    /** Maps flaw networks to the provider that produced them. */
    private final Map<ConstraintNetwork, FlawProvider> flawToProvider = new HashMap<>();
    private final Map<ConstraintNetwork, ConstraintNetwork> resolvers = new HashMap<>();
    private final Map<ConstraintNetwork, ConstraintNetwork> resolversInverse = new HashMap<>();
    private int counterMoves;

    public ChimpSearchEngine(List<FlawProvider> flawProviders, SearchHooks hooks, long animationTime) {
        this.flawProviders = List.copyOf(flawProviders);
        this.hooks = hooks;
        this.animationTime = animationTime;
    }

    /**
     * Initiates backtracking search.
     *
     * @return {@code true} iff a consistent assignment of resolvers to
     *         all flaws was found
     */
    public boolean backtrack() {
        searchTree = new DelegateForest<>();
        logger.info("Starting search...");
        Flaw conflict = getConflict();
        if (conflict != null) {
            currentVertex = conflict.graphNode;
            if (backtrackHelper(conflict)) {
                logger.info("... solution found");
                return true;
            }
            return false;
        }
        logger.info("... no conflicts found");
        return true;
    }

    /**
     * Draws the search tree visualization. Preserved from
     * {@code MetaConstraintSolver.draw()}.
     */
    public void draw() {
        SearchTreeFrame.draw(searchTree);
    }

    /**
     * Returns the search tree built during the last {@link #backtrack()}
     * call, or {@code null} if no search has been run.
     */
    public DelegateForest<MetaVariable, ConstraintNetwork> getSearchTree() {
        return searchTree;
    }

    public int getCounterMoves() {
        return counterMoves;
    }

    // --- Search loop (ported from MetaConstraintSolver) ---

    private Flaw getConflict() {
        for (FlawProvider provider : flawProviders) {
            ConstraintNetwork cn = provider.currentFlaw();
            if (cn != null) {
                return new Flaw(provider, cn);
            }
        }
        return null;
    }

    private boolean backtrackHelper(Flaw flaw) {
        hooks.preBacktrack();
        if (searchTree.getRoot() == null) {
            searchTree.addVertex(currentVertex);
        }
        ConstraintNetwork flawNetwork = flaw.network();
        logger.fine("Solving conflict: " + flaw);

        ConstraintNetwork[] values = flaw.provider().resolvers(flawNetwork);
        if (values != null) {
            for (ConstraintNetwork value : values) {
                value.setAnnotation(flaw);
            }
        }
        Comparator<ConstraintNetwork> ordering = flaw.provider().resolverOrdering();
        if (ordering != null && values != null) {
            Arrays.sort(values, ordering);
        }

        if (values == null || values.length == 0) {
            searchTree.addEdge(new NullConstraintNetwork(null), currentVertex, new TerminalNode(false));
            logger.fine("Failure (1)...");
        } else {
            for (ConstraintNetwork value : values) {
                if (animationTime != 0) {
                    try { Thread.sleep(animationTime); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                String valString = "";
                if (value.getVariables().length != 0) {
                    valString += "Vars = " + Arrays.toString(value.getVariables());
                }
                if (value.getConstraints().length != 0) {
                    valString += " Cons = " + Arrays.toString(value.getConstraints());
                }
                logger.fine("Trying value: " + valString);

                if (addResolver(flawNetwork, value)) {
                    resolvers.put(flawNetwork, value);
                    resolversInverse.put(value, flawNetwork);
                    counterMoves++;
                    logger.fine("Success...");

                    flaw.provider().markResolved(flawNetwork, value);
                    Flaw newConflict = getConflict();

                    if (newConflict == null) {
                        searchTree.addEdge(value, currentVertex, new TerminalNode(true));
                        return true;
                    }
                    searchTree.addEdge(value, currentVertex, newConflict.graphNode);
                    currentVertex = newConflict.graphNode;
                    if (backtrackHelper(newConflict)) {
                        return true;
                    }
                    logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));
                    retractResolver(flawNetwork, value);
                    resolvers.remove(flawNetwork);
                    resolversInverse.remove(value);
                    counterMoves--;
                } else {
                    searchTree.addEdge(value, currentVertex, new TerminalNode(false));
                    logger.fine("Failure... (2)");
                }
            }
        }
        logger.fine("Backtracking...");
        currentVertex = searchTree.getParent(currentVertex);
        hooks.postBacktrack(flawNetwork);
        return false;
    }

    private boolean addResolver(ConstraintNetwork flaw, ConstraintNetwork resolver) {
        if (!hooks.addResolverSub(flaw, resolver)) {
            return false;
        }
        Constraint[] resolverConstraints = resolver.getConstraints();
        Map<ConstraintSolver, Vector<Constraint>> solvers2constraints = new HashMap<>();
        for (Constraint c : resolverConstraints) {
            solvers2constraints
                    .computeIfAbsent(c.getScope()[0].getConstraintSolver(), k -> new Vector<>())
                    .add(c);
        }
        Vector<Constraint[]> addedConstraints = new Vector<>();
        for (ConstraintSolver cs : solvers2constraints.keySet()) {
            Constraint[] toAdd = solvers2constraints.get(cs).toArray(new Constraint[0]);
            if (cs.addConstraints(toAdd)) {
                addedConstraints.add(toAdd);
            } else {
                for (Constraint[] toDel : addedConstraints) {
                    toDel[0].getScope()[0].getConstraintSolver().removeConstraints(toDel);
                }
                hooks.retractResolverSub(flaw, resolver);
                return false;
            }
        }
        return true;
    }

    private void retractResolver(ConstraintNetwork flaw, ConstraintNetwork resolver) {
        logger.finest("Retracting resolver:");
        logger.finest("  Flaw: " + flaw);
        logger.finest("  Resolver: " + resolver);

        Constraint[] groundConstraints = resolver.getConstraints();
        Map<ConstraintSolver, Vector<Constraint>> solvers2constraints = new HashMap<>();
        for (Constraint c : groundConstraints) {
            solvers2constraints
                    .computeIfAbsent(c.getScope()[0].getConstraintSolver(), k -> new Vector<>())
                    .add(c);
        }
        for (ConstraintSolver cs : solvers2constraints.keySet()) {
            Constraint[] toRemove = solvers2constraints.get(cs).toArray(new Constraint[0]);
            cs.removeConstraints(toRemove);
        }
        hooks.retractResolverSub(flaw, resolver);
        logger.finest("Done retracting resolver.");
    }

    // --- Internal types ---

    private final class Flaw {
        private final FlawProvider provider;
        private final ConstraintNetwork network;
        private final MetaVariable graphNode;

        Flaw(FlawProvider provider, ConstraintNetwork network) {
            this.provider = provider;
            this.network = network;
            this.graphNode = new MetaVariable(null, network);
        }

        FlawProvider provider() { return provider; }
        ConstraintNetwork network() { return network; }

        @Override
        public String toString() {
            return network.toString();
        }
    }

    /** Terminal search-tree node (SUCCESS/FAILURE leaf), matching MetaConstraintSolver.TerminalNode. */
    private static final class TerminalNode extends MetaVariable {
        private final boolean success;

        TerminalNode(boolean success) {
            super(null, null);
            this.success = success;
        }

        @Override
        public String toString() {
            return success ? "SUCCESS" : "FAILURE";
        }
    }
}
