"""Draws the three report plots from results/results.csv and writes results/analysis.md
(counter / formula ratios and log-log slopes).

    python3 scripts/plots.py
"""
import csv
import math
import os
from collections import defaultdict

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV = os.path.join(ROOT, "results", "results.csv")
PLOTS = os.path.join(ROOT, "results", "plots")

ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = {"Kruskal": "#2a78d6", "Prim": "#eb6834", "Kosaraju": "#1baf7a",
          "Kahn": "#eda100", "DagShortestPath": "#e87ba4"}
MARKERS = {"Kruskal": "o", "Prim": "s", "Kosaraju": "^", "Kahn": "D", "DagShortestPath": "v"}
DENSITIES = ["sparse", "dense"]
INK, INK_2, GRID = "#0b0b0b", "#52514e", "#e4e3df"


def load():
    data = defaultdict(list)  # (algorithm, density) -> rows sorted by n
    with open(CSV) as f:
        for r in csv.DictReader(f):
            density = r["graph"].split("-")[0]
            row = {k: (r[k] if k in ("algorithm", "graph") else float(r[k])) for k in r}
            data[(r["algorithm"], density)].append(row)
    for rows in data.values():
        rows.sort(key=lambda r: r["n"])
    return data


def slope(xs, ys):
    lx = [math.log10(x) for x in xs]
    ly = [math.log10(y) for y in ys]
    mx, my = sum(lx) / len(lx), sum(ly) / len(ly)
    return sum((a - mx) * (b - my) for a, b in zip(lx, ly)) / sum((a - mx) ** 2 for a in lx)


def total_ops(r):
    return r["dsu_ops"] + r["edge_scans"] + r["comparisons"]


def style(ax):
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(GRID)
    ax.tick_params(colors=INK_2, labelsize=9)
    ax.grid(True, which="major", color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)


def n_axis(ax, ns):
    ax.set_xscale("log")
    ax.set_xticks(ns)
    ax.set_xticklabels([f"{int(n):,}" for n in ns])
    ax.minorticks_off()
    ax.set_xlabel("vertices n (log scale)", color=INK_2, fontsize=9)


def plot_operations(data, ns):
    fig, axes = plt.subplots(1, 2, figsize=(11, 5), sharey=True)
    for ax, density in zip(axes, DENSITIES):
        for alg in ALGORITHMS:
            rows = data[(alg, density)]
            label = "DagShortestPath (same 2m as Kosaraju)" if alg == "DagShortestPath" else alg
            ax.plot([r["n"] for r in rows], [total_ops(r) for r in rows], color=COLORS[alg],
                    marker=MARKERS[alg], markersize=6, linewidth=2, label=label)
        n_axis(ax, ns)
        ax.set_yscale("log")
        ax.set_title(f"{density} (m = {4 if density == 'sparse' else 20}n)", color=INK, fontsize=11, loc="left")
        style(ax)
    axes[0].set_ylabel("counted operations (log scale)\ndsuOps + edgeScans + comparisons", color=INK_2, fontsize=9)
    handles, labels = axes[0].get_legend_handles_labels()
    fig.legend(handles, labels, frameon=False, fontsize=9, loc="lower center", ncol=5)
    fig.suptitle("Operations vs n: every line is straight on log-log axes", x=0.06, ha="left", color=INK, fontsize=13)
    fig.tight_layout(rect=(0, 0.07, 1, 1))
    fig.savefig(os.path.join(PLOTS, "operations_vs_n.png"), dpi=150)
    plt.close(fig)


def plot_scans_per_edge(data, ns):
    fig, axes = plt.subplots(1, 2, figsize=(11, 5), sharey=True)
    for ax, density in zip(axes, DENSITIES):
        for alg in ALGORITHMS:
            rows = data[(alg, density)]
            ax.plot([r["n"] for r in rows], [r["edge_scans"] / r["m"] for r in rows], color=COLORS[alg],
                    marker=MARKERS[alg], markersize=6, linewidth=2, label=alg)
        n_axis(ax, ns)
        ax.set_ylim(0, 2.3)
        ax.set_title(f"{density} (m = {4 if density == 'sparse' else 20}n)", color=INK, fontsize=11, loc="left")
        ax.annotate("Prim, Kosaraju: 2 scans per edge", (ns[0], 2.0), xytext=(0, 8), textcoords="offset points",
                    color=INK_2, fontsize=8.5)
        ax.annotate("Kahn, DAG relaxation: 1 scan per edge", (ns[0], 1.0), xytext=(0, 8),
                    textcoords="offset points", color=INK_2, fontsize=8.5)
        style(ax)
    axes[0].set_ylabel("edgeScans / m", color=INK_2, fontsize=9)
    handles, labels = axes[0].get_legend_handles_labels()
    fig.legend(handles, labels, frameon=False, fontsize=9, loc="lower center", ncol=5)
    fig.suptitle("Scans per edge: flat lines confirm linear work", x=0.06, ha="left", color=INK, fontsize=13)
    fig.tight_layout(rect=(0, 0.07, 1, 1))
    fig.savefig(os.path.join(PLOTS, "scans_per_edge.png"), dpi=150)
    plt.close(fig)


def plot_kruskal_share(data, ns):
    fig, ax = plt.subplots(figsize=(8, 4.6))
    colors = {"sparse": "#2a78d6", "dense": "#eb6834"}
    for density in DENSITIES:
        rows = data[("Kruskal", density)]
        xs = [r["n"] for r in rows]
        ys = [100 * r["edge_scans"] / r["m"] for r in rows]
        ax.plot(xs, ys, color=colors[density], marker="o", markersize=7, linewidth=2)
        for x, y in zip(xs, ys):
            ax.annotate(f"{y:.0f}%", (x, y), xytext=(0, 9), textcoords="offset points", ha="center",
                        color=INK_2, fontsize=8.5)
        ax.annotate(f"{density} (m = {4 if density == 'sparse' else 20}n)", (xs[-1], ys[-1]), xytext=(12, -3),
                    textcoords="offset points", color=INK, fontsize=9.5)
    n_axis(ax, ns)
    ax.set_xlim(ns[0] * 0.75, ns[-1] * 2.6)
    ax.set_ylim(0, 110)
    ax.set_ylabel("share of sorted edges Kruskal examines (%)", color=INK_2, fontsize=9)
    style(ax)
    fig.suptitle("Kruskal stops early on dense graphs, not on sparse ones", x=0.08, ha="left", color=INK,
                 fontsize=13)
    fig.tight_layout()
    fig.savefig(os.path.join(PLOTS, "kruskal_share.png"), dpi=150)
    plt.close(fig)


def analysis(data):
    out = ["# Counters vs formulas (generated by scripts/plots.py)", ""]
    formulas = [
        ("Kruskal", "dsu_ops / m", lambda r: r["dsu_ops"] / r["m"]),
        ("Kruskal", "dsu_ops / edge_scans", lambda r: r["dsu_ops"] / r["edge_scans"]),
        ("Kruskal", "comparisons / (m log2 m)", lambda r: r["comparisons"] / (r["m"] * math.log2(r["m"]))),
        ("Prim", "edge_scans / m", lambda r: r["edge_scans"] / r["m"]),
        ("Prim", "comparisons / (m log2 m)", lambda r: r["comparisons"] / (r["m"] * math.log2(r["m"]))),
        ("Kosaraju", "edge_scans / (n + m)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
        ("Kahn", "edge_scans / (n + m)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
        ("DagShortestPath", "edge_scans / (n + m)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
    ]
    ns = [int(r["n"]) for r in data[("Kruskal", "sparse")]]
    out.append("| Algorithm | Ratio | Density | " + " | ".join(f"n={n:,}" for n in ns) + " |")
    out.append("|" + " --- |" * (3 + len(ns)))
    for alg, name, f in formulas:
        for density in DENSITIES:
            vals = [f(r) for r in data[(alg, density)]]
            out.append(f"| {alg} | {name} | {density} | " + " | ".join(f"{v:.3f}" for v in vals) + " |")
    out += ["", "# Log-log slopes", "", "| Algorithm | Counter | Sparse | Dense |", "| --- | --- | --- | --- |"]
    slopes = [(alg, "edge_scans", lambda r: r["edge_scans"]) for alg in ALGORITHMS]
    slopes += [("Kruskal", "comparisons", lambda r: r["comparisons"]),
               ("Kruskal", "all ops", total_ops),
               ("Prim", "comparisons", lambda r: r["comparisons"])]
    for alg, name, f in slopes:
        cells = []
        for density in DENSITIES:
            rows = data[(alg, density)]
            cells.append(f"{slope([r['n'] for r in rows], [f(r) for r in rows]):.3f}")
        out.append(f"| {alg} | {name} | " + " | ".join(cells) + " |")
    with open(os.path.join(ROOT, "results", "analysis.md"), "w") as fh:
        fh.write("\n".join(out) + "\n")
    print("\n".join(out))


def main():
    os.makedirs(PLOTS, exist_ok=True)
    data = load()
    ns = [r["n"] for r in data[("Kruskal", "sparse")]]
    plot_operations(data, ns)
    plot_scans_per_edge(data, ns)
    plot_kruskal_share(data, ns)
    analysis(data)


if __name__ == "__main__":
    main()
