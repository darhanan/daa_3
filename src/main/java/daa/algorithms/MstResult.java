package daa.algorithms;

/**
 * A minimum spanning forest: the chosen edge ids, their total weight, and how many trees
 * (connected components) the forest has. A connected graph gives one tree with n - 1 edges.
 */
public record MstResult(int[] edges, long totalWeight, int components) {
    public int edgeCount() {
        return edges.length;
    }

    public boolean isSpanningTree() {
        return components <= 1;
    }
}
