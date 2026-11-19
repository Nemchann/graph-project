package model;

import java.util.Objects;

/**
 * Вершина графа.
 * Идентификатор (id) является неизменяемым и используется в equals/hashCode,
 * поэтому Vertex можно безопасно использовать как ключ в Map.
 */
public class Vertex {
    private final int id;
    private String label; // опциональная метка для вывода (имя вершины)

    public Vertex(int id) {
        this(id, String.valueOf(id));
    }

    public Vertex(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return label;
    }
}