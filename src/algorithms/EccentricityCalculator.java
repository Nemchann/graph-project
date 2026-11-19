package algorithms;

import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Вычисление эксцентриситетов вершин, диаметра и радиуса графа.
 *
 * Эти понятия определены только для связных графов.
 * Для несвязного графа метод возвращает результат с пометкой isDisconnected = true.
 *
 * Алгоритм: для каждой вершины запускаем Дейкстру и берём максимум расстояний.
 * Сложность: O(V * (V + E) log V) — для учебных графов (до ~100 вершин) работает нормально.
 */
public class EccentricityCalculator {

    /**
     * Результат вычисления эксцентриситетов.
     */
    public static class EccentricityResult {
        private final Map<Vertex, Double> eccentricities; // вершина -> её эксцентриситет
        private final double diameter;
        private final double radius;
        private final List<Vertex> centers;               // вершины с эксцентриситетом = радиус
        private final boolean disconnected;

        public EccentricityResult(Map<Vertex, Double> eccentricities,
                                  double diameter, double radius,
                                  List<Vertex> centers, boolean disconnected) {
            this.eccentricities = Collections.unmodifiableMap(eccentricities);
            this.diameter = diameter;
            this.radius = radius;
            this.centers = Collections.unmodifiableList(centers);
            this.disconnected = disconnected;
        }

        public Map<Vertex, Double> getEccentricities() { return eccentricities; }
        public double getDiameter() { return diameter; }
        public double getRadius() { return radius; }
        public List<Vertex> getCenters() { return centers; }
        public boolean isDisconnected() { return disconnected; }
    }

    /**
     * Вычисляет эксцентриситеты для всех вершин графа.
     */
    public static EccentricityResult calculate(Graph graph) {
        // Сначала проверяем связность — это быстро (O(V+E))
        GraphTraversal.TraversalResult traversal = GraphTraversal.bfs(graph);
        if (traversal.getComponents() > 1) {
            // Граф несвязный — эксцентриситеты не определены
            return new EccentricityResult(
                    Map.of(),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    List.of(),
                    true
            );
        }

        // Граф связный — считаем эксцентриситеты
        Map<Vertex, Double> eccentricities = new LinkedHashMap<>();
        double diameter = 0.0;
        double radius = Double.POSITIVE_INFINITY;

        for (Vertex v : graph.getVertices()) {
            DijkstraAlgorithm.DijkstraResult dijkstra =
                    DijkstraAlgorithm.shortestPaths(graph, v);

            // Эксцентриситет = максимум расстояний до всех достижимых вершин
            double maxDist = 0.0;
            for (double d : dijkstra.getDistances().values()) {
                if (d != Double.POSITIVE_INFINITY && d > maxDist) {
                    maxDist = d;
                }
            }
            eccentricities.put(v, maxDist);

            if (maxDist > diameter) diameter = maxDist;
            if (maxDist < radius) radius = maxDist;
        }

        // Находим центры — вершины с эксцентриситетом = радиус
        // (сравниваем с небольшой точностью для double)
        List<Vertex> centers = new ArrayList<>();
        for (Map.Entry<Vertex, Double> entry : eccentricities.entrySet()) {
            if (Math.abs(entry.getValue() - radius) < 1e-9) {
                centers.add(entry.getKey());
            }
        }
        centers.sort(Comparator.comparingInt(Vertex::getId));

        return new EccentricityResult(eccentricities, diameter, radius, centers, false);
    }
}