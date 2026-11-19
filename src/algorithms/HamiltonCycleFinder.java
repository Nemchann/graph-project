package algorithms;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Поиск Гамильтонова цикла в неориентированном графе.
 *
 * Гамильтонов цикл — это цикл, проходящий через каждую вершину графа ровно один раз.
 * Задача NP-полная, поэтому используется backtracking с отсечениями.
 *
 * Необходимые условия (проверяются перед запуском backtracking):
 *   1) граф связный,
 *   2) |V| >= 3,
 *   3) степень каждой вершины >= 2,
 *   4) |E| >= |V|.
 */
public class HamiltonCycleFinder {

    /**
     * Результат поиска Гамильтонова цикла.
     */
    public static class HamiltonCycleResult {
        private final boolean exists;
        private final List<Vertex> cycle;  // последовательность вершин цикла
        private final String reason;       // причина, если цикл не существует

        public HamiltonCycleResult(boolean exists, List<Vertex> cycle, String reason) {
            this.exists = exists;
            this.cycle = cycle == null ? List.of() : Collections.unmodifiableList(cycle);
            this.reason = reason;
        }

        public boolean isExists() { return exists; }
        public List<Vertex> getCycle() { return cycle; }
        public String getReason() { return reason; }
    }

    /**
     * Ищет Гамильтонов цикл в графе.
     */
    public static HamiltonCycleResult findHamiltonCycle(Graph graph) {
        if (graph.isDirected()) {
            return new HamiltonCycleResult(false, null,
                    "Гамильтонов цикл для ориентированного графа не реализован.");
        }

        int n = graph.vertexCount();

        // Условие 1: |V| >= 3
        if (n < 3) {
            return new HamiltonCycleResult(false, null,
                    "В графе менее 3 вершин — Гамильтонов цикл невозможен.");
        }

        // Условие 2: связность
        GraphTraversal.TraversalResult traversal = GraphTraversal.bfs(graph);
        if (traversal.getComponents() > 1) {
            return new HamiltonCycleResult(false, null,
                    "Граф несвязный (компонент: " + traversal.getComponents() + ").");
        }

        // Условие 3: степень каждой вершины >= 2
        List<Vertex> lowDegree = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            if (graph.getIncidentEdges(v).size() < 2) {
                lowDegree.add(v);
            }
        }
        if (!lowDegree.isEmpty()) {
            return new HamiltonCycleResult(false, null,
                    "Вершины со степенью < 2: " + lowDegree.size() +
                            " (первая: " + lowDegree.get(0).getLabel() + ").");
        }

        // Условие 4: |E| >= |V|
        if (graph.edgeCount() < n) {
            return new HamiltonCycleResult(false, null,
                    "Число рёбер (" + graph.edgeCount() + ") меньше числа вершин (" + n + ").");
        }

        // Backtracking
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(Vertex::getId));

        // Стартовая вершина — с максимальной степенью (эвристика: уменьшает ветвление)
        Vertex start = vertices.stream()
                .max(Comparator.comparingInt(v -> graph.getIncidentEdges(v).size()))
                .orElse(vertices.get(0));

        Set<Vertex> visited = new LinkedHashSet<>();
        List<Vertex> path = new ArrayList<>();
        path.add(start);
        visited.add(start);

        boolean found = backtrack(graph, start, start, visited, path, n);

        if (found) {
            return new HamiltonCycleResult(true, path, null);
        } else {
            return new HamiltonCycleResult(false, null,
                    "Гамильтонов цикл не найден (граф негамильтонов).");
        }
    }

    /**
     * Рекурсивный backtracking.
     *
     * @param graph    граф
     * @param start    стартовая вершина (в неё надо вернуться в конце)
     * @param current  текущая вершина
     * @param visited  уже посещённые вершины
     * @param path     текущий путь
     * @param n        общее число вершин
     * @return true, если цикл найден
     */
    /**
     * Рекурсивный backtracking.
     */
    private static boolean backtrack(Graph graph, Vertex start, Vertex current,
                                     Set<Vertex> visited, List<Vertex> path, int n) {
        // База: все вершины посещены — проверяем, можно ли вернуться в start
        if (visited.size() == n) {
            return graph.containsEdge(current, start);
        }

        // Перебираем непосещённых соседей
        for (Vertex neighbor : graph.getNeighbors(current)) {
            if (visited.contains(neighbor)) continue;
            if (neighbor.equals(start)) continue; // в start возвращаемся только в конце

            visited.add(neighbor);
            path.add(neighbor);

            if (backtrack(graph, start, neighbor, visited, path, n)) {
                return true;
            }

            // Откат
            visited.remove(neighbor);
            path.remove(path.size() - 1);
        }

        return false;
    }
}