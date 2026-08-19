package chimp.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import chimp.kernel.Constraint;
import chimp.kernel.ConstraintNetwork;
import chimp.kernel.ConstraintSolver;
import chimp.logging.ChimpLogging;

import edu.uci.ics.jung.graph.DelegateForest;

/**
 * CHIMP-owned backtracking search engine, ported from
 * {@code MetaConstraintSolver}. Owns the recursive backtracking loop,
 * flaw enumeration, resolver application/retraction, and the JUNG
 * search-tree visualization.
 *
 * <p>The engine operates against {@link FlawProvider}s and
 * {@link SearchHooks}. Flaw representation is {@link ChimpFlaw};
 * resolver representation is {@link ChimpResolver}.
 *
 * <p>The JUNG search-tree uses {@link SearchTreeNode} as vertices and
 * {@link SearchTreeEdge} as edges (replacing Meta-CSP {@code MetaVariable}
 * and {@code ConstraintNetwork} respectively).
 */
public final class ChimpSearchEngine {

    private static final Logger logger = ChimpLogging.getLogger(ChimpSearchEngine.class);

    /** Creates a fresh edge label for the JUNG search tree.
     *  Must create a new instance each time — JUNG rejects duplicate
     *  edges based on identity (SearchTreeEdge uses reference equality). */
    private static SearchTreeEdge newEdge(String resolverLabel) {
        return new SearchTreeEdge(resolverLabel);
    }

    private final List<FlawProvider> flawProviders;
    private final SearchHooks hooks;
    private final long animationTime;

    private DelegateForest<SearchTreeNode, SearchTreeEdge> searchTree;
    private SearchTreeNode currentVertex;

    private final Map<ChimpFlaw, FlawProvider> flawToProvider = new HashMap<>();
    private final Map<ChimpFlaw, ChimpResolver> resolvers = new HashMap<>();
    private final Map<ChimpResolver, ChimpFlaw> resolversInverse = new HashMap<>();
    private int counterMoves;

    public ChimpSearchEngine(List<FlawProvider> flawProviders, SearchHooks hooks, long animationTime) {
        this.flawProviders = List.copyOf(flawProviders);
        this.hooks = hooks;
        this.animationTime = animationTime;
    }

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void draw() {
        // TODO: replace with CHIMP-native search-tree visualization
        // (was org.metacsp.utility.UI.SearchTreeFrame.draw(...))
        System.out.println("Search tree: " + searchTree);
    }

    /** Returns the search tree from the last {@link #backtrack()} call,
     *  or {@code null} if no search has been run. For testing only. */
    public DelegateForest<SearchTreeNode, SearchTreeEdge> getSearchTree() {
        return searchTree;
    }

    public int getCounterMoves() { return counterMoves; }

    // --- Search loop ---

    private Flaw getConflict() {
        for (FlawProvider provider : flawProviders) {
            ChimpFlaw flaw = provider.currentFlaw();
            if (flaw != null) {
                return new Flaw(provider, flaw);
            }
        }
        return null;
    }

    private boolean backtrackHelper(Flaw flaw) {
        hooks.preBacktrack();
        if (searchTree.getRoot() == null) {
            searchTree.addVertex(currentVertex);
        }
        ChimpFlaw flawObj = flaw.flaw();
        logger.fine("Solving conflict: " + flaw);

        ChimpResolver[] values = flaw.provider().resolvers(flawObj);
        Comparator<ChimpResolver> ordering = flaw.provider().resolverOrdering();
        if (ordering != null && values != null) {
            Arrays.sort(values, ordering);
        }

        if (values == null || values.length == 0) {
            searchTree.addEdge(newEdge("FAILURE"), currentVertex, new TerminalNode(false));
            logger.fine("Failure (1)...");
        } else {
            for (ChimpResolver value : values) {
                if (animationTime != 0) {
                    try { Thread.sleep(animationTime); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                ConstraintNetwork net = value.delegate;
                String valString = "";
                if (net.getVariables().length != 0) {
                    valString += "Vars = " + Arrays.toString(net.getVariables());
                }
                if (net.getConstraints().length != 0) {
                    valString += " Cons = " + Arrays.toString(net.getConstraints());
                }
                SearchTreeEdge edge = newEdge(valString);
                logger.fine("Trying value: " + valString);

                if (addResolver(flawObj, value)) {
                    resolvers.put(flawObj, value);
                    resolversInverse.put(value, flawObj);
                    counterMoves++;
                    logger.fine("Success...");

                    flaw.provider().markResolved(flawObj, value);
                    Flaw newConflict = getConflict();

                    if (newConflict == null) {
                        searchTree.addEdge(edge, currentVertex, new TerminalNode(true));
                        return true;
                    }
                    searchTree.addEdge(edge, currentVertex, newConflict.graphNode);
                    currentVertex = newConflict.graphNode;
                    if (backtrackHelper(newConflict)) {
                        return true;
                    }
                    logger.fine("Retracting value: " + Arrays.toString(net.getConstraints()));
                    retractResolver(flawObj, value);
                    resolvers.remove(flawObj);
                    resolversInverse.remove(value);
                    counterMoves--;
                } else {
                    searchTree.addEdge(newEdge("FAILURE"), currentVertex, new TerminalNode(false));
                    logger.fine("Failure... (2)");
                }
            }
        }
        logger.fine("Backtracking...");
        currentVertex = searchTree.getParent(currentVertex);
        hooks.postBacktrack(flawObj);
        return false;
    }

    private boolean addResolver(ChimpFlaw flaw, ChimpResolver resolver) {
        if (!hooks.addResolverSub(flaw, resolver)) {
            return false;
        }
        Constraint[] resolverConstraints = resolver.delegate.getConstraints();
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

    private void retractResolver(ChimpFlaw flaw, ChimpResolver resolver) {
        logger.finest("Retracting resolver:");
        logger.finest("  Flaw: " + flaw);
        logger.finest("  Resolver: " + resolver);

        Constraint[] groundConstraints = resolver.delegate.getConstraints();
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
        private final ChimpFlaw flaw;
        private final SearchTreeNode graphNode;

        Flaw(FlawProvider provider, ChimpFlaw flaw) {
            this.provider = provider;
            this.flaw = flaw;
            this.graphNode = new SearchTreeNode(flaw.toString());
        }

        FlawProvider provider() { return provider; }
        ChimpFlaw flaw() { return flaw; }

        @Override
        public String toString() {
            return flaw.toString();
        }
    }

    private static final class TerminalNode extends SearchTreeNode {
        private final boolean success;
        TerminalNode(boolean success) {
            super(success ? "SUCCESS" : "FAILURE");
            this.success = success;
        }
        @Override
        public String toString() {
            return success ? "SUCCESS" : "FAILURE";
        }
    }
}
