package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Lazy Prim: start at vertex 0, keep the edges leaving the tree in a priority queue, always take
 * the lightest one and skip it if its far end is already in the tree. O(E log E).
 *
 * <p>If the queue empties before all vertices are in, the graph is disconnected: the next vertex
 * not yet in the forest starts a new tree. Counters: edgeScans = adjacency entries looked at
 * (2 per undirected edge); comparisons = weight comparisons made by the priority queue.
 */
public final class Prim {
    private Prim() {
    }

    public static MstResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static MstResult run(Graph g, Metrics metrics) {
        Kruskal.requireUndirected(g);
        int n = g.n();
        boolean[] inTree = new boolean[n];
        PriorityQueue<Integer> pq = new PriorityQueue<>((x, y) -> {
            metrics.comparison();
            return Integer.compare(g.weight(x), g.weight(y));
        });
        int[] chosen = new int[Math.max(0, n - 1)];
        int count = 0;
        long total = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            visit(g, start, inTree, pq, metrics);
            while (!pq.isEmpty()) {
                int e = pq.poll();
                int u = g.from(e);
                int v = g.to(e);
                if (inTree[u] && inTree[v]) {
                    continue;
                }
                chosen[count++] = e;
                total += g.weight(e);
                visit(g, inTree[u] ? v : u, inTree, pq, metrics);
            }
        }
        return new MstResult(Arrays.copyOf(chosen, count), total, components);
    }

    private static void visit(Graph g, int v, boolean[] inTree, PriorityQueue<Integer> pq, Metrics metrics) {
        inTree[v] = true;
        for (int i = 0, d = g.degree(v); i < d; i++) {
            int e = g.edgeAt(v, i);
            metrics.edgeScan();
            if (!inTree[g.other(e, v)]) {
                pq.add(e);
            }
        }
    }
}
