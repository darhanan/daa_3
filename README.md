# DAA Assignment 3 — Graphs: DSU, MST, SCC and Shortest Paths

City Network & Job Scheduler: one `Graph` class (adjacency list, directed or undirected) and
hand-written DSU, Kruskal, Prim, Kosaraju, Kahn and DAG shortest paths, each counting its own
work. Bonus: Tarjan's SCC and the condensation critical path. The analysis is in
[REPORT.md](REPORT.md).

## Requirements

- JDK 17 or newer (developed on JDK 25)
- Maven is not needed: the Maven wrapper `./mvnw` downloads it on first use
- Python 3 with matplotlib, only to redraw the plots

## Build, test, benchmark

```bash
./mvnw clean package            # compiles and runs all JUnit 5 tests, builds target/graphs.jar
./mvnw test                     # tests only
java -jar target/graphs.jar     # benchmark: 10 graphs x 5 algorithms -> results/results.csv
python3 scripts/plots.py        # results/plots/*.png and results/analysis.md
```

The benchmark checks itself and exits with an error if any check fails. It requires Kruskal =
Prim, a valid topological order, Tarjan = Kosaraju, 2m / m / m edge scans, identical counters in
all 5 runs, and every reference value from the assignment.

Run every applicable algorithm on one input file:

```bash
java -jar target/graphs.jar run data/cyclic.txt
java -jar target/graphs.jar run data/disconnected.txt
java -jar target/graphs.jar benchmark --runs 5 --out results
```

## Input format

```
n m directed|undirected
u v w        (m lines, vertices 0..n-1, int weights)
```

`data/` holds small graphs used by the tests:

| File | Kind | Purpose |
| --- | --- | --- |
| `connected.txt` | undirected | MST weight 14 |
| `disconnected.txt` | undirected | 3 components, minimum spanning forest of weight 12 |
| `cyclic.txt` | directed | 4 SCCs including a self-loop; Kahn reports the cycle |
| `dag.txt` | directed, acyclic | negative edge, shortest paths and path restoring |
| `edge_cases.txt` | undirected | self-loops, parallel edges, isolated vertices |

## Layout

```
src/main/java/daa/graph        Graph, GraphReader, GraphGenerator (exact Random(42) generator)
src/main/java/daa/algorithms   DSU, Kruskal, Prim, Kosaraju, Tarjan, Kahn, DagShortestPath, Condensation
src/main/java/daa/metrics      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/daa/benchmark    Benchmark (self-checking, writes the CSVs)
src/test/java                  JUnit 5 tests (brute force, Bellman-Ford, stack safety, reference values)
data/                          input graphs
results/results.csv            50 rows: 5 algorithms x 10 graphs
results/bonus_*.csv            Tarjan and critical-path results
results/plots/                 the three report plots
scripts/plots.py               plots + counter/formula ratios + log-log slopes
```

## Tests

| Test | What it checks |
| --- | --- |
| `DSUTest` | `find` agrees with a naive relabeling reference after random unions; `components()` matches |
| `MstTest` | Kruskal = Prim on 100 random connected graphs with n - 1 edges; brute force on tiny graphs; forest on disconnected input |
| `SccTest` | Kosaraju against brute-force BFS reachability (n ≤ 200); Tarjan = Kosaraju |
| `TopoTest` | valid order on random DAGs; cycle and self-loop detected |
| `DagShortestPathTest` | equal to Bellman-Ford on 200 random DAGs with negative weights; restored paths |
| `StackSafetyTest` | SCC, Tarjan and DAG paths on a 100 000-vertex path, no `StackOverflowError` |
| `ReferenceValuesTest` | the first six rows of the reference table |

## AI-use acknowledgement

This project was written with the help of Claude (Anthropic), an AI assistant. It drafted the
code, tests, plotting script and report text. I reviewed the work, ran the tests and the
benchmark myself, and checked every reported number against `results/results.csv` and the
reference table.
