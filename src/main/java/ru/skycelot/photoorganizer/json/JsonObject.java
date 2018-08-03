package ru.skycelot.photoorganizer.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class JsonObject implements JsonNode, Iterable<Map.Entry<String, JsonElement>> {

    private final Map<String, JsonElement> properties = new LinkedHashMap<>();

    public JsonElement put(String propertyName, JsonElement propertyValue) {
        return properties.put(propertyName, propertyValue);
    }

    public JsonElement get(String propertyName) {
        return properties.get(propertyName);
    }

    public int size() {
        return properties.size();
    }

    @Override
    public Iterator iterator() {
        return new LinkedHashMap<>(properties).entrySet().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonObject that = (JsonObject) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }
}
