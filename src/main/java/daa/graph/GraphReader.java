package daa.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the text format of the assignment: first line {@code n m directed|undirected}, then m lines
 * {@code u v w}. Blank lines and lines starting with '#' are ignored.
 */
public final class GraphReader {
    private GraphReader() {
    }

    public static Graph read(Path file) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return read(in);
        }
    }

    public static Graph parse(String text) {
        try {
            return read(new BufferedReader(new StringReader(text)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Graph read(Reader reader) throws IOException {
        BufferedReader in = reader instanceof BufferedReader b ? b : new BufferedReader(reader);
        String header = nextLine(in);
        if (header == null) {
            throw new IllegalArgumentException("empty input: expected 'n m directed|undirected'");
        }
        String[] h = header.trim().split("\\s+");
        if (h.length != 3) {
            throw new IllegalArgumentException("bad header '" + header + "': expected 'n m directed|undirected'");
        }
        int n = Integer.parseInt(h[0]);
        int m = Integer.parseInt(h[1]);
        boolean directed = switch (h[2].toLowerCase()) {
            case "directed" -> true;
            case "undirected" -> false;
            default -> throw new IllegalArgumentException("unknown graph kind '" + h[2] + "'");
        };
        Graph g = new Graph(n, directed);
        for (int i = 0; i < m; i++) {
            String line = nextLine(in);
            if (line == null) {
                throw new IllegalArgumentException("expected " + m + " edges, found " + i);
            }
            String[] p = line.trim().split("\\s+");
            if (p.length != 3) {
                throw new IllegalArgumentException("bad edge line '" + line + "': expected 'u v w'");
            }
            g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        }
        return g;
    }

    private static String nextLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            String t = line.trim();
            if (!t.isEmpty() && !t.startsWith("#")) {
                return t;
            }
        }
        return null;
    }
}
