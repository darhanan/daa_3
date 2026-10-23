package daa.graph;

import java.util.Arrays;

/**
 * Weighted graph stored as an adjacency list. Vertices are 0..n-1, weights are int.
 *
 * <p>Every edge gets an id 0..m-1 and lives once in the edge list ({@link #from}, {@link #to},
 * {@link #weight}). Each vertex keeps the ids of its incident edges. A directed edge u -> v is
 * listed only at u; an undirected edge u - v is listed at both ends (a self-loop only once).
 */
public final class Graph {
    private final int n;
    private final boolean directed;

    private int m;
    private int[] from = new int[4];
    private int[] to = new int[4];
    private int[] weight = new int[4];

    private final int[][] adj;
    private final int[] degree;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative: " + n);
        }
        this.n = n;
        this.directed = directed;
        this.adj = new int[n][];
        this.degree = new int[n];
    }

    /** Adds edge u -> v (or u - v when undirected) and returns its id. */
    public int addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);
        if (m == from.length) {
            int cap = m * 2;
            from = Arrays.copyOf(from, cap);
            to = Arrays.copyOf(to, cap);
            weight = Arrays.copyOf(weight, cap);
        }
        int id = m++;
        from[id] = u;
        to[id] = v;
        weight[id] = w;
        attach(u, id);
        if (!directed && u != v) {
            attach(v, id);
        }
        return id;
    }

    private void attach(int v, int id) {
        int[] list = adj[v];
        if (list == null) {
            list = adj[v] = new int[2];
        } else if (degree[v] == list.length) {
            list = adj[v] = Arrays.copyOf(list, list.length * 2);
        }
        list[degree[v]++] = id;
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IllegalArgumentException("vertex " + v + " is outside 0.." + (n - 1));
        }
    }

    public int n() {
        return n;
    }

    public int m() {
        return m;
    }

    public boolean isDirected() {
        return directed;
    }

    public int from(int e) {
        return from[e];
    }

    public int to(int e) {
        return to[e];
    }

    public int weight(int e) {
        return weight[e];
    }

    /** Number of edges listed at v (out-degree for a directed graph). */
    public int degree(int v) {
        return degree[v];
    }

    /** Id of the i-th edge listed at v, 0 <= i < degree(v). */
    public int edgeAt(int v, int i) {
        return adj[v][i];
    }

    /** The end of edge e that is not v (for a directed edge out of v this is its head). */
    public int other(int e, int v) {
        return from[e] == v ? to[e] : from[e];
    }

    /** In-degree of every vertex, computed from the edge list. */
    public int[] inDegrees() {
        int[] in = new int[n];
        for (int e = 0; e < m; e++) {
            in[to[e]]++;
            if (!directed && from[e] != to[e]) {
                in[from[e]]++;
            }
        }
        return in;
    }

    /** Same vertices, every directed edge flipped. Edge ids are preserved. */
    public Graph reverse() {
        if (!directed) {
            throw new IllegalStateException("reverse() is defined for directed graphs only");
        }
        Graph r = new Graph(n, true);
        for (int e = 0; e < m; e++) {
            r.addEdge(to[e], from[e], weight[e]);
        }
        return r;
    }

    @Override
    public String toString() {
        return (directed ? "directed" : "undirected") + " graph, n=" + n + ", m=" + m;
    }
}
