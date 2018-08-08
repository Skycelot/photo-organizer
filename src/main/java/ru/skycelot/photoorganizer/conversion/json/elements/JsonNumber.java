package ru.skycelot.photoorganizer.conversion.json.elements;

import java.util.Objects;

public class JsonNumber implements JsonValue {
    private final Number value;
    private final  boolean floatingPoint;

    public JsonNumber(Number value, boolean floatingPoint) {
        this.value = value;
        this.floatingPoint = floatingPoint;
    }

    public Number getValue() {
        return value;
    }

    public boolean isFloatingPoint() {
        return floatingPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonNumber that = (JsonNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
