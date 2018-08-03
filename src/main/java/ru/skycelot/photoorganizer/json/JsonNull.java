package ru.skycelot.photoorganizer.json;

public class JsonNull implements JsonValue {
    private static final JsonNull value = new JsonNull();

    private JsonNull() {
    }

    public static JsonNull getInstance() {
        return value;
    }
}
