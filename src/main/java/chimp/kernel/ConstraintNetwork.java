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
    private Object specializedAnnotation;

    public ConstraintNetwork() { }

    // -- variables --

    public void addVariable(Variable v) {
        variables.add(Objects.requireNonNull(v));
    }

    /**
     * Returns all variables.
     *
     * <p>Replicates Meta-CSP's {@code ConstraintNetwork.getVariables()}
     * ordering exactly: variables are collected into a fresh id-hashed
     * {@link HashSet} (keys hash by variable id, see {@link Variable#hashCode}),
     * so iteration follows id-keyed bucket order.  Consumers that depend on
     * ordering (e.g. resolver-sub variable materialization in
     * {@link HTNPlanner#addResolverSub}, scheduler flaw construction) thereby
     * reproduce the legacy search behavior.
     */
    public Variable[] getVariables() {
        java.util.HashSet<Variable> set = new java.util.HashSet<>();
        for (Variable v : variables) {
            set.add(v);
        }
        return set.toArray(new Variable[0]);
    }

    // -- constraints --

    public void addConstraint(Constraint c) {
        constraints.add(Objects.requireNonNull(c));
        // Ensure all scope variables are present in the network.  This mirrors
        // Meta-CSP behaviour where variables are derived from constraints.
        Variable[] scope = c.scope();
        if (scope != null) {
            for (Variable v : scope) {
                if (v != null && !variables.contains(v)) {
                    variables.add(v);
                }
            }
        }
    }

    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    public Constraint[] getConstraints() {
        return constraints.toArray(new Constraint[0]);
    }

    /** Returns constraints whose scope includes both {@code from} and {@code to}. */
    public Constraint[] getConstraints(Variable from, Variable to) {
        List<Constraint> result = new ArrayList<>();
        for (Constraint c : constraints) {
            boolean hasFrom = false;
            boolean hasTo = false;
            for (Variable v : c.scope()) {
                if (v.equals(from)) hasFrom = true;
                if (v.equals(to)) hasTo = true;
            }
            if (hasFrom && hasTo) result.add(c);
        }
        return result.toArray(new Constraint[0]);
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

    /**
     * Sets the general-purpose annotation for this network.
     */
    public void setAnnotation(Object annotation) {
        this.annotation = annotation;
    }

    /**
     * Returns the general-purpose annotation.
     */
    public Object getAnnotation() {
        return annotation;
    }

    // -- specialized annotation (used by search engine for flaws/resolvers) --

    /**
     * Sets a specialized annotation object (e.g. a {@link PlanningFlaw}
     * or {@link chimp.search.ResolverCandidate}).
     */
    public void setSpecilizedAnnotation(Object annotation) {
        this.specializedAnnotation = annotation;
    }

    /**
     * Returns the specialized annotation object.
     */
    public Object getSpecilizedAnnotation() {
        return specializedAnnotation;
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
