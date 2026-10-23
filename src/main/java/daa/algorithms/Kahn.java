package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

/**
 * Kahn: start with every vertex of in-degree 0, repeatedly remove one and lower the in-degree of
 * its successors. If fewer than n vertices are removed, the rest sit on or behind a cycle and no
 * topological order exists. O(V + E); edgeScans is exactly m.
 */
public final class Kahn {
    private Kahn() {
    }

    public static TopoResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static TopoResult run(Graph g, Metrics metrics) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("topological order needs a directed graph");
        }
        int n = g.n();
        int[] in = g.inDegrees();
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        for (int v = 0; v < n; v++) {
            if (in[v] == 0) {
                queue[tail++] = v;
            }
        }
        while (head < tail) {
            int v = queue[head++];
            for (int i = 0, d = g.degree(v); i < d; i++) {
                int e = g.edgeAt(v, i);
                metrics.edgeScan();
                int u = g.to(e);
                if (--in[u] == 0) {
                    queue[tail++] = u;
                }
            }
        }
        if (tail < n) {
            return new TopoResult(null, true, tail);
        }
        return new TopoResult(queue, false, n);
    }

    /** Checks pos[u] < pos[v] for every edge u -> v and that order is a permutation of 0..n-1. */
    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order == null || order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (int e = 0; e < g.m(); e++) {
            if (pos[g.from(e)] >= pos[g.to(e)]) {
                return false;
            }
        }
        return true;
    }
}
