package daa.algorithms;

import daa.metrics.Metrics;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DSUTest {
    /** Reference: every vertex stores its set label; a union relabels a whole set in O(n). */
    private static final class NaiveSets {
        final int[] label;
        int sets;

        NaiveSets(int n) {
            label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = i;
            }
            sets = n;
        }

        boolean union(int a, int b) {
            int la = label[a];
            int lb = label[b];
            if (la == lb) {
                return false;
            }
            for (int i = 0; i < label.length; i++) {
                if (label[i] == lb) {
                    label[i] = la;
                }
            }
            sets--;
            return true;
        }
    }

    @RepeatedTest(20)
    void agreesWithNaiveReferenceAfterRandomUnions(RepetitionInfo info) {
        Random rnd = new Random(info.getCurrentRepetition());
        int n = 1 + rnd.nextInt(300);
        DSU dsu = new DSU(n);
        NaiveSets ref = new NaiveSets(n);
        for (int k = 0; k < 2 * n; k++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            assertEquals(ref.union(a, b), dsu.union(a, b));
        }
        for (int a = 0; a < n; a++) {
            for (int b = 0; b < n; b += 7) {
                assertEquals(ref.label[a] == ref.label[b], dsu.find(a) == dsu.find(b));
            }
        }
        assertEquals(ref.sets, dsu.components());
    }

    @Test
    void countsFindsAndSuccessfulUnions() {
        Metrics m = new Metrics();
        DSU dsu = new DSU(4, m);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        dsu.find(3);
        assertEquals(5, dsu.findCalls());
        assertEquals(1, dsu.unionCalls());
        assertEquals(6, m.dsuOps());
        assertEquals(3, dsu.components());
    }

    @Test
    void pathCompressionFlattensLongChains() {
        int n = 1 << 16;
        DSU dsu = new DSU(n);
        for (int i = 1; i < n; i++) {
            dsu.union(0, i);
        }
        assertEquals(1, dsu.components());
        int root = dsu.find(0);
        for (int i = 0; i < n; i++) {
            assertEquals(root, dsu.find(i));
        }
    }

    @Test
    void makeSetResets() {
        DSU dsu = new DSU(3);
        dsu.union(0, 2);
        dsu.makeSet(5);
        assertEquals(5, dsu.components());
        assertFalse(dsu.find(0) == dsu.find(2));
    }
}
