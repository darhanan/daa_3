package daa.algorithms;

import daa.TestGraphs;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {
    @Test
    void kruskalAndPrimAgreeOnHundredRandomConnectedGraphs() {
        Random rnd = new Random(7);
        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(200);
            int m = (n - 1) + rnd.nextInt(5 * n);
            Graph g = GraphGenerator.undirected(n, m, t);
            MstResult k = Kruskal.run(g);
            MstResult p = Prim.run(g);
            assertEquals(k.totalWeight(), p.totalWeight(), "graph " + t);
            assertEquals(n - 1, k.edgeCount());
            assertEquals(n - 1, p.edgeCount());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
            assertTrue(isSpanningForest(g, k));
            assertTrue(isSpanningForest(g, p));
        }
    }

    @Test
    void matchesBruteForceOnTinyGraphs() {
        Random rnd = new Random(3);
        for (int t = 0; t < 50; t++) {
            int n = 2 + rnd.nextInt(5);
            Graph g = GraphGenerator.undirected(n, n - 1 + rnd.nextInt(5), 1000 + t);
            assertEquals(bruteForceMst(g), Kruskal.run(g).totalWeight(), "graph " + t);
        }
    }

    @Test
    void smallFileGraph() {
        Graph g = TestGraphs.load("connected.txt");
        assertEquals(14, Kruskal.run(g).totalWeight());
        assertEquals(14, Prim.run(g).totalWeight());
    }

    @Test
    void disconnectedGraphGivesForestWithoutThrowing() {
        Graph g = TestGraphs.load("disconnected.txt");
        for (MstResult r : new MstResult[]{Kruskal.run(g), Prim.run(g)}) {
            assertEquals(12, r.totalWeight());
            assertEquals(4, r.edgeCount());
            assertEquals(3, r.components());
            assertTrue(isSpanningForest(g, r));
        }
    }

    @Test
    void selfLoopsParallelEdgesAndIsolatedVertices() {
        Graph g = TestGraphs.load("edge_cases.txt");
        for (MstResult r : new MstResult[]{Kruskal.run(g), Prim.run(g)}) {
            assertEquals(5, r.totalWeight());
            assertEquals(2, r.edgeCount());
            assertEquals(3, r.components());
        }
    }

    @Test
    void emptyAndSingleVertexGraphs() {
        assertEquals(0, Kruskal.run(new Graph(0, false)).totalWeight());
        assertEquals(0, Prim.run(new Graph(0, false)).edgeCount());
        assertEquals(1, Kruskal.run(new Graph(1, false)).components());
        assertEquals(1, Prim.run(new Graph(1, false)).components());
    }

    @Test
    void countersAreFilledInsideTheAlgorithms() {
        Graph g = GraphGenerator.undirected(1000, 4000);
        Metrics k = new Metrics();
        Kruskal.run(g, k);
        assertTrue(k.dsuOps() > 0 && k.comparisons() > 0 && k.edgeScans() >= 999);
        Metrics p = new Metrics();
        Prim.run(g, p);
        assertEquals(2L * g.m(), p.edgeScans());
        assertTrue(p.comparisons() > 0);
    }

    @Test
    void rejectsDirectedGraph() {
        Graph g = new Graph(2, true);
        assertThrows(IllegalArgumentException.class, () -> Kruskal.run(g));
        assertThrows(IllegalArgumentException.class, () -> Prim.run(g));
    }

    /** No cycle among the chosen edges and as many trees as the graph has components. */
    private static boolean isSpanningForest(Graph g, MstResult r) {
        DSU check = new DSU(g.n());
        for (int e : r.edges()) {
            if (!check.union(g.from(e), g.to(e))) {
                return false;
            }
        }
        DSU all = new DSU(g.n());
        for (int e = 0; e < g.m(); e++) {
            all.union(g.from(e), g.to(e));
        }
        return check.components() == all.components() && r.components() == all.components();
    }

    /** Tries every subset of n - 1 edges. */
    private static long bruteForceMst(Graph g) {
        int m = g.m();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != g.n() - 1) {
                continue;
            }
            DSU dsu = new DSU(g.n());
            long w = 0;
            boolean ok = true;
            for (int e = 0; e < m && ok; e++) {
                if ((mask & (1 << e)) != 0) {
                    ok = dsu.union(g.from(e), g.to(e));
                    w += g.weight(e);
                }
            }
            if (ok) {
                best = Math.min(best, w);
            }
        }
        return best;
    }
}
