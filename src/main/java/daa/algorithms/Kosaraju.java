package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

/**
 * Kosaraju: a DFS over the graph records the finish order, then a DFS over the reversed graph in
 * decreasing finish order; every tree of the second pass is one SCC. O(V + E).
 *
 * <p>Both passes use an explicit stack with a per-vertex edge cursor, so the depth is limited by
 * the heap, not by the JVM thread stack. Each pass looks at every edge exactly once, so edgeScans
 * is exactly 2m; maxDepth is the largest stack size reached. Components are numbered in
 * topological order of the condensation (a source component gets id 0).
 */
public final class Kosaraju {
    private Kosaraju() {
    }

    public static SccResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static SccResult run(Graph g, Metrics metrics) {
        int n = g.n();
        int[] stack = new int[n];
        int[] cursor = new int[n];

        // Pass 1: finish order on g.
        boolean[] seen = new boolean[n];
        int[] finish = new int[n];
        int finished = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }
            int top = 0;
            stack[top++] = s;
            seen[s] = true;
            metrics.depth(top);
            while (top > 0) {
                int v = stack[top - 1];
                if (cursor[v] < g.degree(v)) {
                    int e = g.edgeAt(v, cursor[v]++);
                    metrics.edgeScan();
                    int u = g.other(e, v);
                    if (!seen[u]) {
                        seen[u] = true;
                        stack[top++] = u;
                        metrics.depth(top);
                    }
                } else {
                    top--;
                    finish[finished++] = v;
                }
            }
        }

        // Pass 2: reversed graph, vertices in decreasing finish time.
        Graph r = g.isDirected() ? g.reverse() : g;
        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1);
        java.util.Arrays.fill(cursor, 0);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (comp[s] != -1) {
                continue;
            }
            int top = 0;
            stack[top++] = s;
            comp[s] = count;
            metrics.depth(top);
            while (top > 0) {
                int v = stack[top - 1];
                if (cursor[v] < r.degree(v)) {
                    int e = r.edgeAt(v, cursor[v]++);
                    metrics.edgeScan();
                    int u = r.other(e, v);
                    if (comp[u] == -1) {
                        comp[u] = count;
                        stack[top++] = u;
                        metrics.depth(top);
                    }
                } else {
                    top--;
                }
            }
            count++;
        }
        return new SccResult(comp, count);
    }
}
