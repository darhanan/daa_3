package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;

/**
 * Tarjan (bonus task A): one DFS pass with index[], lowlink[] and a stack of active vertices.
 * A vertex whose lowlink equals its own index is the root of an SCC; everything above it on the
 * active stack belongs to that SCC. O(V + E) and only m edge scans, against 2m for Kosaraju,
 * because there is no second pass and no reversed graph.
 */
public final class Tarjan {
    private Tarjan() {
    }

    public static SccResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static SccResult run(Graph g, Metrics metrics) {
        int n = g.n();
        int[] index = new int[n];
        int[] low = new int[n];
        int[] comp = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);

        int[] active = new int[n];
        int activeTop = 0;
        int[] call = new int[n];
        int[] cursor = new int[n];
        int nextIndex = 0;
        int count = 0;

        for (int s = 0; s < n; s++) {
            if (index[s] != -1) {
                continue;
            }
            int top = 0;
            call[top++] = s;
            index[s] = low[s] = nextIndex++;
            active[activeTop++] = s;
            onStack[s] = true;
            metrics.depth(top);
            while (top > 0) {
                int v = call[top - 1];
                if (cursor[v] < g.degree(v)) {
                    int e = g.edgeAt(v, cursor[v]++);
                    metrics.edgeScan();
                    int w = g.other(e, v);
                    if (index[w] == -1) {
                        index[w] = low[w] = nextIndex++;
                        active[activeTop++] = w;
                        onStack[w] = true;
                        call[top++] = w;
                        metrics.depth(top);
                    } else if (onStack[w]) {
                        low[v] = Math.min(low[v], index[w]);
                    }
                } else {
                    top--;
                    if (top > 0) {
                        int p = call[top - 1];
                        low[p] = Math.min(low[p], low[v]);
                    }
                    if (low[v] == index[v]) {
                        int w;
                        do {
                            w = active[--activeTop];
                            onStack[w] = false;
                            comp[w] = count;
                        } while (w != v);
                        count++;
                    }
                }
            }
        }
        return new SccResult(comp, count);
    }
}
