package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StackSafetyTest {
    private static final int N = 100_000;

    private static Graph path(boolean closeCycle) {
        Graph g = new Graph(N, true);
        for (int v = 0; v + 1 < N; v++) {
            g.addEdge(v, v + 1, 1);
        }
        if (closeCycle) {
            g.addEdge(N - 1, 0, 1);
        }
        return g;
    }

    @Test
    void kosarajuOnPathOfHundredThousandVertices() {
        Metrics m = new Metrics();
        SccResult r = assertDoesNotThrow(() -> Kosaraju.run(path(false), m));
        assertEquals(N, r.count());
        assertEquals(N, m.maxDepth(), "a path is one long DFS branch");
    }

    @Test
    void kosarajuAndTarjanOnOneBigCycle() {
        Graph g = path(true);
        assertEquals(1, Kosaraju.run(g).count());
        assertEquals(1, Tarjan.run(g).count());
    }

    @Test
    void tarjanAndShortestPathsOnLongPath() {
        Graph g = path(false);
        assertEquals(N, Tarjan.run(g).count());
        assertEquals((long) (N - 1) * N / 2, DagShortestPath.fromAllSources(g).sumOfDistances());
    }
}
