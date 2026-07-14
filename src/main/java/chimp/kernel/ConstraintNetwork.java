package chimp.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CHIMP-native constraint network — a resolver representation holding
 * variables, constraints, and a prototype&#x2192;real-variable substitution map.
 *
 * <p>Uses <b>reference equality</b> (default {@code Object.equals()})
 * to avoid the duplicate-edge bug in JUNG search trees (M15 decision 5).
 * Two distinct {@code ConstraintNetwork} instances are never equal, even
 * if they contain the same variables and constraints.
 */
public class ConstraintNetwork {

    private final List<Variable> variables = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();
    private final Map<VariablePrototype, Variable> substitutions = new HashMap<>();
    private Object annotation;

    public ConstraintNetwork() { }

    // -- variables --

    public void addVariable(Variable v) {
        variables.add(Objects.requireNonNull(v));
    }

    public Variable[] getVariables() {
        return variables.toArray(new Variable[0]);
    }

    // -- constraints --

    public void addConstraint(Constraint c) {
        constraints.add(Objects.requireNonNull(c));
    }

    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    public Constraint[] getConstraints() {
        return constraints.toArray(new Constraint[0]);
    }

    // -- substitutions --

    /**
     * Records that {@code prototype} should be replaced by {@code real}
     * during scope repair (see {@link HTNPlanner#addResolverSub}).
     */
    public void addSubstitution(VariablePrototype prototype, Variable real) {
        substitutions.put(prototype, real);
    }

    /**
     * Returns the real variable that replaces the given prototype,
     * or {@code null} if no substitution was recorded.
     */
    public Variable getSubstitution(VariablePrototype prototype) {
        return substitutions.get(prototype);
    }

    /** All recorded substitutions (for debugging). */
    public Map<VariablePrototype, Variable> getSubstitutions() {
        return Collections.unmodifiableMap(substitutions);
    }

    // -- annotation (used for SubVariableKind tags during compilation) --

    public void setAnnotation(Object annotation) {
        this.annotation = annotation;
    }

    public Object getAnnotation() {
        return annotation;
    }

    // -- standard --

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ConstraintNetwork:");
        if (!variables.isEmpty()) {
            sb.append(" Vars=").append(variables);
        }
        if (!constraints.isEmpty()) {
            sb.append(" Cons=").append(constraints);
        }
        sb.append("]");
        return sb.toString();
    }
}
