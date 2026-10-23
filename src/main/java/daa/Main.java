package daa;

import daa.algorithms.DagShortestPath;
import daa.algorithms.Kahn;
import daa.algorithms.Kosaraju;
import daa.algorithms.Kruskal;
import daa.algorithms.MstResult;
import daa.algorithms.PathResult;
import daa.algorithms.Prim;
import daa.algorithms.SccResult;
import daa.algorithms.TopoResult;
import daa.benchmark.Benchmark;
import daa.graph.Graph;
import daa.graph.GraphReader;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Entry point.
 * <pre>
 *   java -jar target/graphs.jar                     run the benchmark (same as "benchmark")
 *   java -jar target/graphs.jar benchmark [--runs N] [--out DIR]
 *   java -jar target/graphs.jar run data/cyclic.txt run every applicable algorithm on one file
 * </pre>
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].startsWith("--")) {
            Benchmark.main(args);
        } else if (args[0].equals("benchmark")) {
            Benchmark.main(Arrays.copyOfRange(args, 1, args.length));
        } else if (args[0].equals("run") && args.length == 2) {
            runFile(Path.of(args[1]));
        } else {
            System.err.println("usage: benchmark [--runs N] [--out DIR] | run <graph-file>");
            System.exit(2);
        }
    }

    private static void runFile(Path file) throws Exception {
        Graph g = GraphReader.read(file);
        System.out.println(file + ": " + g);
        if (!g.isDirected()) {
            MstResult k = Kruskal.run(g);
            MstResult p = Prim.run(g);
            System.out.println("Kruskal: weight " + k.totalWeight() + ", " + k.edgeCount() + " edges, "
                    + k.components() + " component(s)");
            System.out.println("Prim:    weight " + p.totalWeight() + ", " + p.edgeCount() + " edges, "
                    + p.components() + " component(s)");
            return;
        }
        SccResult scc = Kosaraju.run(g);
        System.out.println("Kosaraju: " + scc.count() + " SCC(s) " + scc.components());
        TopoResult topo = Kahn.run(g);
        if (topo.hasCycle()) {
            System.out.println("Kahn: cycle detected, only " + topo.processed() + " of " + g.n()
                    + " vertices can be ordered");
            return;
        }
        System.out.println("Kahn: order " + Arrays.toString(topo.order()));
        PathResult sp = DagShortestPath.fromAllSources(g);
        for (int v = 0; v < g.n(); v++) {
            System.out.println("  dist[" + v + "] = " + sp.dist()[v] + "  path " + sp.pathTo(v));
        }
    }
}
