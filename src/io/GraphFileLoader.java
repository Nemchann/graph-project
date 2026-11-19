package io;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Загрузка графа из текстового файла.
 * Формат:
 *   V <количество_вершин>          — объявление вершин (id от 1 до N)
 *   E <u> <v> [<weight>]           — ребро между вершинами u и v
 *   Строки, начинающиеся с #, считаются комментариями.
 */
public class GraphFileLoader {

    public static Graph load(Path path, boolean directed) throws IOException {
        AdjacencyListGraph graph = new AdjacencyListGraph(directed);
        Map<Integer, Vertex> index = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                switch (parts[0].toUpperCase()) {
                    case "V" -> {
                        int n = Integer.parseInt(parts[1]);
                        for (int i = 1; i <= n; i++) {
                            Vertex v = new Vertex(i);
                            graph.addVertex(v);
                            index.put(i, v);
                        }
                    }
                    case "E" -> {
                        if (parts.length < 3) {
                            throw new IllegalArgumentException(
                                    "Строка " + lineNum + ": ребро должно иметь минимум u и v");
                        }
                        int u = Integer.parseInt(parts[1]);
                        int v = Integer.parseInt(parts[2]);
                        double w = parts.length >= 4 ? Double.parseDouble(parts[3]) : 1.0;
                        Vertex vu = index.get(u);
                        Vertex vv = index.get(v);
                        if (vu == null || vv == null) {
                            throw new IllegalArgumentException(
                                    "Строка " + lineNum + ": неизвестная вершина");
                        }
                        graph.addEdge(vu, vv, w);
                    }
                    default -> throw new IllegalArgumentException(
                            "Строка " + lineNum + ": неизвестная директива " + parts[0]);
                }
            }
        }
        return graph;
    }
}