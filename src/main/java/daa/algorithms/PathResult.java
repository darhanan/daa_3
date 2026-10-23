package daa.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Distances and shortest-path tree. dist[v] == {@link #UNREACHABLE} if v cannot be reached;
 * parent[v] == -1 for a source or an unreachable vertex.
 */
public record PathResult(long[] dist, int[] parent) {
    public static final long UNREACHABLE = Long.MAX_VALUE;

    public boolean reachable(int v) {
        return dist[v] != UNREACHABLE;
    }

    /** Vertices of the path from its source to v, or an empty list if v is unreachable. */
    public List<Integer> pathTo(int v) {
        if (!reachable(v)) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        for (int x = v; x != -1; x = parent[x]) {
            path.add(x);
        }
        Collections.reverse(path);
        return path;
    }

    /** Sum of dist[v] over all reachable vertices. */
    public long sumOfDistances() {
        long sum = 0;
        for (long d : dist) {
            if (d != UNREACHABLE) {
                sum += d;
            }
        }
        return sum;
    }
}
