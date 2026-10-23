package daa.algorithms;

import daa.TestGraphs;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.graph.GraphReader;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoTest {
    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(21);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(200);
            Graph g = GraphGenerator.dag(n, rnd.nextInt(4 * n + 1), t);
            TopoResult r = Kahn.run(g);
            assertFalse(r.hasCycle());
            assertTrue(Kahn.isValidOrder(g, r.order()), "graph " + t);
        }
        Graph dag = TestGraphs.load("dag.txt");
        assertTrue(Kahn.isValidOrder(dag, Kahn.run(dag).order()));
    }

    @Test
    void cycleIsReportedInsteadOfAnOrder() {
        TopoResult r = Kahn.run(TestGraphs.load("cyclic.txt"));
        assertTrue(r.hasCycle());
        assertNull(r.order());
        assertThrows(IllegalStateException.class, r::orderOrThrow);

        Graph g = GraphReader.parse("4 4 directed\n0 1 1\n1 2 1\n2 1 1\n2 3 1\n");
        TopoResult partial = Kahn.run(g);
        assertTrue(partial.hasCycle());
        assertEquals(1, partial.processed());
    }

    @Test
    void selfLoopIsACycle() {
        assertTrue(Kahn.run(GraphReader.parse("2 1 directed\n1 1 3\n")).hasCycle());
    }

    @Test
    void validatorRejectsBrokenOrders() {
        Graph g = GraphReader.parse("3 2 directed\n0 1 1\n1 2 1\n");
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{1, 0, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 0, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1}));
    }

    @Test
    void scansEveryEdgeOnce() {
        Graph g = GraphGenerator.dag(1000, 4000);
        Metrics m = new Metrics();
        Kahn.run(g, m);
        assertEquals(g.m(), m.edgeScans());
    }
}
