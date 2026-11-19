package algorithms;

import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Раскраска графа (жадный алгоритм Welsh-Powell).
 *
 * Идея:
 *   1) Сортируем вершины по убыванию степени.
 *   2) Проходим по вершинам в этом порядке.
 *   3) Каждой вершине присваиваем минимальный цвет,
 *      который не используется ни одним из её уже раскрашенных соседей.
 *
 * Результат — допустимая раскраска (смежные вершины разного цвета).
 * Число цветов не обязательно минимально (хроматическое число),
 * но для учебных графов обычно близко к нему.
 */
public class GraphColoring {

    /**
     * Результат раскраски.
     */
    public static class ColoringResult {
        private final Map<Vertex, Integer> colors;    // вершина -> её цвет (1, 2, 3, ...)
        private final int chromaticNumber;            // число использованных цветов
        private final Map<Integer, List<Vertex>> groups; // цвет -> список вершин этого цвета

        public ColoringResult(Map<Vertex, Integer> colors, int chromaticNumber,
                              Map<Integer, List<Vertex>> groups) {
            this.colors = Collections.unmodifiableMap(colors);
            this.chromaticNumber = chromaticNumber;
            this.groups = Collections.unmodifiableMap(groups);
        }

        public Map<Vertex, Integer> getColors() { return colors; }
        public int getChromaticNumber() { return chromaticNumber; }
        public Map<Integer, List<Vertex>> getGroups() { return groups; }
    }

    /**
     * Раскрашивает граф жадным алгоритмом Welsh-Powell.
     */
    public static ColoringResult color(Graph graph) {
        // 1. Сортируем вершины по убыванию степени
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort((a, b) -> {
            int degA = graph.getIncidentEdges(a).size();
            int degB = graph.getIncidentEdges(b).size();
            int cmp = Integer.compare(degB, degA); // по убыванию
            if (cmp != 0) return cmp;
            return Integer.compare(a.getId(), b.getId()); // при равенстве — по id
        });

        // 2. Жадная раскраска
        Map<Vertex, Integer> colors = new LinkedHashMap<>();
        int maxColor = 0;

        for (Vertex v : vertices) {
            // Собираем цвета уже раскрашенных соседей
            Set<Integer> forbidden = new HashSet<>();
            for (Vertex neighbor : graph.getNeighbors(v)) {
                Integer c = colors.get(neighbor);
                if (c != null) {
                    forbidden.add(c);
                }
            }

            // Находим минимальный цвет, не входящий в forbidden
            int color = 1;
            while (forbidden.contains(color)) {
                color++;
            }

            colors.put(v, color);
            if (color > maxColor) maxColor = color;
        }

        // 3. Группируем вершины по цветам
        Map<Integer, List<Vertex>> groups = new TreeMap<>();
        for (Map.Entry<Vertex, Integer> entry : colors.entrySet()) {
            groups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }
        // Сортируем вершины внутри каждой группы по id
        for (List<Vertex> list : groups.values()) {
            list.sort(Comparator.comparingInt(Vertex::getId));
        }

        return new ColoringResult(colors, maxColor, groups);
    }

    /**
     * Проверяет корректность раскраски: все ли рёбра соединяют вершины разного цвета.
     */
    public static boolean isValid(Graph graph, ColoringResult result) {
        for (model.Edge e : graph.getEdges()) {
            int c1 = result.getColors().get(e.getSource());
            int c2 = result.getColors().get(e.getTarget());
            if (c1 == c2) return false;
        }
        return true;
    }
}