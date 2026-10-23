package daa.algorithms;

import daa.TestGraphs;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DagShortestPathTest {
    @Test
    void matchesBellmanFordWithNegativeWeights() {
        Random rnd = new Random(31);
        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(60);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n + 1));
            assertArrayEquals(bellmanFord(g, true, 0), DagShortestPath.fromAllSources(g).dist(), "graph " + t);
            int s = rnd.nextInt(n);
            assertArrayEquals(bellmanFord(g, false, s), DagShortestPath.from(g, s).dist(), "graph " + t);
        }
    }

    @Test
    void pathsAreRestoredAndConsistentWithDistances() {
        Random rnd = new Random(32);
        for (int t = 0; t < 50; t++) {
            Graph g = TestGraphs.randomDag(rnd, 40, 120);
            PathResult r = DagShortestPath.fromAllSources(g);
            int[] in = g.inDegrees();
            for (int v = 0; v < g.n(); v++) {
                List<Integer> path = r.pathTo(v);
                assertEquals(v, path.get(path.size() - 1));
                assertEquals(0, in[path.get(0)], "a path starts at a source");
                long len = 0;
                for (int i = 0; i + 1 < path.size(); i++) {
                    len += lightestEdge(g, path.get(i), path.get(i + 1));
                }
                assertEquals(r.dist()[v], len);
            }
        }
    }

    @Test
    void smallFileGraph() {
        PathResult r = DagShortestPath.fromAllSources(TestGraphs.load("dag.txt"));
        assertArrayEquals(new long[]{0, 5, 3, 10, 7, 5}, r.dist());
        assertEquals(List.of(0, 2, 4), r.pathTo(4));
        assertEquals(30, r.sumOfDistances());
    }

    @Test
    void singleSourceLeavesOtherPartsUnreachable() {
        PathResult r = DagShortestPath.from(TestGraphs.load("dag.txt"), 1);
        assertFalse(r.reachable(0));
        assertEquals(List.of(), r.pathTo(0));
        assertEquals(List.of(1, 3, 4), r.pathTo(4), "the negative edge 3 -> 4 makes the longer route shorter");
        assertEquals(5, r.dist()[4]);
    }

    @Test
    void cyclicGraphIsRejected() {
        assertThrows(IllegalStateException.class, () -> DagShortestPath.fromAllSources(TestGraphs.load("cyclic.txt")));
    }

    @Test
    void relaxationScansEveryEdgeOnce() {
        Graph g = GraphGenerator.dag(1000, 20000);
        Metrics m = new Metrics();
        DagShortestPath.fromAllSources(g, m);
        assertEquals(g.m(), m.edgeScans());
        assertEquals(g.m(), m.comparisons());
    }

    @Test
    void criticalPathOfCondensation() {
        Graph g = TestGraphs.load("cyclic.txt");
        SccResult scc = Kosaraju.run(g);
        Graph cond = Condensation.build(g, scc);
        assertEquals(4, cond.n());
        assertEquals(3, cond.m());
        assertFalse(Kahn.run(cond).hasCycle());
        Condensation.CriticalPath cp = Condensation.longestPath(cond);
        assertEquals(2 + 4 + 1, cp.length());
        assertEquals(4, cp.path().size());

        Graph dag = TestGraphs.load("dag.txt");
        Condensation.CriticalPath longest = Condensation.longestPath(dag);
        assertEquals(5 + 2 + 7, longest.length());
        assertEquals(List.of(0, 1, 2, 3), longest.path());
    }

    private static long lightestEdge(Graph g, int u, int v) {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < g.degree(u); i++) {
            int e = g.edgeAt(u, i);
            if (g.to(e) == v) {
                best = Math.min(best, g.weight(e));
            }
        }
        return best;
    }

    /** Reference: n - 1 rounds over all edges. */
    private static long[] bellmanFord(Graph g, boolean allSources, int source) {
        int n = g.n();
        long[] dist = new long[n];
        int[] in = g.inDegrees();
        for (int v = 0; v < n; v++) {
            boolean start = allSources ? in[v] == 0 : v == source;
            dist[v] = start ? 0 : PathResult.UNREACHABLE;
        }
        for (int round = 1; round < n; round++) {
            for (int e = 0; e < g.m(); e++) {
                long du = dist[g.from(e)];
                if (du != PathResult.UNREACHABLE && du + g.weight(e) < dist[g.to(e)]) {
                    dist[g.to(e)] = du + g.weight(e);
                }
            }
        }
        return dist;
    }
}
