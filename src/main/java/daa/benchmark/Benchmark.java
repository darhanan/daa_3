package daa.benchmark;

import daa.algorithms.Condensation;
import daa.algorithms.DagShortestPath;
import daa.algorithms.Kahn;
import daa.algorithms.Kosaraju;
import daa.algorithms.Kruskal;
import daa.algorithms.MstResult;
import daa.algorithms.Prim;
import daa.algorithms.SccResult;
import daa.algorithms.Tarjan;
import daa.algorithms.TopoResult;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ToLongFunction;

/**
 * Runs every algorithm on the ten generated graphs and writes results/results.csv.
 *
 * <p>The run checks itself and stops with an exception if anything is off: Kruskal and Prim must
 * agree, Kahn's order must be valid, Tarjan must match Kosaraju, the edge-scan counts must equal
 * 2m / m / m, the counters must be identical in every repetition, and the MST weight, SCC count
 * and sum of DAG distances must equal the reference table of the assignment.
 */
public final class Benchmark {
    static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    static final int[] DENSITIES = {4, 20};

    /** {MST total weight, SCC count, sum of DAG distances} per (size, density), from the assignment. */
    static final long[][] REFERENCE = {
            {147_970, 27, 396_274}, {30_363, 1, 252_411},
            {468_911, 137, 1_271_911}, {97_589, 1, 723_157},
            {1_486_139, 373, 3_941_828}, {309_550, 1, 2_381_342},
            {4_715_515, 1_188, 12_551_891}, {968_517, 1, 7_376_612},
            {15_012_094, 3_923, 40_096_539}, {3_056_681, 1, 23_512_087},
    };

    static final String HEADER = "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private final int runs;
    private final List<String> rows = new ArrayList<>();
    private final List<String> bonusRows = new ArrayList<>();
    private final List<String> criticalRows = new ArrayList<>();

    Benchmark(int runs) {
        this.runs = runs;
    }

    public static void main(String[] args) throws IOException {
        int runs = 5;
        Path outDir = Path.of("results");
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--runs" -> runs = Integer.parseInt(args[++i]);
                case "--out" -> outDir = Path.of(args[++i]);
                default -> throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }
        new Benchmark(runs).runAll(outDir);
    }

    void runAll(Path outDir) throws IOException {
        System.out.printf(Locale.ROOT, "%-22s %12s %8s %14s   %s%n", "graph", "MST weight", "SCCs", "sum DAG dist", "reference");
        for (int s = 0; s < SIZES.length; s++) {
            for (int d = 0; d < DENSITIES.length; d++) {
                runGraph(SIZES[s], DENSITIES[d], REFERENCE[s * DENSITIES.length + d]);
            }
        }
        Files.createDirectories(outDir);
        write(outDir.resolve("results.csv"), HEADER, rows);
        write(outDir.resolve("bonus_tarjan.csv"), HEADER, bonusRows);
        write(outDir.resolve("bonus_critical_path.csv"),
                "graph,n,m,scc_count,condensation_edges,critical_path_length,critical_path_vertices", criticalRows);
        System.out.println("All checks passed. Wrote " + rows.size() + " rows to " + outDir.resolve("results.csv"));
    }

    private void runGraph(int n, int density, long[] ref) {
        int m = density * n;
        String name = (density == 4 ? "sparse" : "dense") + "-" + n;
        Graph undirected = GraphGenerator.undirected(n, m);
        Graph directed = GraphGenerator.directed(n, m);
        Graph dag = GraphGenerator.dag(n, m);

        // Correctness checks, outside the timed region.
        MstResult kruskal = Kruskal.run(undirected);
        MstResult prim = Prim.run(undirected);
        check(kruskal.totalWeight() == prim.totalWeight(),
                name + ": Kruskal weight " + kruskal.totalWeight() + " != Prim weight " + prim.totalWeight());
        check(kruskal.edgeCount() == n - 1 && prim.edgeCount() == n - 1, name + ": MST must have n - 1 edges");
        SccResult kosaraju = Kosaraju.run(directed);
        check(kosaraju.samePartition(Tarjan.run(directed)), name + ": Tarjan and Kosaraju disagree");
        TopoResult topo = Kahn.run(dag);
        check(!topo.hasCycle() && Kahn.isValidOrder(dag, topo.order()), name + ": invalid topological order");
        long distSum = DagShortestPath.fromAllSources(dag).sumOfDistances();

        boolean refOk = kruskal.totalWeight() == ref[0] && kosaraju.count() == ref[1] && distSum == ref[2];
        System.out.printf(Locale.ROOT, "%-22s %,12d %,8d %,14d   %s%n", name + " (m=" + m + ")",
                kruskal.totalWeight(), kosaraju.count(), distSum, refOk ? "OK" : "MISMATCH");
        check(refOk, name + ": reference values differ, expected " + Arrays.toString(ref));

        Metrics k = measure("Kruskal", name, undirected, rows, mt -> Kruskal.run(undirected, mt).totalWeight());
        measure("Prim", name, undirected, rows, mt -> Prim.run(undirected, mt).totalWeight());
        Metrics ko = measure("Kosaraju", name, directed, rows, mt -> Kosaraju.run(directed, mt).count());
        Metrics ka = measure("Kahn", name, dag, rows, mt -> Kahn.run(dag, mt).processed());
        Metrics sp = measure("DagShortestPath", name, dag, rows,
                mt -> DagShortestPath.fromAllSources(dag, mt).sumOfDistances());
        check(ko.edgeScans() == 2L * m, name + ": Kosaraju must scan 2m edges, scanned " + ko.edgeScans());
        check(ka.edgeScans() == m, name + ": Kahn must scan m edges, scanned " + ka.edgeScans());
        check(sp.edgeScans() == m, name + ": DAG relaxation must scan m edges, scanned " + sp.edgeScans());
        check(k.dsuOps() > 0, name + ": Kruskal performed no DSU operations");

        // Bonus: Tarjan and the critical path of the condensation.
        Metrics t = measure("Tarjan", name, directed, bonusRows, mt -> Tarjan.run(directed, mt).count());
        check(t.edgeScans() == m, name + ": Tarjan must scan m edges, scanned " + t.edgeScans());
        Graph cond = Condensation.build(directed, kosaraju);
        Condensation.CriticalPath cp = Condensation.longestPath(cond);
        criticalRows.add(String.join(",", name, str(n), str(m), str(kosaraju.count()), str(cond.m()),
                str(cp.length()), str(cp.path().size())));
    }

    /** Runs the algorithm once to warm up, then {@code runs} times; returns the counters. */
    private Metrics measure(String algorithm, String graphName, Graph g, List<String> out, ToLongFunction<Metrics> body) {
        body.applyAsLong(new Metrics());
        double[] times = new double[runs];
        Metrics first = null;
        long result = 0;
        for (int r = 0; r < runs; r++) {
            Metrics mt = new Metrics();
            long t0 = System.nanoTime();
            long value = body.applyAsLong(mt);
            times[r] = (System.nanoTime() - t0) / 1e6;
            if (first == null) {
                first = mt;
                result = value;
            } else {
                check(first.sameCounts(mt) && value == result,
                        algorithm + " on " + graphName + ": counters changed between runs");
            }
        }
        Arrays.sort(times);
        double median = times[runs / 2];
        out.add(String.join(",", algorithm, graphName, str(g.n()), str(g.m()),
                String.format(Locale.ROOT, "%.3f", median), str(first.dsuOps()), str(first.edgeScans()),
                str(first.comparisons()), str(first.maxDepth()), str(result)));
        return first;
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new IllegalStateException("CHECK FAILED: " + message);
        }
    }

    private static String str(long v) {
        return Long.toString(v);
    }

    private static void write(Path file, String header, List<String> lines) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println(header);
            lines.forEach(w::println);
        }
    }
}
