package model;

/**
 * Ребро графа. Для неориентированного графа порядок (source, target)
 * условный — метод getOther() позволяет удобно получать "соседнюю" вершину.
 */
public class Edge {
    private final Vertex source;
    private final Vertex target;
    private double weight;

    public Edge(Vertex source, Vertex target) {
        this(source, target, 1.0);
    }

    public Edge(Vertex source, Vertex target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Vertex getSource() { return source; }
    public Vertex getTarget() { return target; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    /** Возвращает вершину, противоположную переданной (удобно для обходов). */
    public Vertex getOther(Vertex v) {
        if (v.equals(source)) return target;
        if (v.equals(target)) return source;
        throw new IllegalArgumentException("Вершина не инцидентна ребру");
    }

    @Override
    public String toString() {
        return "(" + source + " — " + target + ", w=" + weight + ")";
    }
}