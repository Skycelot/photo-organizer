package ru.skycelot.photoorganizer.json;

import java.util.*;

public class JsonArray implements JsonNode, Iterable<JsonElement> {

    private final List<JsonElement> elements = new LinkedList<>();

    public void add(JsonElement element) {
        elements.add(element);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<JsonElement> iterator() {
        return new ArrayList<>(elements).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonArray that = (JsonArray) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }
}
