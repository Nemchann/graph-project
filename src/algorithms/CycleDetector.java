package algorithms;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Индивидуальное задание №13: проверить, содержит ли граф хотя бы один цикл.
 *
 * Поддерживаются как неориентированные, так и ориентированные графы.
 * Основной метод detect() работает за O(V + E) и, если цикл найден,
 * возвращает его вершины и рёбра.
 */
public class CycleDetector {

    /** Результат проверки наличия цикла. */
    public static class CycleResult {
        private final boolean hasCycle;
        private final List<Vertex> cycleVertices; // вершины цикла по порядку обхода
        private final List<Edge> cycleEdges;      // рёбра, образующие цикл

        public CycleResult(boolean hasCycle, List<Vertex> cycleVertices, List<Edge> cycleEdges) {
            this.hasCycle = hasCycle;
            this.cycleVertices = Collections.unmodifiableList(cycleVertices);
            this.cycleEdges = Collections.unmodifiableList(cycleEdges);
        }

        static CycleResult noCycle() {
            return new CycleResult(false, List.of(), List.of());
        }

        public boolean hasCycle() { return hasCycle; }
        public List<Vertex> getCycleVertices() { return cycleVertices; }
        public List<Edge> getCycleEdges() { return cycleEdges; }
        public int getCycleLength() { return cycleVertices.size(); }
    }

    // ==================== Точка входа ====================

    public static CycleResult detect(Graph graph) {
        return graph.isDirected() ? detectDirected(graph) : detectUndirected(graph);
    }

    /**
     * Быстрая проверка без поиска самого цикла (только неориентированный граф).
     * Граф — лес ⟺ |E| = |V| - C. Следовательно, цикл есть ⟺ |E| > |V| - C.
     */
    public static boolean hasCycleQuick(Graph graph) {
        if (graph.isDirected()) {
            throw new UnsupportedOperationException(
                    "Быстрая проверка через число компонент работает только для неориентированного графа");
        }
        int components = GraphTraversal.bfs(graph).getComponents();
        return graph.edgeCount() > graph.vertexCount() - components;
    }

    // ==================== Неориентированный граф ====================

    private static CycleResult detectUndirected(Graph graph) {
        Set<Vertex> visited = new HashSet<>();
        Set<Vertex> onStack = new HashSet<>();          // вершины текущей ветви рекурсии
        Map<Vertex, Vertex> parent = new HashMap<>();
        Map<Vertex, Edge> parentEdge = new HashMap<>(); // по какому ребру пришли в вершину

        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        for (Vertex start : vertices) {
            if (!visited.contains(start)) {
                CycleResult result = dfsUndirected(graph, start, visited, onStack, parent, parentEdge);
                if (result != null) return result;
            }
        }
        return CycleResult.noCycle();
    }

    private static CycleResult dfsUndirected(Graph graph, Vertex v,
                                             Set<Vertex> visited, Set<Vertex> onStack,
                                             Map<Vertex, Vertex> parent,
                                             Map<Vertex, Edge> parentEdge) {
        visited.add(v);
        onStack.add(v);

        for (Edge e : graph.getIncidentEdges(v)) {
            // Пропускаем ТО САМОЕ ребро, по которому пришли в v.
            // Сравниваем по ссылке на объект, а не по вершине-родителю —
            // это корректно обрабатывает кратные рёбра (параллельные рёбра = цикл длины 2).
            if (e == parentEdge.get(v)) continue;

            Vertex to = e.getOther(v);

            if (!visited.contains(to)) {
                parent.put(to, v);
                parentEdge.put(to, e);
                CycleResult result = dfsUndirected(graph, to, visited, onStack, parent, parentEdge);
                if (result != null) return result;
            } else if (onStack.contains(to)) {
                // to — предок v в дереве обхода => обратное ребро => цикл найден
                return buildCycle(v, to, e, parent, parentEdge);
            }
        }

        onStack.remove(v); // выходим из ветви
        return null;
    }

    // ==================== Ориентированный граф ====================

    private static CycleResult detectDirected(Graph graph) {
        Set<Vertex> visited = new HashSet<>();
        Set<Vertex> onStack = new HashSet<>(); // «серые» вершины
        Map<Vertex, Vertex> parent = new HashMap<>();
        Map<Vertex, Edge> parentEdge = new HashMap<>();

        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        for (Vertex start : vertices) {
            if (!visited.contains(start)) {
                CycleResult result = dfsDirected(graph, start, visited, onStack, parent, parentEdge);
                if (result != null) return result;
            }
        }
        return CycleResult.noCycle();
    }

    private static CycleResult dfsDirected(Graph graph, Vertex v,
                                           Set<Vertex> visited, Set<Vertex> onStack,
                                           Map<Vertex, Vertex> parent,
                                           Map<Vertex, Edge> parentEdge) {
        visited.add(v);
        onStack.add(v);

        for (Edge e : graph.getIncidentEdges(v)) {
            Vertex to = e.getTarget(); // для орграфа важнее направление, чем «родитель»

            if (!visited.contains(to)) {
                parent.put(to, v);
                parentEdge.put(to, e);
                CycleResult result = dfsDirected(graph, to, visited, onStack, parent, parentEdge);
                if (result != null) return result;
            } else if (onStack.contains(to)) {
                // обратная дуга в текущую ветвь => ориентированный цикл
                return buildCycle(v, to, e, parent, parentEdge);
            }
            // если to посещена и НЕ в стеке («чёрная») — это поперечная/прямая дуга, цикла нет
        }

        onStack.remove(v);
        return null;
    }

    // ==================== Восстановление цикла ====================

    /**
     * Поднимается от descendant вверх по дереву обхода до ancestor
     * и собирает найденный цикл.
     */
    private static CycleResult buildCycle(Vertex descendant, Vertex ancestor, Edge closingEdge,
                                          Map<Vertex, Vertex> parent,
                                          Map<Vertex, Edge> parentEdge) {
        LinkedList<Vertex> vertices = new LinkedList<>();
        LinkedList<Edge> edges = new LinkedList<>();

        Vertex cur = descendant;
        vertices.addFirst(cur);
        while (!cur.equals(ancestor)) {
            edges.addFirst(parentEdge.get(cur));
            cur = parent.get(cur);
            vertices.addFirst(cur);
        }
        edges.addLast(closingEdge); // замыкающее ребро descendant -> ancestor

        return new CycleResult(true, vertices, edges);
    }
}