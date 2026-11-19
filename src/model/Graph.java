package model;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс графа. Все алгоритмы работают через этот интерфейс,
 * что позволяет при необходимости подменить реализацию (например, на матрицу смежности).
 */
public interface Graph {

    boolean isDirected();

    // --- Управление вершинами ---
    void addVertex(Vertex v);
    boolean removeVertex(Vertex v);
    boolean containsVertex(Vertex v);
    Collection<Vertex> getVertices();

    // --- Управление рёбрами ---
    void addEdge(Vertex from, Vertex to, double weight);
    void addEdge(Vertex from, Vertex to); // вес = 1 по умолчанию
    boolean removeEdge(Vertex from, Vertex to);
    boolean containsEdge(Vertex from, Vertex to);
    Collection<Edge> getEdges();

    // --- Запросы для алгоритмов ---
    /** Все рёбра, инцидентные данной вершине. */
    List<Edge> getIncidentEdges(Vertex v);

    /** Вершины-соседи данной вершины. */
    List<Vertex> getNeighbors(Vertex v);

    int vertexCount();
    int edgeCount();
}