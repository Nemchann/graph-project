package algorithms;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Обходы графа: DFS и BFS.
 *
 * Оба метода возвращают TraversalResult, который содержит:
 *   - порядок обхода вершин (для вывода);
 *   - карту "родителей" (откуда пришли в каждую вершину) — это дерево обхода;
 *   - список рёбер дерева обхода.
 *
 * Для неориентированного графа корректно обрабатывается случай,
 * когда мы "возвращаемся" по тому же ребру — это не считается ни циклом,
 * ни ошибкой.
 */
public class GraphTraversal {

    /**
     * Результат обхода графа.
     */
    public static class TraversalResult {
        private final List<Vertex> order;              // порядок обхода
        private final Map<Vertex, Vertex> parent;      // дерево обхода: v -> откуда пришли
        private final List<Edge> treeEdges;            // рёбра дерева обхода
        private final int components;                  // количество компонент связности

        public TraversalResult(List<Vertex> order, Map<Vertex, Vertex> parent,
                               List<Edge> treeEdges, int components) {
            this.order = Collections.unmodifiableList(order);
            this.parent = Collections.unmodifiableMap(parent);
            this.treeEdges = Collections.unmodifiableList(treeEdges);
            this.components = components;
        }

        public List<Vertex> getOrder() { return order; }
        public Map<Vertex, Vertex> getParent() { return parent; }
        public List<Edge> getTreeEdges() { return treeEdges; }
        public int getComponents() { return components; }
    }

    // ====================== DFS ======================

    /** Обход в глубину всего графа (все компоненты связности). */
    public static TraversalResult dfs(Graph graph) {
        List<Vertex> order = new ArrayList<>();
        Map<Vertex, Vertex> parent = new LinkedHashMap<>();
        List<Edge> treeEdges = new ArrayList<>();
        Set<Vertex> visited = new LinkedHashSet<>();
        int components = 0;

        // Сортируем вершины по id — для стабильного порядка обхода
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        for (Vertex start : vertices) {
            if (!visited.contains(start)) {
                components++;
                parent.put(start, null); // корень компоненты
                dfsVisit(graph, start, visited, order, parent, treeEdges);
            }
        }
        return new TraversalResult(order, parent, treeEdges, components);
    }

    /** Обход в глубину из конкретной вершины. */
    public static TraversalResult dfs(Graph graph, Vertex start) {
        List<Vertex> order = new ArrayList<>();
        Map<Vertex, Vertex> parent = new LinkedHashMap<>();
        List<Edge> treeEdges = new ArrayList<>();
        Set<Vertex> visited = new LinkedHashSet<>();
        parent.put(start, null);
        dfsVisit(graph, start, visited, order, parent, treeEdges);
        return new TraversalResult(order, parent, treeEdges, 1);
    }

    /** Рекурсивный визит DFS. */
    private static void dfsVisit(Graph graph, Vertex v, Set<Vertex> visited,
                                 List<Vertex> order, Map<Vertex, Vertex> parent,
                                 List<Edge> treeEdges) {
        visited.add(v);
        order.add(v);

        for (Edge e : graph.getIncidentEdges(v)) {
            Vertex to = e.getOther(v);
            if (!visited.contains(to)) {
                parent.put(to, v);
                treeEdges.add(e);
                dfsVisit(graph, to, visited, order, parent, treeEdges);
            }
        }
    }

    // ====================== BFS ======================

    /** Обход в ширину всего графа (все компоненты связности). */
    public static TraversalResult bfs(Graph graph) {
        List<Vertex> order = new ArrayList<>();
        Map<Vertex, Vertex> parent = new LinkedHashMap<>();
        List<Edge> treeEdges = new ArrayList<>();
        Set<Vertex> visited = new LinkedHashSet<>();
        int components = 0;

        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        for (Vertex start : vertices) {
            if (!visited.contains(start)) {
                components++;
                parent.put(start, null);
                bfsVisit(graph, start, visited, order, parent, treeEdges);
            }
        }
        return new TraversalResult(order, parent, treeEdges, components);
    }

    /** Обход в ширину из конкретной вершины. */
    public static TraversalResult bfs(Graph graph, Vertex start) {
        List<Vertex> order = new ArrayList<>();
        Map<Vertex, Vertex> parent = new LinkedHashMap<>();
        List<Edge> treeEdges = new ArrayList<>();
        Set<Vertex> visited = new LinkedHashSet<>();
        parent.put(start, null);
        bfsVisit(graph, start, visited, order, parent, treeEdges);
        return new TraversalResult(order, parent, treeEdges, 1);
    }

    /** Визит BFS через очередь. */
    private static void bfsVisit(Graph graph, Vertex start, Set<Vertex> visited,
                                 List<Vertex> order, Map<Vertex, Vertex> parent,
                                 List<Edge> treeEdges) {
        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Vertex v = queue.poll();
            order.add(v);

            for (Edge e : graph.getIncidentEdges(v)) {
                Vertex to = e.getOther(v);
                if (!visited.contains(to)) {
                    visited.add(to);
                    parent.put(to, v);
                    treeEdges.add(e);
                    queue.add(to);
                }
            }
        }
    }

    // ====================== Вспомогательные методы ======================

    /** Найти вершину по id. */
    public static Vertex findVertexById(Graph graph, int id) {
        for (Vertex v : graph.getVertices()) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    /**
     * Восстановить путь от start до target по карте родителей.
     * Возвращает путь в порядке от start до target (включительно).
     * Если пути нет — возвращает пустой список.
     */
    public static List<Vertex> reconstructPath(Map<Vertex, Vertex> parent,
                                               Vertex start, Vertex target) {
        if (!parent.containsKey(target)) return List.of();
        LinkedList<Vertex> path = new LinkedList<>();
        Vertex cur = target;
        while (cur != null) {
            path.addFirst(cur);
            if (cur.equals(start)) break;
            cur = parent.get(cur);
        }
        if (!path.getFirst().equals(start)) return List.of(); // недостижима
        return path;
    }
}