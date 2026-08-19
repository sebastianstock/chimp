package chimp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CHIMP-native replacement for {@code org.metacsp.utility.PowerSet}.
 *
 * <p>Returns all subsets of the given set (excluding the empty set).
 */
public final class PowerSet {

    private PowerSet() { }

    public static <T> Set<Set<T>> powerSet(Set<T> set) {
        Set<Set<T>> result = new HashSet<>();
        List<T> elements = new ArrayList<>(set);
        int count = 1 << elements.size();
        for (int i = 1; i < count; i++) {
            Set<T> subset = new HashSet<>();
            for (int j = 0; j < elements.size(); j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(elements.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }
}
