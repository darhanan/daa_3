package daa.algorithms;

import daa.metrics.Metrics;

/**
 * Disjoint set union with union by rank and path compression. Together the two heuristics give
 * O(α(n)) amortized time per operation.
 */
public final class DSU {
    private final Metrics metrics;
    private int[] parent;
    private int[] rank;
    private int components;
    private long findCalls;
    private long unionCalls;

    public DSU(int n) {
        this(n, new Metrics());
    }

    public DSU(int n, Metrics metrics) {
        this.metrics = metrics;
        makeSet(n);
    }

    /** Resets the structure to n singleton sets {0}, {1}, ..., {n-1}. */
    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        components = n;
    }

    /** Representative of x's set. Every vertex on the walked path is re-hung onto the root. */
    public int find(int x) {
        findCalls++;
        metrics.dsuOp();
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** Merges the sets of x and y. Returns false (and counts nothing extra) if they already match. */
    public boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) {
            return false;
        }
        unionCalls++;
        metrics.dsuOp();
        if (rank[rx] < rank[ry]) {
            int t = rx;
            rx = ry;
            ry = t;
        }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) {
            rank[rx]++;
        }
        components--;
        return true;
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    /** Number of unions that actually merged two sets. */
    public long unionCalls() {
        return unionCalls;
    }
}
