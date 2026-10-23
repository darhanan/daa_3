package daa.algorithms;

/**
 * Result of Kahn's algorithm. On a DAG {@code order} holds all n vertices; on a cyclic graph
 * {@code hasCycle} is true, {@code order} is null and {@code processed} tells how many vertices
 * left the queue before it ran dry.
 */
public record TopoResult(int[] order, boolean hasCycle, int processed) {
    public int[] orderOrThrow() {
        if (hasCycle) {
            throw new IllegalStateException("graph has a cycle: only " + processed + " vertices could be ordered");
        }
        return order;
    }
}
