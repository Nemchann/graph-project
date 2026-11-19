package algorithms;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Алгоритм Дейкстры для нахождения кратчайших путей от одной вершины до всех остальных.
 * Работает только с графами, у которых все веса рёбер >= 0.
 */
public class DijkstraAlgorithm {

    /**
     * Результат работы алгоритма Дейкстры.
     */
    public static class DijkstraResult {
        private final Map<Vertex, Double> distances;  // расстояния от стартовой вершины
        private final Map<Vertex, Vertex> parent;     // дерево кратчайших путей

        public DijkstraResult(Map<Vertex, Double> distances, Map<Vertex, Vertex> parent) {
            this.distances = Collections.unmodifiableMap(distances);
            this.parent = Collections.unmodifiableMap(parent);
        }

        public Map<Vertex, Double> getDistances() { return distances; }
        public Map<Vertex, Vertex> getParent() { return parent; }

        /**
         * Восстановить путь от стартовой вершины до target.
         * Возвращает список вершин в порядке от start до target.
         * Если вершина недостижима, возвращает пустой список.
         */
        public List<Vertex> getPathTo(Vertex target) {
            if (!distances.containsKey(target) || distances.get(target) == Double.POSITIVE_INFINITY) {
                return List.of();
            }
            LinkedList<Vertex> path = new LinkedList<>();
            Vertex cur = target;
            while (cur != null) {
                path.addFirst(cur);
                cur = parent.get(cur);
            }
            return path;
        }
    }

    /**
     * Находит кратчайшие пути от стартовой вершины до всех остальных.
     *
     * @param graph граф (все веса должны быть >= 0)
     * @param start стартовая вершина
     * @return результат с расстояниями и деревом путей
     */
    public static DijkstraResult shortestPaths(Graph graph, Vertex start) {
        // Проверка: все веса должны быть неотрицательными
        for (Edge e : graph.getEdges()) {
            if (e.getWeight() < 0) {
                throw new IllegalArgumentException(
                        "Алгоритм Дейкстры не работает с отрицательными весами. Ребро " +
                                e.getSource() + " — " + e.getTarget() + " имеет вес " + e.getWeight());
            }
        }

        // Инициализация
        Map<Vertex, Double> distances = new LinkedHashMap<>();
        Map<Vertex, Vertex> parent = new LinkedHashMap<>();
        Set<Vertex> visited = new LinkedHashSet<>();

        for (Vertex v : graph.getVertices()) {
            distances.put(v, Double.POSITIVE_INFINITY);
            parent.put(v, null);
        }
        distances.put(start, 0.0);

        // Приоритетная очередь: вершины, отсортированные по текущему расстоянию
        PriorityQueue<Vertex> pq = new PriorityQueue<>(
                Comparator.comparingDouble(distances::get)
        );
        pq.add(start);

        while (!pq.isEmpty()) {
            Vertex u = pq.poll();

            // Если вершину уже обработали — пропускаем
            if (visited.contains(u)) continue;
            visited.add(u);

            // Релаксация рёбер
            for (Edge e : graph.getIncidentEdges(u)) {
                Vertex v = e.getOther(u);
                double newDist = distances.get(u) + e.getWeight();

                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    parent.put(v, u);
                    pq.add(v); // добавляем в очередь с новым приоритетом
                }
            }
        }

        return new DijkstraResult(distances, parent);
    }
}