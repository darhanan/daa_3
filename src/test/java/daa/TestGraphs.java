package daa;

import daa.graph.Graph;
import daa.graph.GraphReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Random;

/** Small random graphs and the data/ files shared by the tests. */
public final class TestGraphs {
    private TestGraphs() {
    }

    public static Graph load(String name) {
        try {
            return GraphReader.read(Path.of("data", name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Random directed graph that may contain self-loops and parallel edges. */
    public static Graph randomDirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    /** Random DAG (edges go from a lower to a higher index) with weights in [-10, 20]. */
    public static Graph randomDag(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m && n > 1; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) {
                continue;
            }
            g.addEdge(Math.min(a, b), Math.max(a, b), rnd.nextInt(31) - 10);
        }
        return g;
    }
}
