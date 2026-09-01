package chimp.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a planned action, extracted from a solved plan.
 * This is the CHIMP-owned replacement for using {@code Variable[]} (Meta-CSP)
 * directly as the plan-extraction API.
 */
public record PlannedAction(
        int id,
        String component,
        String name,
        List<String> arguments,
        long est,
        long lst,
        long eet,
        long let,
        List<String> integerValues) {

    /**
     * Compact constructor for validation.
     */
    public PlannedAction {
        Objects.requireNonNull(name, "name");
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
        integerValues = integerValues == null ? List.of() : List.copyOf(integerValues);
    }

    /**
     * Returns the name and arguments as a string, e.g. "!move_base(table1Area)".
     */
    public String nameWithArguments() {
        if (arguments.isEmpty()) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(id).append(")::");
        sb.append(nameWithArguments());
        if (!integerValues.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < integerValues.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(integerValues.get(i));
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
