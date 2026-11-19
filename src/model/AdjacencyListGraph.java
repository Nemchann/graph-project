package model;

import java.util.*;

/**
 * Реализация графа через списки смежности.
 * Подходит для разреженных графов и для всех алгоритмов из задания
 * (обходы, Дейкстра, поиск циклов и т.д.).
 */
public class AdjacencyListGraph implements Graph {

    private final boolean directed;
    private final Map<Vertex, List<Edge>> adjacency = new LinkedHashMap<>();
    private final Set<Edge> allEdges = new LinkedHashSet<>();

    public AdjacencyListGraph(boolean directed) {
        this.directed = directed;
    }

    public AdjacencyListGraph() {
        this(false); // по умолчанию — неориентированный
    }

    @Override
    public boolean isDirected() { return directed; }

    // ---------- Вершины ----------

    @Override
    public void addVertex(Vertex v) {
        adjacency.putIfAbsent(v, new ArrayList<>());
    }

    @Override
    public boolean removeVertex(Vertex v) {
        if (!adjacency.containsKey(v)) return false;
        // удаляем все инцидентные рёбра
        List<Edge> incident = new ArrayList<>(adjacency.get(v));
        for (Edge e : incident) {
            removeEdge(e.getSource(), e.getTarget());
        }
        adjacency.remove(v);
        return true;
    }

    @Override
    public boolean containsVertex(Vertex v) {
        return adjacency.containsKey(v);
    }

    @Override
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    // ---------- Рёбра ----------

    @Override
    public void addEdge(Vertex from, Vertex to, double weight) {
        addVertex(from);
        addVertex(to);
        Edge e = new Edge(from, to, weight);
        adjacency.get(from).add(e);
        if (!directed && !from.equals(to)) {
            adjacency.get(to).add(e); // то же самое ребро в обратную сторону
        }
        allEdges.add(e);
    }

    @Override
    public void addEdge(Vertex from, Vertex to) {
        addEdge(from, to, 1.0);
    }

    @Override
    public boolean removeEdge(Vertex from, Vertex to) {
        List<Edge> listFrom = adjacency.get(from);
        if (listFrom == null) return false;
        Edge found = listFrom.stream()
                .filter(e -> e.getOther(from).equals(to))
                .findFirst().orElse(null);
        if (found == null) return false;

        listFrom.remove(found);
        if (!directed) {
            adjacency.get(to).remove(found);
        }
        allEdges.remove(found);
        return true;
    }

    @Override
    public boolean containsEdge(Vertex from, Vertex to) {
        List<Edge> list = adjacency.get(from);
        if (list == null) return false;
        return list.stream().anyMatch(e -> e.getOther(from).equals(to));
    }

    @Override
    public Collection<Edge> getEdges() {
        return Collections.unmodifiableSet(allEdges);
    }

    // ---------- Запросы ----------

    @Override
    public List<Edge> getIncidentEdges(Vertex v) {
        List<Edge> list = adjacency.get(v);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    @Override
    public List<Vertex> getNeighbors(Vertex v) {
        List<Edge> incident = getIncidentEdges(v);
        List<Vertex> result = new ArrayList<>(incident.size());
        for (Edge e : incident) result.add(e.getOther(v));
        return result;
    }

    @Override
    public int vertexCount() { return adjacency.size(); }

    @Override
    public int edgeCount() { return allEdges.size(); }
}