package daa.graph;

import daa.TestGraphs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {
    @Test
    void readsHeaderAndEdges() {
        Graph g = TestGraphs.load("connected.txt");
        assertFalse(g.isDirected());
        assertEquals(6, g.n());
        assertEquals(9, g.m());
        assertTrue(TestGraphs.load("cyclic.txt").isDirected());
    }

    @Test
    void undirectedEdgeIsListedAtBothEndsSelfLoopOnce() {
        Graph g = GraphReader.parse("3 2 undirected\n0 1 5\n2 2 1\n");
        assertEquals(1, g.degree(0));
        assertEquals(1, g.degree(1));
        assertEquals(1, g.degree(2));
        assertEquals(1, g.other(g.edgeAt(0, 0), 0));
        assertEquals(0, g.other(g.edgeAt(1, 0), 1));
    }

    @Test
    void directedEdgeIsListedAtTailOnlyAndReverseFlipsIt() {
        Graph g = GraphReader.parse("3 2 directed\n0 1 5\n1 2 -3\n");
        assertEquals(1, g.degree(0));
        assertEquals(1, g.degree(1));
        assertEquals(0, g.degree(2));
        assertArrayEquals(new int[]{0, 1, 1}, g.inDegrees());
        Graph r = g.reverse();
        assertEquals(0, r.degree(0));
        assertEquals(1, r.degree(2));
        assertEquals(-3, r.weight(r.edgeAt(2, 0)));
    }

    @Test
    void isolatedVerticesAndEmptyGraphAreFine() {
        Graph g = GraphReader.parse("4 0 undirected\n");
        assertEquals(4, g.n());
        assertEquals(0, g.m());
        assertEquals(0, g.degree(3));
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("2 1 undirected\n0 5 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("2 2 directed\n0 1 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("2 1 mixed\n0 1 1\n"));
    }

    @Test
    void generatorBuildsTheRequestedShapes() {
        Graph u = GraphGenerator.undirected(1000, 4000);
        Graph d = GraphGenerator.directed(1000, 4000);
        Graph dag = GraphGenerator.dag(1000, 4000);
        assertEquals(4000, u.m());
        assertEquals(4000, d.m());
        assertEquals(4000, dag.m());
        for (int e = 0; e < d.m(); e++) {
            assertTrue(d.from(e) != d.to(e), "generator must not create self-loops");
        }
    }
}
