package daa.algorithms;

import java.util.ArrayList;
import java.util.List;

/** Strongly connected components: componentOf[v] is a component id in 0..count-1. */
public record SccResult(int[] componentOf, int count) {
    public List<List<Integer>> components() {
        List<List<Integer>> out = new ArrayList<>(count);
        for (int c = 0; c < count; c++) {
            out.add(new ArrayList<>());
        }
        for (int v = 0; v < componentOf.length; v++) {
            out.get(componentOf[v]).add(v);
        }
        return out;
    }

    /** True if both results put exactly the same vertices together (ids may differ). */
    public boolean samePartition(SccResult other) {
        if (count != other.count || componentOf.length != other.componentOf.length) {
            return false;
        }
        int[] map = new int[count];
        java.util.Arrays.fill(map, -1);
        for (int v = 0; v < componentOf.length; v++) {
            int a = componentOf[v];
            int b = other.componentOf[v];
            if (map[a] == -1) {
                map[a] = b;
            } else if (map[a] != b) {
                return false;
            }
        }
        return true;
    }
}
