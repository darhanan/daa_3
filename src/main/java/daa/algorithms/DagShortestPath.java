package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;

/**
 * Shortest paths in a DAG: take a topological order (Kahn) and relax the outgoing edges of every
 * vertex in that order, once. O(V + E). Negative weights are fine because no cycle exists, so
 * when a vertex is processed every edge into it has already been relaxed.
 *
 * <p>Counters cover the relaxation pass only: edgeScans = m, comparisons = m (one per
 * relaxation). The topological sort is counted under Kahn.
 */
public final class DagShortestPath {
    private DagShortestPath() {
    }

    /** Every vertex with in-degree 0 starts at distance 0 (a virtual super-source). */
    public static PathResult fromAllSources(Graph g) {
        return fromAllSources(g, new Metrics());
    }

    public static PathResult fromAllSources(Graph g, Metrics metrics) {
        int[] in = g.inDegrees();
        long[] dist = new long[g.n()];
        for (int v = 0; v < g.n(); v++) {
            dist[v] = in[v] == 0 ? 0 : PathResult.UNREACHABLE;
        }
        return relax(g, dist, metrics);
    }

    /** Single source s; vertices not reachable from s stay unreachable. */
    public static PathResult from(Graph g, int source) {
        return from(g, source, new Metrics());
    }

    public static PathResult from(Graph g, int source, Metrics metrics) {
        long[] dist = new long[g.n()];
        Arrays.fill(dist, PathResult.UNREACHABLE);
        dist[source] = 0;
        return relax(g, dist, metrics);
    }

    private static PathResult relax(Graph g, long[] dist, Metrics metrics) {
        int[] order = Kahn.run(g).orderOrThrow();
        int[] parent = new int[g.n()];
        Arrays.fill(parent, -1);
        for (int v : order) {
            long dv = dist[v];
            for (int i = 0, d = g.degree(v); i < d; i++) {
                int e = g.edgeAt(v, i);
                metrics.edgeScan();
                if (dv == PathResult.UNREACHABLE) {
                    continue;
                }
                int u = g.to(e);
                long candidate = dv + g.weight(e);
                metrics.comparison();
                if (candidate < dist[u]) {
                    dist[u] = candidate;
                    parent[u] = v;
                }
            }
        }
        return new PathResult(dist, parent);
    }
}
