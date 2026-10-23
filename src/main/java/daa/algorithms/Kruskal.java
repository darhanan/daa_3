package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;

/**
 * Kruskal: sort the edges by weight, then take an edge whenever its ends are in different DSU
 * sets. O(E log E) for the sort plus O(E α(V)) for the DSU work.
 *
 * <p>Counters: comparisons = weight comparisons made by the merge sort; edgeScans = edges taken
 * from the sorted list; dsuOps = find calls plus successful unions. The scan stops as soon as
 * n - 1 edges are chosen, so on dense graphs only a share of the sorted list is examined.
 */
public final class Kruskal {
    private Kruskal() {
    }

    public static MstResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static MstResult run(Graph g, Metrics metrics) {
        requireUndirected(g);
        int n = g.n();
        int[] order = sortByWeight(g, metrics);
        DSU dsu = new DSU(n, metrics);
        int[] chosen = new int[Math.max(0, n - 1)];
        int count = 0;
        long total = 0;
        for (int i = 0; i < order.length && count < n - 1; i++) {
            int e = order[i];
            metrics.edgeScan();
            if (dsu.union(g.from(e), g.to(e))) {
                chosen[count++] = e;
                total += g.weight(e);
            }
        }
        return new MstResult(Arrays.copyOf(chosen, count), total, dsu.components());
    }

    /** Bottom-up merge sort of the edge ids by weight; stable, counts every weight comparison. */
    static int[] sortByWeight(Graph g, Metrics metrics) {
        int m = g.m();
        int[] a = new int[m];
        for (int i = 0; i < m; i++) {
            a[i] = i;
        }
        int[] buf = new int[m];
        for (int width = 1; width < m; width *= 2) {
            for (int lo = 0; lo < m - width; lo += 2 * width) {
                int mid = lo + width;
                int hi = Math.min(lo + 2 * width, m);
                merge(g, a, buf, lo, mid, hi, metrics);
            }
        }
        return a;
    }

    private static void merge(Graph g, int[] a, int[] buf, int lo, int mid, int hi, Metrics metrics) {
        System.arraycopy(a, lo, buf, lo, hi - lo);
        int i = lo;
        int j = mid;
        for (int k = lo; k < hi; k++) {
            if (i >= mid) {
                a[k] = buf[j++];
            } else if (j >= hi) {
                a[k] = buf[i++];
            } else {
                metrics.comparison();
                a[k] = g.weight(buf[j]) < g.weight(buf[i]) ? buf[j++] : buf[i++];
            }
        }
    }

    static void requireUndirected(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
    }
}
