package chimp.kernel;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Placeholder variable used in resolver networks before fluent materialization.
 *
 * <p>During plan generation, resolvers are compiled into
 * {@link ConstraintNetwork}s before the real {@link Fluent}s exist.
 * {@code VariablePrototype} acts as a stand-in that is later substituted
 * with a real variable via {@link ConstraintNetwork#addSubstitution}.
 *
 * <p>Two kinds of prototypes exist:
 * <ul>
 *   <li><b>Fluent prototype</b> — carries component name, predicate symbol,
 *       and arguments.  Becomes a real {@code Fluent} during materialization.</li>
 *   <li><b>Integer prototype</b> — nested: refers to a parent fluent prototype
 *       and an index into that fluent's integer sub-variables.</li>
 * </ul>
 */
public class VariablePrototype extends Variable {

    /** What kind of variable this prototype will become. */
    public enum Kind { FLUENT, INTEGER }

    /**
     * Global prototype counter (0,1,2,...). Mirrors Meta-CSP's
     * {@code VariablePrototype.id} counter: ids increase with creation
     * order, so id-sequenced iteration reproduces legacy resolution
     * (VP materialization) ordering.
     */
    private static final AtomicInteger NEXT_ID = new AtomicInteger(-1);

    private final Kind kind;

    // Fluent-prototype fields
    private final String component;
    private final String symbol;
    private final String[] arguments;

    // Integer-prototype fields (nested)
    private final VariablePrototype parentPrototype;
    private final int subIndex;

    private VariablePrototype(ConstraintSolver solver, int id,
                               Kind kind, String component, String symbol,
                               String[] arguments,
                               VariablePrototype parentPrototype, int subIndex) {
        super(solver, id);
        this.kind = kind;
        this.component = component;
        this.symbol = symbol;
        this.arguments = arguments != null ? arguments.clone() : null;
        this.parentPrototype = parentPrototype;
        this.subIndex = subIndex;
    }

    /** Creates a fluent placeholder with an explicit id. */
    public static VariablePrototype forFluent(ConstraintSolver solver, int id,
                                               String component, String symbol,
                                               String[] arguments) {
        return new VariablePrototype(solver, id, Kind.FLUENT,
                component, symbol, arguments, null, -1);
    }

    /** Creates a fluent placeholder with an auto-generated id. */
    public static VariablePrototype forFluent(ConstraintSolver solver,
                                               String component, String symbol,
                                               String[] arguments) {
        return forFluent(solver, NEXT_ID.incrementAndGet(), component, symbol, arguments);
    }

    /** Creates an integer sub-variable placeholder with an explicit id. */
    public static VariablePrototype forInteger(ConstraintSolver solver, int id,
                                                VariablePrototype parentPrototype,
                                                int subIndex) {
        return new VariablePrototype(solver, id, Kind.INTEGER,
                null, null, null, parentPrototype, subIndex);
    }

    /** Creates an integer sub-variable placeholder with an auto-generated id. */
    public static VariablePrototype forInteger(ConstraintSolver solver,
                                                VariablePrototype parentPrototype,
                                                int subIndex) {
        return forInteger(solver, NEXT_ID.incrementAndGet(), parentPrototype, subIndex);
    }

    // -- typed accessors --

    public Kind prototypeKind() { return kind; }

    public String component() { return component; }

    public String symbol() { return symbol; }

    public String[] arguments() {
        return arguments != null ? arguments.clone() : null;
    }

    public VariablePrototype parentPrototype() { return parentPrototype; }

    public int subIndex() { return subIndex; }

    // -- legacy Object[] accessor (for callers not yet migrated) --

    /**
     * Returns parameters in the legacy Meta-CSP {@code Object[]} layout.
     *
     * <p>For fluent prototypes: {@code [component, symbol, arguments]}.
     * For integer prototypes: {@code [parentPrototype, subIndex]}.
     *
     * @deprecated use typed accessors instead; kept during M15 migration
     */
    @Deprecated
    public Object[] getParameters() {
        if (kind == Kind.FLUENT) {
            return new Object[] { component, symbol, arguments };
        } else {
            return new Object[] { parentPrototype, Integer.valueOf(subIndex) };
        }
    }

    // -- standard --

    @Override
    public String toString() {
        if (kind == Kind.FLUENT) {
            return "VP[" + component + ":" + symbol
                    + (arguments != null ? Arrays.toString(arguments) : "") + "]";
        } else {
            return "VP[integer:" + parentPrototype + "[" + subIndex + "]]";
        }
    }
}
