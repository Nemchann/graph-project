import algorithms.*;
import io.GraphFileLoader;
import io.GraphPrinter;
import model.Edge;
import model.Graph;
import model.Vertex;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// tests/resources/dijkstra_test.txt
// tests/resources/ecc_test.txt
// src/test.txt

// Для №13
// tests/resources/no_cycle.txt
// tests/resources/cycle3.txt
// tests/resources/cycle_component.txt

// Орграфы
// tests/resources/dag.txt
// tests/resources/dir_cycle.txt
// 1 - 2 - 1
// tests/resources/multi.txt

// Эйлеров цикл
// tests/resources/euler_test.txt
// tests/resources/euler_no.txt


// Гамильтонов цикл
// tests/resources/hamilton_yes.txt
// tests/resources/hamilton_no.txt
// tests/resources/hamilton_tricky.txt

/**
 * Точка входа. Консольное меню для работы с графом.
 */
public class Main {

    private static Graph currentGraph = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Рабочая директория: " + System.getProperty("user.dir"));
        System.out.println("=== Проект по теории графов ===");
        System.out.println("Индивидуальное задание №13: проверка наличия цикла в графе\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Ваш выбор: ").trim();

            switch (choice) {
                case "1" -> loadGraph();
                case "2" -> printGraph();
                case "3" -> runDFS();
                case "4" -> runBFS();
                case "5" -> runDijkstra();
                case "6" -> runEccentricities();
                case "7" -> runEulerCycle();
                case "8" -> runHamiltonCycle();
                case "9" -> runColoring();
                case "10" -> runIndividualTask();
                case "0" -> {
                    running = false;
                    System.out.println("Выход.");
                }
                default -> System.out.println("Неизвестный пункт меню.\n");
            }
        }
    }

    private static void printMenu() {
        System.out.println("---------- МЕНЮ ----------");
        System.out.println(" 1. Загрузить граф из файла");
        System.out.println(" 2. Вывести граф (матрица / списки смежности)");
        System.out.println(" 3. Обход в глубину (DFS)");
        System.out.println(" 4. Обход в ширину (BFS)");
        System.out.println(" 5. Алгоритм Дейкстры");
        System.out.println(" 6. Эксцентриситеты, диаметр, радиус");
        System.out.println(" 7. Эйлеров цикл");
        System.out.println(" 8. Гамильтонов цикл");
        System.out.println(" 9. Раскраска графа");
        System.out.println("10. Индивидуальное задание №13 (проверка цикла)");
        System.out.println(" 0. Выход");
        System.out.println("--------------------------");
    }

    // ---------- Вспомогательные методы ----------

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static boolean requireGraph() {
        if (currentGraph == null) {
            System.out.println("Сначала загрузите граф (пункт 1).\n");
            return false;
        }
        return true;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Ожидалось целое число. Попробуйте ещё раз.");
            }
        }
    }

    // ---------- Пункты меню ----------

    private static void loadGraph() {
        String pathStr = readLine("Путь к файлу: ").trim();
        if (pathStr.isEmpty()) {
            System.out.println("Путь не указан.\n");
            return;
        }
        System.out.print("Граф ориентированный? (y/n, по умолчанию n): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        boolean directed = ans.equals("y") || ans.equals("yes") || ans.equals("д") || ans.equals("да");

        try {
            Path path = Paths.get(pathStr);
            currentGraph = GraphFileLoader.load(path, directed);
            System.out.println("Граф успешно загружен: |V|=" + currentGraph.vertexCount()
                    + ", |E|=" + currentGraph.edgeCount() + ".\n");
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage() + "\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка формата файла: " + e.getMessage() + "\n");
        }
    }

    private static void printGraph() {
        if (!requireGraph()) return;
        System.out.println("Формат вывода: 1 — матрица смежности, 2 — списки смежности.");
        int choice = readInt("Ваш выбор: ");
        System.out.println();
        if (choice == 1) {
            GraphPrinter.printAdjacencyMatrix(currentGraph, System.out);
        } else if (choice == 2) {
            GraphPrinter.printAdjacencyLists(currentGraph, System.out);
        } else {
            System.out.println("Неверный вариант.\n");
        }
    }

    // ---------- Заглушки для алгоритмов (подключим по мере реализации) ----------

    private static void runDFS() {
        if (!requireGraph()) return;
        System.out.println("1 — обход всего графа (все компоненты)");
        System.out.println("2 — обход из конкретной вершины");
        int mode = readInt("Ваш выбор: ");

        GraphTraversal.TraversalResult result;
        if (mode == 2) {
            int id = readInt("Номер стартовой вершины: ");
            Vertex start = GraphTraversal.findVertexById(currentGraph, id);
            if (start == null) {
                System.out.println("Вершина с id=" + id + " не найдена.\n");
                return;
            }
            result = GraphTraversal.dfs(currentGraph, start);
        } else {
            result = GraphTraversal.dfs(currentGraph);
        }

        System.out.println("\n[DFS] Порядок обхода:");
        for (Vertex v : result.getOrder()) {
            System.out.print(v.getLabel() + " ");
        }
        System.out.println("\n\nКомпонент связности: " + result.getComponents());

        System.out.println("\nДерево обхода (рёбра):");
        if (result.getTreeEdges().isEmpty()) {
            System.out.println("  (нет рёбер)");
        } else {
            for (Edge e : result.getTreeEdges()) {
                System.out.println("  " + e.getSource().getLabel() + " — " + e.getTarget().getLabel());
            }
        }
        System.out.println();
    }

    private static void runBFS() {
        if (!requireGraph()) return;
        System.out.println("1 — обход всего графа (все компоненты)");
        System.out.println("2 — обход из конкретной вершины");
        int mode = readInt("Ваш выбор: ");

        GraphTraversal.TraversalResult result;
        if (mode == 2) {
            int id = readInt("Номер стартовой вершины: ");
            Vertex start = GraphTraversal.findVertexById(currentGraph, id);
            if (start == null) {
                System.out.println("Вершина с id=" + id + " не найдена.\n");
                return;
            }
            result = GraphTraversal.bfs(currentGraph, start);
        } else {
            result = GraphTraversal.bfs(currentGraph);
        }

        System.out.println("\n[BFS] Порядок обхода:");
        for (Vertex v : result.getOrder()) {
            System.out.print(v.getLabel() + " ");
        }
        System.out.println("\n\nКомпонент связности: " + result.getComponents());

        System.out.println("\nДерево обхода (рёбра):");
        if (result.getTreeEdges().isEmpty()) {
            System.out.println("  (нет рёбер)");
        } else {
            for (Edge e : result.getTreeEdges()) {
                System.out.println("  " + e.getSource().getLabel() + " — " + e.getTarget().getLabel());
            }
        }
        System.out.println();
    }

    private static void runDijkstra() {
        if (!requireGraph()) return;

        // Проверка: граф должен быть взвешенным (хотя бы одно ребро с весом != 1)
        boolean hasWeights = false;
        for (model.Edge e : currentGraph.getEdges()) {
            if (e.getWeight() != 1.0) {
                hasWeights = true;
                break;
            }
        }
        if (!hasWeights) {
            System.out.println("Внимание: все рёбра имеют вес 1. Дейкстра работает, но результат будет таким же, как у BFS.\n");
        }

        int id = readInt("Номер стартовой вершины: ");
        Vertex start = GraphTraversal.findVertexById(currentGraph, id);
        if (start == null) {
            System.out.println("Вершина с id=" + id + " не найдена.\n");
            return;
        }

        try {
            DijkstraAlgorithm.DijkstraResult result = DijkstraAlgorithm.shortestPaths(currentGraph, start);

            System.out.println("\n[Дейкстра] Кратчайшие пути от вершины " + start.getLabel() + ":");
            System.out.println("---------------------------------------------------");
            System.out.printf("%-10s | %-15s | %s%n", "Вершина", "Расстояние", "Путь");
            System.out.println("---------------------------------------------------");

            // Сортируем вершины по id для стабильного вывода
            List<Vertex> vertices = new ArrayList<>(currentGraph.getVertices());
            vertices.sort(Comparator.comparingInt(Vertex::getId));

            for (Vertex v : vertices) {
                double dist = result.getDistances().get(v);
                String distStr = (dist == Double.POSITIVE_INFINITY) ? "∞" : String.format("%.2f", dist);
                String pathStr = "";

                if (dist != Double.POSITIVE_INFINITY) {
                    List<Vertex> path = result.getPathTo(v);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < path.size(); i++) {
                        sb.append(path.get(i).getLabel());
                        if (i < path.size() - 1) sb.append(" → ");
                    }
                    pathStr = sb.toString();
                } else {
                    pathStr = "(недостижима)";
                }

                System.out.printf("%-10s | %-15s | %s%n", v.getLabel(), distStr, pathStr);
            }
            System.out.println("---------------------------------------------------\n");

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private static void runEccentricities() {
        if (!requireGraph()) return;

        if (currentGraph.isDirected()) {
            System.out.println("Внимание: для ориентированного графа понятия диаметра/радиуса " +
                    "обычно определяются иначе. Результат может быть некорректным.\n");
        }

        try {
            EccentricityCalculator.EccentricityResult result =
                    EccentricityCalculator.calculate(currentGraph);

            if (result.isDisconnected()) {
                System.out.println("\nГраф несвязный (количество компонент: " +
                        GraphTraversal.bfs(currentGraph).getComponents() + ").");
                System.out.println("Эксцентриситеты, диаметр и радиус для несвязного графа не определены.\n");
                return;
            }

            System.out.println("\n[Эксцентриситеты вершин]:");
            System.out.println("----------------------------------");
            System.out.printf("%-15s | %s%n", "Вершина", "Эксцентриситет");
            System.out.println("----------------------------------");

            // Сортируем по id для стабильного вывода
            List<Vertex> vertices = new ArrayList<>(currentGraph.getVertices());
            vertices.sort(Comparator.comparingInt(Vertex::getId));

            for (Vertex v : vertices) {
                double ecc = result.getEccentricities().get(v);
                System.out.printf("%-15s | %.2f%n", v.getLabel(), ecc);
            }
            System.out.println("----------------------------------");

            System.out.printf("%nДиаметр графа (D) = %.2f%n", result.getDiameter());
            System.out.printf("Радиус  графа (R) = %.2f%n", result.getRadius());

            System.out.print("Центр   графа: ");
            if (result.getCenters().isEmpty()) {
                System.out.println("(нет)");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < result.getCenters().size(); i++) {
                    sb.append(result.getCenters().get(i).getLabel());
                    if (i < result.getCenters().size() - 1) sb.append(", ");
                }
                System.out.println(sb);
            }
            System.out.println();

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private static void runEulerCycle() {
        if (!requireGraph()) return;

        if (currentGraph.isDirected()) {
            System.out.println("Эйлеров цикл для ориентированного графа не реализован.\n");
            return;
        }

        EulerCycleFinder.EulerCycleResult result = EulerCycleFinder.findEulerCycle(currentGraph);

        if (!result.isExists()) {
            System.out.println("\n[Эйлеров цикл] НЕ СУЩЕСТВУЕТ.");
            System.out.println("Причина: " + result.getReason() + "\n");
            return;
        }

        System.out.println("\n[Эйлеров цикл] СУЩЕСТВУЕТ.");
        System.out.println("Последовательность вершин (" + result.getCycle().size() + "):");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.getCycle().size(); i++) {
            sb.append(result.getCycle().get(i).getLabel());
            if (i < result.getCycle().size() - 1) sb.append(" → ");
        }
        System.out.println(sb);
        System.out.println("(Первая и последняя вершины совпадают — это цикл.)\n");
    }

    private static void runHamiltonCycle() {
        if (!requireGraph()) return;

        if (currentGraph.isDirected()) {
            System.out.println("Гамильтонов цикл для ориентированного графа не реализован.\n");
            return;
        }

        HamiltonCycleFinder.HamiltonCycleResult result =
                HamiltonCycleFinder.findHamiltonCycle(currentGraph);

        if (!result.isExists()) {
            System.out.println("\n[Гамильтонов цикл] НЕ СУЩЕСТВУЕТ.");
            System.out.println("Причина: " + result.getReason() + "\n");
            return;
        }

        System.out.println("\n[Гамильтонов цикл] СУЩЕСТВУЕТ.");
        System.out.println("Последовательность вершин (" + result.getCycle().size() + "):");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.getCycle().size(); i++) {
            sb.append(result.getCycle().get(i).getLabel());
            if (i < result.getCycle().size() - 1) sb.append(" → ");
        }
        System.out.println(sb);
        System.out.println("(Первая и последняя вершины совпадают — это цикл.)\n");
    }

    private static void runColoring() {
        if (!requireGraph()) return;

        if (currentGraph.isDirected()) {
            System.out.println("Раскраска для ориентированного графа обычно определяется " +
                    "для базового неориентированного графа. Результат может быть некорректным.\n");
        }

        GraphColoring.ColoringResult result = GraphColoring.color(currentGraph);

        System.out.println("\n[Раскраска графа]");
        System.out.println("Использовано цветов (хроматическое число): " +
                result.getChromaticNumber());
        System.out.println("----------------------------------");

        // Группы по цветам
        for (Map.Entry<Integer, List<Vertex>> entry : result.getGroups().entrySet()) {
            System.out.print("Цвет " + entry.getKey() + ": ");
            StringBuilder sb = new StringBuilder();
            List<Vertex> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).getLabel());
                if (i < list.size() - 1) sb.append(", ");
            }
            System.out.println(sb);
        }

        System.out.println("----------------------------------");
        boolean valid = GraphColoring.isValid(currentGraph, result);
        System.out.println("Проверка корректности: " + (valid ? "ОК (все рёбра соединяют вершины разного цвета)" : "ОШИБКА"));
        System.out.println();
    }

    private static void runIndividualTask() {
        if (!requireGraph()) return;

        System.out.println("\n=== Индивидуальное задание №13 ===");
        System.out.println("Проверка: содержит ли граф хотя бы один цикл\n");

        CycleDetector.CycleResult result = CycleDetector.detect(currentGraph);

        if (!result.hasCycle()) {
            System.out.println("Ответ: граф НЕ содержит циклов.");
            System.out.println(currentGraph.isDirected()
                    ? "Граф является ациклическим (DAG, если он связный)."
                    : "Граф является лесом (деревом, если он связный).");
        } else {
            System.out.println("Ответ: граф СОДЕРЖИТ цикл.");
            System.out.println("Длина найденного цикла: " + result.getCycleLength());

            System.out.print("Цикл (вершины): ");
            List<Vertex> cycle = result.getCycleVertices();
            for (int i = 0; i < cycle.size(); i++) {
                System.out.print(cycle.get(i).getLabel());
                if (i < cycle.size() - 1) System.out.print(" → ");
            }
            // замыкаем вывод обратно на первую вершину
            System.out.println(" → " + cycle.get(0).getLabel());

            System.out.println("Рёбра цикла:");
            for (Edge e : result.getCycleEdges()) {
                String sep = currentGraph.isDirected() ? " → " : " — ";
                System.out.println("  " + e.getSource().getLabel() + sep + e.getTarget().getLabel());
            }
        }

        // Дополнительная перекрёстная проверка для неориентированного графа
        if (!currentGraph.isDirected()) {
            boolean quick = CycleDetector.hasCycleQuick(currentGraph);
            int components = GraphTraversal.bfs(currentGraph).getComponents();
            System.out.println("\nПерекрёстная проверка (|E| > |V| - C): "
                    + graph_stats(components) + " => " + (quick ? "цикл есть" : "циклов нет"));
            if (quick != result.hasCycle()) {
                System.out.println("ВНИМАНИЕ: результаты методов расходятся — есть баг!");
            }
        }
        System.out.println();
    }

    /** Вспомогательный метод для наглядного вывода формулы. */
    private static String graph_stats(int components) {
        return "|E|=" + currentGraph.edgeCount()
                + ", |V|=" + currentGraph.vertexCount()
                + ", C=" + components
                + " (|V| - C = " + (currentGraph.vertexCount() - components) + ")";
    }
}