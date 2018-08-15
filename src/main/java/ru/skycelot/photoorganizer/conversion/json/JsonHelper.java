package ru.skycelot.photoorganizer.conversion.json;

import ru.skycelot.photoorganizer.conversion.json.elements.*;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

public class JsonHelper {

    public String stringify(JsonNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Node argument can't be null");
        }
        StringBuilder result = new StringBuilder();
        if (node instanceof JsonObject) {
            result.append(stringifyObject((JsonObject) node));
        } else if (node instanceof JsonArray) {
            result.append(stringifyArray((JsonArray) node));
        }
        return result.toString();
    }

    private String stringifyObject(JsonObject object) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        int index = 0;
        for (Map.Entry<String, JsonElement> property : object) {
            result.append('"').append(property.getKey()).append('"').append(':');
            if (property.getValue() instanceof JsonObject) {
                result.append(stringifyObject((JsonObject) property.getValue()));
            } else if (property.getValue() instanceof JsonArray) {
                result.append(stringifyArray((JsonArray) property.getValue()));
            } else {
                result.append(stringifyValue((JsonValue) property.getValue()));
            }
            if (++index < object.size()) {
                result.append(',');
            }
        }
        result.append('}');
        return result.toString();
    }

    private String stringifyArray(JsonArray array) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        int index = 0;
        for (JsonElement element : array) {
            if (element instanceof JsonObject) {
                result.append(stringifyObject((JsonObject) element));
            } else if (element instanceof JsonArray) {
                result.append(stringifyArray((JsonArray) element));
            } else {
                result.append(stringifyValue((JsonValue) element));
            }
            if (++index < array.size()) {
                result.append(',');
            }
        }
        result.append(']');
        return result.toString();
    }

    private String stringifyValue(JsonValue value) {
        String result;
        if (value instanceof JsonString) {
            result = ((JsonString) value).getValue();
            result = result.replace("\\", "\\\\");
            result = result.replace("\"", "\\\"");
            result = result.replace("\n", "\\n");
            result = result.replace("\r", "\\r");
            result = result.replace("\t", "\\t");
            result = "\"" + result + "\"";
        } else if (value instanceof JsonNumber) {
            JsonNumber typedValue = (JsonNumber) value;
            result = typedValue.isFloatingPoint() ? Double.toString(typedValue.getValue().doubleValue()) : Long.toString(typedValue.getValue().longValue());
        } else if (value instanceof JsonBoolean) {
            result = ((JsonBoolean) value).getValue();
        } else if (value instanceof JsonNull) {
            result = "null";
        } else {
            throw new IllegalArgumentException("Only String, Number and Boolean classes are allowed as property values, but was " + value.getClass().getCanonicalName());
        }
        return result;
    }

    public JsonNode parse(String json) {
        json = json.trim();
        JsonNode root = null;
        Deque<JsonNode> objectStack = new LinkedList<>();
        Deque<State> stateStack = new LinkedList<>();
        Deque<String> propertyStack = new LinkedList<>();
        stateStack.add(State.START);
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char character = json.charAt(i);
            switch (character) {
                case '{':
                    switch (stateStack.getLast()) {
                        case START:
                            root = new JsonObject();
                            objectStack.add(root);
                            stateStack.removeLast();
                            stateStack.add(State.NODE);
                            break;
                        case NODE:
                            if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray parentArray = (JsonArray) objectStack.getLast();
                                JsonObject innerObject = new JsonObject();
                                parentArray.add(innerObject);
                                objectStack.add(innerObject);
                                stateStack.add(State.NODE);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case VALUE:
                            JsonObject innerObject = new JsonObject();
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject parentObject = (JsonObject) objectStack.getLast();
                                parentObject.put(propertyStack.getLast(), innerObject);
                            } else if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray parentArray = (JsonArray) objectStack.getLast();
                                parentArray.add(innerObject);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            objectStack.add(innerObject);
                            stateStack.add(State.NODE);
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case VALUE_NEXT:
                            if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray parentArray = (JsonArray) objectStack.getLast();
                                innerObject = new JsonObject();
                                parentArray.add(innerObject);
                                objectStack.add(innerObject);
                                stateStack.add(State.NODE);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case STRING_ESCAPE:
                        case LITERAL:
                        case VALUE_END:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case '}':
                    switch (stateStack.getLast()) {
                        case STRING:
                            value.append(character);
                            break;
                        case LITERAL:
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject object = (JsonObject) objectStack.removeLast();
                                String finalValue = value.toString();
                                value.setLength(0);
                                if (finalValue.equals("null")) {
                                    object.put(propertyStack.removeLast(), JsonNull.getInstance());
                                } else if (finalValue.equals("true")) {
                                    object.put(propertyStack.removeLast(), JsonBoolean.getInstance(true));
                                } else if (finalValue.equals("false")) {
                                    object.put(propertyStack.removeLast(), JsonBoolean.getInstance(false));
                                } else {
                                    try {
                                        BigDecimal number = new BigDecimal(finalValue);
                                        boolean fraction = number.scale() > 0;
                                        object.put(propertyStack.removeLast(), new JsonNumber(fraction ? Double.valueOf(finalValue) : Long.valueOf(finalValue), fraction));
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                                    }
                                }
                                stateStack.removeLast();
                                if (!objectStack.isEmpty()) {
                                    stateStack.removeLast();
                                    stateStack.add(State.VALUE_END);
                                } else {
                                    stateStack.add(State.FINISH);
                                }
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case NODE:
                        case VALUE_END:
                            objectStack.removeLast();
                            stateStack.removeLast();
                            if (!objectStack.isEmpty()) {
                                stateStack.removeLast();
                                stateStack.add(State.VALUE_END);
                            } else {
                                stateStack.add(State.FINISH);
                            }
                            break;
                        case VALUE_NEXT:
                        case START:
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case VALUE:
                        case STRING_ESCAPE:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case '[':
                    switch (stateStack.getLast()) {
                        case START:
                            root = new JsonArray();
                            objectStack.add(root);
                            stateStack.add(State.NODE);
                            break;
                        case NODE:
                        case VALUE_NEXT:
                            if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray parentArray = (JsonArray) objectStack.getLast();
                                JsonArray innerArray = new JsonArray();
                                parentArray.add(innerArray);
                                objectStack.add(innerArray);
                                stateStack.add(State.NODE);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case VALUE:
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject parentObject = (JsonObject) objectStack.getLast();
                                JsonArray innerArray = new JsonArray();
                                parentObject.put(propertyStack.getLast(), innerArray);
                                objectStack.add(innerArray);
                                stateStack.add(State.NODE);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case VALUE_END:
                            if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray parentArray = (JsonArray) objectStack.getLast();
                                JsonArray innerArray = new JsonArray();
                                parentArray.add(innerArray);
                                objectStack.add(innerArray);
                                stateStack.add(State.NODE);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case STRING_ESCAPE:
                        case LITERAL:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case ']':
                    switch (stateStack.getLast()) {
                        case NODE:
                            if (objectStack.getLast() instanceof JsonArray) {
                                objectStack.removeLast();
                                stateStack.removeLast();
                                stateStack.removeLast();
                                if (objectStack.peekLast() == null) {
                                    stateStack.add(State.FINISH);
                                } else {
                                    stateStack.add(State.VALUE_END);
                                }
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case LITERAL:
                            if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray array = (JsonArray) objectStack.removeLast();
                                String finalValue = value.toString();
                                value.setLength(0);
                                if (finalValue.equals("null")) {
                                    array.add(JsonNull.getInstance());
                                } else if (finalValue.equals("true")) {
                                    array.add(JsonBoolean.getInstance(true));
                                } else if (finalValue.equals("false")) {
                                    array.add(JsonBoolean.getInstance(false));
                                } else {
                                    try {
                                        BigDecimal number = new BigDecimal(finalValue);
                                        boolean fraction = number.scale() > 0;
                                        array.add(new JsonNumber(fraction ? Double.valueOf(finalValue) : Long.valueOf(finalValue), fraction));
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                                    }
                                }
                                stateStack.removeLast();
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case VALUE_END:
                            if (objectStack.getLast() instanceof JsonArray) {
                                objectStack.removeLast();
                                stateStack.removeLast();
                                stateStack.removeLast();
                                stateStack.add(State.VALUE_END);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case START:
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case VALUE:
                        case STRING_ESCAPE:
                        case VALUE_NEXT:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case '"':
                    switch (stateStack.getLast()) {
                        case NODE:
                        case VALUE_NEXT:
                            if (objectStack.getLast() instanceof JsonObject) {
                                stateStack.removeLast();
                                stateStack.add(State.PROPERTY_NAME);
                            } else if (objectStack.getLast() instanceof JsonArray) {
                                stateStack.removeLast();
                                stateStack.add(State.STRING);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case PROPERTY_NAME:
                            propertyStack.add(value.toString());
                            value.setLength(0);
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_NAME_END);
                            break;
                        case VALUE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            break;
                        case STRING:
                            stateStack.removeLast();
                            stateStack.add(State.VALUE_END);
                            JsonString jsonString = new JsonString(value.toString());
                            value.setLength(0);
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject object = (JsonObject) objectStack.getLast();
                                object.put(propertyStack.removeLast(), jsonString);
                            } else if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray array = (JsonArray) objectStack.getLast();
                                array.add(jsonString);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case STRING_ESCAPE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            value.append(character);
                            break;
                        case START:
                        case PROPERTY_NAME_END:
                        case LITERAL:
                        case VALUE_END:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case ':':
                    switch (stateStack.getLast()) {
                        case PROPERTY_NAME_END:
                            stateStack.removeLast();
                            stateStack.add(State.VALUE);
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case START:
                        case NODE:
                        case PROPERTY_NAME:
                        case VALUE:
                        case STRING_ESCAPE:
                        case LITERAL:
                        case VALUE_END:
                        case VALUE_NEXT:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case ',':
                    switch (stateStack.getLast()) {
                        case STRING:
                            value.append(character);
                            break;
                        case LITERAL:
                            JsonValue jsonValue;
                            String finalValue = value.toString();
                            value.setLength(0);
                            if (finalValue.equals("null")) {
                                jsonValue = JsonNull.getInstance();
                            } else if (finalValue.equals("true")) {
                                jsonValue = JsonBoolean.getInstance(true);
                            } else if (finalValue.equals("false")) {
                                jsonValue = JsonBoolean.getInstance(false);
                            } else {
                                try {
                                    BigDecimal number = new BigDecimal(finalValue);
                                    boolean fraction = number.scale() > 0;
                                    jsonValue = new JsonNumber(fraction ? number.doubleValue() : number.longValue(), fraction);
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                                }
                            }
                            stateStack.removeLast();
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject object = (JsonObject) objectStack.getLast();
                                object.put(propertyStack.removeLast(), jsonValue);
                                stateStack.add(State.VALUE_NEXT);
                            } else if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray array = (JsonArray) objectStack.getLast();
                                array.add(jsonValue);
                                stateStack.add(State.VALUE_NEXT);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case VALUE_END:
                            stateStack.removeLast();
                            stateStack.add(State.VALUE_NEXT);
                            break;
                        case START:
                        case NODE:
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case VALUE:
                        case STRING_ESCAPE:
                        case VALUE_NEXT:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                case '\\':
                    switch (stateStack.getLast()) {
                        case STRING:
                            stateStack.removeLast();
                            stateStack.add(State.STRING_ESCAPE);
                            break;
                        case STRING_ESCAPE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            value.append('\\');
                            break;
                        case START:
                        case NODE:
                        case PROPERTY_NAME:
                        case PROPERTY_NAME_END:
                        case VALUE:
                        case LITERAL:
                        case VALUE_END:
                        case VALUE_NEXT:
                        case FINISH:
                            throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                    }
                    break;
                default:
                    switch (stateStack.getLast()) {
                        case START:
                        case NODE:
                        case PROPERTY_NAME_END:
                        case VALUE_END:
                        case VALUE_NEXT:
                        case FINISH:
                            if (!Character.isWhitespace(character)) {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case PROPERTY_NAME:
                            if (Character.isLetter(character)) {
                                value.append(character);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case VALUE:
                            if (!Character.isSpaceChar(character)) {
                                stateStack.removeLast();
                                stateStack.add(State.LITERAL);
                                value.append(character);
                            }
                            break;
                        case STRING:
                            if (Character.getType(character) != Character.LINE_SEPARATOR) {
                                value.append(character);
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            break;
                        case STRING_ESCAPE:
                            if (character == 'n') {
                                value.append("\n");
                            } else if (character == 'r') {
                                value.append("\r");
                            } else if (character == 't') {
                                value.append("\t");
                            } else {
                                throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                            }
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            break;
                        case LITERAL:
                            if (!Character.isWhitespace(character)) {
                                value.append(character);
                            } else {
                                JsonValue jsonValue;
                                String finalValue = value.toString();
                                value.setLength(0);
                                if (finalValue.equals("null")) {
                                    jsonValue = JsonNull.getInstance();
                                } else if (finalValue.equals("true")) {
                                    jsonValue = JsonBoolean.getInstance(true);
                                } else if (finalValue.equals("false")) {
                                    jsonValue = JsonBoolean.getInstance(false);
                                } else {
                                    try {
                                        BigDecimal number = new BigDecimal(finalValue);
                                        boolean fraction = number.scale() > 0;
                                        jsonValue = new JsonNumber(fraction ? Double.valueOf(finalValue) : Long.valueOf(finalValue), fraction);
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                                    }
                                }
                                stateStack.removeLast();
                                if (objectStack.getLast() instanceof JsonObject) {
                                    JsonObject object = (JsonObject) objectStack.getLast();
                                    object.put(propertyStack.removeLast(), jsonValue);
                                    stateStack.add(State.VALUE_END);
                                } else if (objectStack.getLast() instanceof JsonArray) {
                                    JsonArray array = (JsonArray) objectStack.getLast();
                                    array.add(jsonValue);
                                    stateStack.add(State.VALUE_END);
                                } else {
                                    throw new IllegalArgumentException(stateStack.getLast() + " -> " + character);
                                }
                            }
                            break;
                    }
                    break;
            }
        }
        return root;
    }

    public enum State {
        START, NODE, PROPERTY_NAME, PROPERTY_NAME_END, VALUE, STRING, STRING_ESCAPE, LITERAL, VALUE_END, VALUE_NEXT, FINISH
    }
}
