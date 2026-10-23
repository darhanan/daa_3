package daa.algorithms;

import daa.graph.Graph;

import java.util.List;

/**
 * Bonus task B. The condensation turns every SCC into one vertex and keeps the edges between
 * different SCCs; it is always a DAG. Its longest (critical) path is found with the DAG
 * shortest-path pass on negated weights.
 */
public final class Condensation {
    private Condensation() {
    }

    public static Graph build(Graph g, SccResult scc) {
        int[] comp = scc.componentOf();
        Graph dag = new Graph(scc.count(), true);
        for (int e = 0; e < g.m(); e++) {
            int a = comp[g.from(e)];
            int b = comp[g.to(e)];
            if (a != b) {
                dag.addEdge(a, b, g.weight(e));
            }
        }
        return dag;
    }

    /** Longest path in a DAG: its length and its vertices from start to end. */
    public record CriticalPath(long length, List<Integer> path) {
    }

    public static CriticalPath longestPath(Graph dag) {
        Graph negated = new Graph(dag.n(), true);
        for (int e = 0; e < dag.m(); e++) {
            negated.addEdge(dag.from(e), dag.to(e), -dag.weight(e));
        }
        PathResult r = DagShortestPath.fromAllSources(negated);
        int end = -1;
        long best = 0;
        for (int v = 0; v < dag.n(); v++) {
            if (end == -1 || r.dist()[v] < best) {
                best = r.dist()[v];
                end = v;
            }
        }
        if (end == -1) {
            return new CriticalPath(0, List.of());
        }
        return new CriticalPath(-best, r.pathTo(end));
    }
}
