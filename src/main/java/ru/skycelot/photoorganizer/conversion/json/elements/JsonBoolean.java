package ru.skycelot.photoorganizer.conversion.json.elements;

public enum JsonBoolean implements JsonValue {
    TRUE, FALSE;

    public static JsonBoolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    public String getValue() {
        return name().toLowerCase();
    }
}
