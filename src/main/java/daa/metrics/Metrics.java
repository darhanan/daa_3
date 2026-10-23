package daa.metrics;

/**
 * Operation counters. The algorithms increment them while they work; nothing here is computed
 * from a formula afterwards.
 */
public final class Metrics {
    private long dsuOps;
    private long edgeScans;
    private long comparisons;
    private long maxDepth;

    /** One find call or one successful union. */
    public void dsuOp() {
        dsuOps++;
    }

    /** One edge looked at. */
    public void edgeScan() {
        edgeScans++;
    }

    /** One comparison of two weights (or of a tentative distance with a candidate). */
    public void comparison() {
        comparisons++;
    }

    /** Records the current size of an explicit DFS stack. */
    public void depth(long current) {
        if (current > maxDepth) {
            maxDepth = current;
        }
    }

    public long dsuOps() {
        return dsuOps;
    }

    public long edgeScans() {
        return edgeScans;
    }

    public long comparisons() {
        return comparisons;
    }

    public long maxDepth() {
        return maxDepth;
    }

    public boolean sameCounts(Metrics o) {
        return dsuOps == o.dsuOps && edgeScans == o.edgeScans
                && comparisons == o.comparisons && maxDepth == o.maxDepth;
    }

    @Override
    public String toString() {
        return "dsuOps=" + dsuOps + ", edgeScans=" + edgeScans
                + ", comparisons=" + comparisons + ", maxDepth=" + maxDepth;
    }
}
