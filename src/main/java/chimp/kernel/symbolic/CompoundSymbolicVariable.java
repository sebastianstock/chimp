package chimp.kernel.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chimp.kernel.ConstraintSolver;
import chimp.kernel.Variable;

/**
 * CHIMP-native compound symbolic variable — a predicate (name + arguments)
 * composed of multiple {@link SymbolicVariable}s.
 *
 * <p>Port of {@code unify.CompoundSymbolicVariable}. The first internal
 * variable holds the predicate name; subsequent variables hold arguments.
 * Ground arguments can be read via {@link #getGroundArgs()}.
 */
public class CompoundSymbolicVariable extends Variable {

    private static final Pattern NAME_PATTERN = Pattern.compile("^(.+)\\((.*)\\)$");
    public static final String NONESYMBOL = "n";

    private static final String[] NO_STRS = {};

    private final SymbolicVariable[] internalVars;
    private final int internalVarCount;

    /**
     * Creates a compound variable with the given internal variables.
     * The first internal variable is the predicate name; the rest are arguments.
     */
    public CompoundSymbolicVariable(ConstraintSolver solver, int id,
                                     SymbolicVariable[] internalVars) {
        super(solver, id);
        this.internalVars = Objects.requireNonNull(internalVars);
        this.internalVarCount = internalVars.length;
        // Set component to the predicate name if possible
        if (internalVars.length > 0) {
            setComponent(internalVars[0].toString());
        }
    }

    public SymbolicVariable[] getInternalVariables() { return internalVars; }

    // -- name setting --

    /** Sets the predicate name and ground arguments. */
    public void setName(String type, String... arguments) {
        if (arguments == null) arguments = NO_STRS;
        if ((arguments.length + 1) > internalVarCount) {
            throw new IllegalArgumentException(
                    "Too many arguments: " + type);
        }
        internalVars[0].setConstant(type);
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].charAt(0) != '?') {
                internalVars[i + 1].setConstant(arguments[i]);
            }
        }
        for (int i = arguments.length + 1; i < internalVarCount; i++) {
            internalVars[i].setConstant(NONESYMBOL);
        }
    }

    /** Parses and sets a full name like {@code On(mug1 table1)}. */
    public void setFullName(String name) {
        Objects.requireNonNull(name, "name");
        Matcher m = NAME_PATTERN.matcher(name);
        if (m.find()) {
            String head = m.group(1);
            String argStr = m.group(2);
            String[] args = argStr.isEmpty() ? NO_STRS : argStr.split("\\s+");
            setName(head, args);
        } else {
            throw new IllegalArgumentException(
                    "Name does not match predicate pattern: " + name);
        }
    }

    // -- name retrieval --

    /** Full name: {@code HEAD(ARG1 ARG2 ...)}. */
    public String getName() {
        StringBuilder sb = new StringBuilder(internalVars[0].toString());
        sb.append("(");
        if (internalVars.length > 1 && !internalVars[1].toString().isEmpty()) {
            sb.append(internalVars[1].toString());
        }
        for (int i = 2; i < internalVars.length; i++) {
            String s = internalVars[i].toString();
            if (!s.isEmpty() && !s.equals(NONESYMBOL)) {
                sb.append(" ").append(s);
            } else {
                break;
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /** Short name using {@link SymbolicVariable#toStringShort()}. */
    public String getShortName() {
        StringBuilder sb = new StringBuilder(internalVars[0].toStringShort());
        sb.append("(");
        if (internalVars.length > 1 && !internalVars[1].toStringShort().isEmpty()) {
            sb.append(internalVars[1].toStringShort());
        }
        for (int i = 2; i < internalVars.length; i++) {
            String s = internalVars[i].toStringShort();
            if (!s.isEmpty() && !s.equals(NONESYMBOL)) {
                sb.append(" ").append(s);
            } else {
                break;
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /** Ground arguments (throws if any are not ground). */
    public String[] getGroundArgs() {
        if (internalVars.length < 2) return NO_STRS;
        List<String> ret = new ArrayList<>();
        for (int i = 1; i < internalVars.length; i++) {
            if (internalVars[i].isGround()) {
                String s = internalVars[i].toString();
                if (!s.equals(NONESYMBOL)) {
                    ret.add(s);
                } else {
                    break;
                }
            } else {
                throw new IllegalStateException(
                        (i - 1) + "th argument is not ground");
            }
        }
        return ret.toArray(NO_STRS);
    }

    /** All arguments (can be unground). */
    public String[] getArgs() {
        if (internalVars.length < 2) return NO_STRS;
        List<String> ret = new ArrayList<>();
        for (int i = 1; i < internalVars.length; i++) {
            String s = internalVars[i].toString();
            if (!s.equals(NONESYMBOL)) {
                ret.add(s);
            } else {
                break;
            }
        }
        return ret.toArray(NO_STRS);
    }

    // -- predicate queries --

    public String[] getPossiblePredicateNames() {
        return getSymbolsAt(0);
    }

    public String getPredicateName() {
        String[] vals = getSymbolsAt(0);
        if (vals.length == 1) return vals[0];
        throw new IllegalStateException("Predicate name is not ground");
    }

    /** Possible symbols at the given internal variable position. */
    public String[] getSymbolsAt(int position) {
        if (position >= internalVarCount) {
            throw new IllegalArgumentException("position out of range: " + position);
        }
        return internalVars[position].possibleSymbols();
    }

    /** Ground symbol at position, or empty string if not ground. */
    public String getGroundSymbolAt(int position) {
        String[] vals = getSymbolsAt(position);
        return vals.length == 1 ? vals[0] : "";
    }

    // -- matching queries --

    public boolean possibleMatch(String fluentType, String[] arguments) {
        if (fluentType == null) return false;
        for (String s : getSymbolsAt(0)) {
            if (fluentType.equals(s)) return true;
        }
        return false;
    }

    public boolean possibleArgumentsMatch(CompoundSymbolicVariable other, int[] connections) {
        if (connections.length > 0) {
            for (int i = 0; i < connections.length; i += 2) {
                if (!possibleArgumentMatch(other, connections[i], connections[i + 1])) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean possibleArgumentMatch(CompoundSymbolicVariable other, int from, int to) {
        for (String s : getSymbolsAt(from + 1)) {
            for (String os : other.getSymbolsAt(to + 1)) {
                if (s.equals(os)) return true;
            }
        }
        return false;
    }

    public boolean possibleArgumentDifferent(CompoundSymbolicVariable other,
                                              int fromId, int toId) {
        String[] fromSyms = getSymbolsAt(fromId + 1);
        String[] toSyms = other.getSymbolsAt(toId + 1);
        if (fromSyms.length != 1 || toSyms.length != 1) return true;
        return !fromSyms[0].equals(toSyms[0]);
    }

    // -- standard --

    @Override
    public String toString() {
        return getName();
    }
}
