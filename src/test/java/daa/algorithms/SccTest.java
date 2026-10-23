package daa.algorithms;

import daa.TestGraphs;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SccTest {
    @Test
    void kosarajuMatchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(11);
        for (int t = 0; t < 60; t++) {
            int n = 1 + rnd.nextInt(200);
            Graph g = TestGraphs.randomDirected(rnd, n, rnd.nextInt(3 * n));
            boolean[][] reach = reachability(g);
            SccResult scc = Kosaraju.run(g);
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    boolean same = reach[u][v] && reach[v][u];
                    assertEquals(same, scc.componentOf()[u] == scc.componentOf()[v], "graph " + t);
                }
            }
        }
    }

    @Test
    void tarjanReturnsTheSameComponentsAsKosaraju() {
        Random rnd = new Random(12);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(200);
            Graph g = TestGraphs.randomDirected(rnd, n, rnd.nextInt(4 * n));
            assertTrue(Kosaraju.run(g).samePartition(Tarjan.run(g)), "graph " + t);
        }
        Graph cyclic = TestGraphs.load("cyclic.txt");
        assertTrue(Kosaraju.run(cyclic).samePartition(Tarjan.run(cyclic)));
    }

    @Test
    void cyclicFileGraph() {
        Graph g = TestGraphs.load("cyclic.txt");
        SccResult scc = Kosaraju.run(g);
        assertEquals(4, scc.count());
        int[] c = scc.componentOf();
        assertTrue(c[0] == c[1] && c[1] == c[2]);
        assertTrue(c[3] == c[4] && c[4] == c[5]);
        assertTrue(c[0] != c[3] && c[6] != c[7]);
    }

    @Test
    void kosarajuScansEveryEdgeTwiceTarjanOnce() {
        Graph g = TestGraphs.randomDirected(new Random(5), 500, 3000);
        Metrics k = new Metrics();
        Kosaraju.run(g, k);
        Metrics t = new Metrics();
        Tarjan.run(g, t);
        assertEquals(2L * g.m(), k.edgeScans());
        assertEquals(g.m(), t.edgeScans());
        assertTrue(k.maxDepth() >= 1 && k.maxDepth() <= g.n());
    }

    @Test
    void componentsComeInTopologicalOrderOfTheCondensation() {
        Graph g = TestGraphs.load("cyclic.txt");
        int[] c = Kosaraju.run(g).componentOf();
        for (int e = 0; e < g.m(); e++) {
            assertTrue(c[g.from(e)] <= c[g.to(e)]);
        }
    }

    /** BFS from every vertex. */
    private static boolean[][] reachability(Graph g) {
        int n = g.n();
        boolean[][] reach = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            ArrayDeque<Integer> q = new ArrayDeque<>();
            q.add(s);
            reach[s][s] = true;
            while (!q.isEmpty()) {
                int v = q.poll();
                for (int i = 0; i < g.degree(v); i++) {
                    int u = g.to(g.edgeAt(v, i));
                    if (!reach[s][u]) {
                        reach[s][u] = true;
                        q.add(u);
                    }
                }
            }
        }
        return reach;
    }
}
