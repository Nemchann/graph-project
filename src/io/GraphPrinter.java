package io;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Вывод графа в удобочитаемом виде.
 * Поддерживает два формата: матрица смежности и списки смежности.
 */
public class GraphPrinter {

    /**
     * Выводит матрицу смежности.
     * Для взвешенного графа в ячейках — веса рёбер (0 означает отсутствие ребра).
     * Для невзвешенного — 1/0.
     */
    public static void printAdjacencyMatrix(Graph graph, PrintStream out) {
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        // сортируем по id для стабильного порядка
        vertices.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        // индекс вершины -> позиция в матрице
        Map<Vertex, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            index.put(vertices.get(i), i);
        }

        int n = vertices.size();
        double[][] matrix = new double[n][n];

        for (Edge e : graph.getEdges()) {
            int i = index.get(e.getSource());
            int j = index.get(e.getTarget());
            matrix[i][j] = e.getWeight();
            if (!graph.isDirected()) {
                matrix[j][i] = e.getWeight();
            }
        }

        // заголовок
        out.println("Матрица смежности (" + (graph.isDirected() ? "ориентированный" : "неориентированный")
                + " граф, |V|=" + n + ", |E|=" + graph.edgeCount() + "):");

        // шапка с именами вершин
        out.print("    ");
        for (Vertex v : vertices) {
            out.printf("%6s", v.getLabel());
        }
        out.println();

        // строки
        for (int i = 0; i < n; i++) {
            out.printf("%4s", vertices.get(i).getLabel());
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == 0) {
                    out.print("     0");
                } else if (matrix[i][j] == (int) matrix[i][j]) {
                    out.printf("%6d", (int) matrix[i][j]);
                } else {
                    out.printf("%6.2f", matrix[i][j]);
                }
            }
            out.println();
        }
        out.println();
    }

    /**
     * Выводит списки смежности.
     */
    public static void printAdjacencyLists(Graph graph, PrintStream out) {
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        out.println("Списки смежности (" + (graph.isDirected() ? "ориентированный" : "неориентированный")
                + " граф, |V|=" + vertices.size() + ", |E|=" + graph.edgeCount() + "):");

        for (Vertex v : vertices) {
            out.printf("  %s -> ", v.getLabel());
            List<Edge> incident = graph.getIncidentEdges(v);
            if (incident.isEmpty()) {
                out.println("(нет соседей)");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < incident.size(); i++) {
                    Edge e = incident.get(i);
                    Vertex neighbor = e.getOther(v);
                    sb.append(neighbor.getLabel());
                    if (e.getWeight() != 1.0) {
                        if (e.getWeight() == (int) e.getWeight()) {
                            sb.append("(w=").append((int) e.getWeight()).append(")");
                        } else {
                            sb.append("(w=").append(String.format("%.2f", e.getWeight())).append(")");
                        }
                    }
                    if (i < incident.size() - 1) sb.append(", ");
                }
                out.println(sb);
            }
        }
        out.println();
    }
}