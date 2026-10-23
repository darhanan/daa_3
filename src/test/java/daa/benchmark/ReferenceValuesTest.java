package daa.benchmark;

import daa.algorithms.DagShortestPath;
import daa.algorithms.Kosaraju;
import daa.algorithms.Kruskal;
import daa.algorithms.Prim;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The first six rows of the reference table (the rest are checked by the benchmark itself). */
class ReferenceValuesTest {
    @Test
    void reproducesReferenceTable() {
        for (int s = 0; s < 3; s++) {
            for (int d = 0; d < Benchmark.DENSITIES.length; d++) {
                int n = Benchmark.SIZES[s];
                int m = Benchmark.DENSITIES[d] * n;
                long[] ref = Benchmark.REFERENCE[s * Benchmark.DENSITIES.length + d];
                Graph u = GraphGenerator.undirected(n, m);
                assertEquals(ref[0], Kruskal.run(u).totalWeight(), "MST n=" + n + " m=" + m);
                assertEquals(ref[0], Prim.run(u).totalWeight(), "MST n=" + n + " m=" + m);
                assertEquals(ref[1], Kosaraju.run(GraphGenerator.directed(n, m)).count(), "SCC n=" + n);
                assertEquals(ref[2], DagShortestPath.fromAllSources(GraphGenerator.dag(n, m)).sumOfDistances(),
                        "DAG n=" + n);
            }
        }
    }
}
