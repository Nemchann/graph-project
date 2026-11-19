package algorithms;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Поиск Эйлерова цикла в неориентированном графе.
 *
 * Эйлеров цикл — это цикл, проходящий через каждое ребро графа ровно один раз.
 * Существует тогда и только тогда, когда:
 *   1) граф связный (все вершины с рёбрами в одной компоненте),
 *   2) степени всех вершин чётные.
 *
 * Используется алгоритм Хирхольцера (Hierholzer) — O(E).
 */
public class EulerCycleFinder {

    /**
     * Результат поиска Эйлерова цикла.
     */
    public static class EulerCycleResult {
        private final boolean exists;
        private final List<Vertex> cycle;  // последовательность вершин цикла
        private final String reason;       // если цикл не существует — причина

        public EulerCycleResult(boolean exists, List<Vertex> cycle, String reason) {
            this.exists = exists;
            this.cycle = cycle == null ? List.of() : Collections.unmodifiableList(cycle);
            this.reason = reason;
        }

        public boolean isExists() { return exists; }
        public List<Vertex> getCycle() { return cycle; }
        public String getReason() { return reason; }
    }

    /**
     * Ищет Эйлеров цикл в графе.
     */
    public static EulerCycleResult findEulerCycle(Graph graph) {
        if (graph.isDirected()) {
            return new EulerCycleResult(false, null,
                    "Эйлеров цикл для ориентированного графа не реализован.");
        }

        // Проверка 1: все степени должны быть чётными
        List<Vertex> oddVertices = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            int degree = graph.getIncidentEdges(v).size();
            if (degree % 2 != 0) {
                oddVertices.add(v);
            }
        }
        if (!oddVertices.isEmpty()) {
            return new EulerCycleResult(false, null,
                    "Вершины с нечётной степенью: " + oddVertices.size() +
                            " (первая: " + oddVertices.get(0).getLabel() + "). " +
                            "Эйлеров цикл не существует.");
        }

        // Проверка 2: связность (учитываем только вершины с рёбрами)
        List<Vertex> nonIsolated = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            if (!graph.getIncidentEdges(v).isEmpty()) {
                nonIsolated.add(v);
            }
        }
        if (nonIsolated.isEmpty()) {
            return new EulerCycleResult(false, null, "В графе нет рёбер.");
        }

        // Проверяем связность через BFS
        Set<Vertex> visited = new LinkedHashSet<>();
        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(nonIsolated.get(0));
        visited.add(nonIsolated.get(0));
        while (!queue.isEmpty()) {
            Vertex v = queue.poll();
            for (Edge e : graph.getIncidentEdges(v)) {
                Vertex to = e.getOther(v);
                if (!visited.contains(to)) {
                    visited.add(to);
                    queue.add(to);
                }
            }
        }
        if (visited.size() != nonIsolated.size()) {
            return new EulerCycleResult(false, null,
                    "Граф несвязный (вершины с рёбрами не образуют единую компоненту).");
        }

        // Алгоритм Хирхольцера
        // Создаём копию списков смежности, чтобы не портить оригинальный граф
        Map<Vertex, LinkedList<Edge>> adjCopy = new LinkedHashMap<>();
        for (Vertex v : graph.getVertices()) {
            adjCopy.put(v, new LinkedList<>(graph.getIncidentEdges(v)));
        }

        // Стек для построения цикла
        Deque<Vertex> stack = new ArrayDeque<>();
        List<Vertex> circuit = new ArrayList<>();

        Vertex start = nonIsolated.get(0);
        stack.push(start);

        while (!stack.isEmpty()) {
            Vertex v = stack.peek();
            if (!adjCopy.get(v).isEmpty()) {
                // Берём первое ребро, удаляем его из обоих списков
                Edge e = adjCopy.get(v).removeFirst();
                Vertex to = e.getOther(v);
                adjCopy.get(to).remove(e);
                stack.push(to);
            } else {
                // Из v больше некуда идти — добавляем в результат и возвращаемся назад
                circuit.add(stack.pop());
            }
        }

        // circuit содержит вершины в обратном порядке — разворачиваем
        Collections.reverse(circuit);

        return new EulerCycleResult(true, circuit, null);
    }
}