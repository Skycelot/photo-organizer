package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.json.*;

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
                            stateStack.add(State.OBJECT);
                            break;
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            if (!(objectStack.getLast() instanceof JsonArray)) {
                                throw new IllegalStateException("Attempt to add an element in non JsonArray");
                            }
                            JsonArray parentArray = (JsonArray) objectStack.getLast();
                            JsonObject object = new JsonObject();
                            parentArray.add(object);
                            objectStack.add(object);
                            stateStack.add(State.OBJECT);
                            break;
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            if (!(objectStack.getLast() instanceof JsonObject)) {
                                throw new IllegalStateException("Attempt to set a property value in non JsonObject");
                            }
                            JsonObject parentObject = (JsonObject) objectStack.getLast();
                            JsonObject innerObject = new JsonObject();
                            parentObject.put(propertyStack.getLast(), innerObject);
                            objectStack.add(innerObject);
                            stateStack.add(State.OBJECT);
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            throw new IllegalArgumentException(ErrorMessages.LITERAL);
                        case PROPERTY_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_PROPERTY);
                        case PROPERTY_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NEXT);
                        case ELEMENT_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                        case ELEMENT_NEXT:
                            if (!(objectStack.getLast() instanceof JsonArray)) {
                                throw new IllegalStateException("Attempt to add an element in non JsonArray");
                            }
                            parentArray = (JsonArray) objectStack.getLast();
                            innerObject = new JsonObject();
                            parentArray.add(innerObject);
                            objectStack.add(innerObject);
                            stateStack.add(State.OBJECT);
                            break;
                    }
                    break;
                case '}':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            objectStack.removeLast();
                            stateStack.removeLast();
                            break;
                        case ARRAY:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_ARRAY);
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_VALUE);
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            if (!(objectStack.getLast() instanceof JsonObject)) {
                                throw new IllegalStateException("Attempt to set a property value in non JsonObject");
                            }
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
                                    object.put(propertyStack.removeLast(), new JsonNumber(fraction ? Double.valueOf(value.toString()) : Long.valueOf(value.toString()), fraction));
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException(ErrorMessages.LITERAL);
                                }
                            }
                            stateStack.removeLast();
                            break;
                        case PROPERTY_VALUE_END:
                            objectStack.removeLast();
                            stateStack.removeLast();
                            break;
                        case PROPERTY_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.COMMA);
                        case ELEMENT_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                        case ELEMENT_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                    }
                    break;
                case '[':
                    switch (stateStack.getLast()) {
                        case START:
                            root = new JsonArray();
                            objectStack.add(root);
                            stateStack.add(State.ARRAY);
                            break;
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            if (!(objectStack.getLast() instanceof JsonArray)) {
                                throw new IllegalStateException("Attempt to add an element in non JsonArray");
                            }
                            JsonArray parentArray = (JsonArray) objectStack.getLast();
                            JsonArray innerArray = new JsonArray();
                            parentArray.add(innerArray);
                            objectStack.add(innerArray);
                            stateStack.add(State.ARRAY);
                            break;
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            if (!(objectStack.getLast() instanceof JsonObject)) {
                                throw new IllegalStateException("Attempt to set a property value in non JsonObject");
                            }
                            JsonObject parentObject = (JsonObject) objectStack.getLast();
                            innerArray = new JsonArray();
                            parentObject.put(propertyStack.getLast(), innerArray);
                            objectStack.add(innerArray);
                            stateStack.add(State.ARRAY);
                            break;
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            throw new IllegalArgumentException(ErrorMessages.LITERAL);
                        case PROPERTY_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_PROPERTY);
                        case PROPERTY_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NEXT);
                        case ELEMENT_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                        case ELEMENT_NEXT:
                            if (!(objectStack.getLast() instanceof JsonArray)) {
                                throw new IllegalStateException("Attempt to add an element in non JsonArray");
                            }
                            parentArray = (JsonArray) objectStack.getLast();
                            innerArray = new JsonArray();
                            parentArray.add(innerArray);
                            objectStack.add(innerArray);
                            stateStack.add(State.ARRAY);
                            break;
                    }
                    break;
                case ']':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            objectStack.removeLast();
                            stateStack.removeLast();
                            break;
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_VALUE);
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            if (!(objectStack.getLast() instanceof JsonArray)) {
                                throw new IllegalStateException("Attempt to add an element in non JsonArray");
                            }
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
                                    array.add(new JsonNumber(fraction ? Double.valueOf(value.toString()) : Long.valueOf(value.toString()), fraction));
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException(ErrorMessages.LITERAL);
                                }
                            }
                            stateStack.removeLast();
                            break;
                        case PROPERTY_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_PROPERTY);
                        case PROPERTY_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NEXT);
                        case ELEMENT_VALUE_END:
                            throw new UnsupportedOperationException();
                        case ELEMENT_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.COMMA);
                    }
                    break;
                case '"':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_NAME);
                            break;
                        case ARRAY:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            break;
                        case PROPERTY_NAME:
                            propertyStack.add(value.toString());
                            value.setLength(0);
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_NAME_END);
                            break;
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            break;
                        case STRING:
                            stateStack.removeLast();
                            JsonString jsonString = new JsonString(value.toString());
                            value.setLength(0);
                            if (objectStack.getLast() instanceof JsonObject) {
                                JsonObject object = (JsonObject) objectStack.getLast();
                                object.put(propertyStack.removeLast(), jsonString);
                                stateStack.add(State.PROPERTY_VALUE_END);
                            } else if (objectStack.getLast() instanceof JsonArray) {
                                JsonArray array = (JsonArray) objectStack.getLast();
                                array.add(jsonString);
                                stateStack.add(State.ELEMENT_VALUE_END);
                            } else {
                                throw new IllegalStateException("Attempt to set a property value or add an element in non JsonObject and JsonArray");
                            }
                            break;
                        case STRING_ESCAPE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            value.append(character);
                            break;
                        case LITERAL:
                            throw new IllegalArgumentException(ErrorMessages.LITERAL);
                        case PROPERTY_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_PROPERTY);
                        case PROPERTY_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.COMMA);
                        case ELEMENT_VALUE_END:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                        case ELEMENT_NEXT:
                            throw new IllegalArgumentException(ErrorMessages.COMMA);
                    }
                    break;
                case ':':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            throw new UnsupportedOperationException();
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_VALUE);
                            break;
                        case PROPERTY_VALUE:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_VALUE);
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            throw new UnsupportedOperationException();
                        case PROPERTY_VALUE_END:
                            throw new UnsupportedOperationException();
                        case PROPERTY_NEXT:
                            throw new UnsupportedOperationException();
                        case ELEMENT_VALUE_END:
                            throw new UnsupportedOperationException();
                        case ELEMENT_NEXT:
                            throw new UnsupportedOperationException();
                    }
                    break;
                case ',':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            throw new UnsupportedOperationException();
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_VALUE);
                        case STRING:
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                        case LITERAL:
                            if (!(objectStack.getLast() instanceof JsonObject)) {
                                throw new IllegalStateException("Attempt to set a property value in non JsonObject");
                            }
                            JsonObject object = (JsonObject) objectStack.getLast();
                            boolean fraction = value.indexOf(".") > 0;
                            object.put(propertyStack.removeLast(), new JsonNumber(fraction ? Double.valueOf(value.toString()) : Long.valueOf(value.toString()), fraction));
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_NEXT);
                            break;
                        case PROPERTY_VALUE_END:
                            stateStack.removeLast();
                            stateStack.add(State.PROPERTY_NEXT);
                            break;
                        case PROPERTY_NEXT:
                            throw new UnsupportedOperationException();
                        case ELEMENT_VALUE_END:
                            stateStack.removeLast();
                            stateStack.add(State.ELEMENT_NEXT);
                        case ELEMENT_NEXT:
                            throw new UnsupportedOperationException();
                    }
                    break;
                case '\\':
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                        case ARRAY:
                            throw new UnsupportedOperationException();
                        case PROPERTY_NAME:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                        case PROPERTY_NAME_END:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                        case PROPERTY_VALUE:
                            throw new IllegalArgumentException(ErrorMessages.PROPERTY_VALUE);
                        case STRING:
                            stateStack.removeLast();
                            stateStack.add(State.STRING_ESCAPE);
                            break;
                        case STRING_ESCAPE:
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            value.append('\\');
                            break;
                        case LITERAL:
                            throw new UnsupportedOperationException();
                        case PROPERTY_VALUE_END:
                            throw new UnsupportedOperationException();
                        case PROPERTY_NEXT:
                            throw new UnsupportedOperationException();
                        case ELEMENT_VALUE_END:
                            throw new UnsupportedOperationException();
                        case ELEMENT_NEXT:
                            throw new UnsupportedOperationException();
                    }
                    break;
                default:
                    switch (stateStack.getLast()) {
                        case START:
                            throw new IllegalArgumentException(ErrorMessages.FIRST_IN_STRING);
                        case OBJECT:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.FIRST_IN_OBJECT);
                            }
                            break;
                        case ARRAY:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.FIRST_IN_ARRAY);
                            }
                            break;
                        case PROPERTY_NAME:
                            if (!Character.isLetter(character)) {
                                throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME);
                            }
                            value.append(character);
                            break;
                        case PROPERTY_NAME_END:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.PROPERTY_NAME_END);
                            }
                            break;
                        case PROPERTY_VALUE:
                            if (!Character.isSpaceChar(character)) {
                                stateStack.removeLast();
                                stateStack.add(State.LITERAL);
                                value.append(character);
                            }
                            break;
                        case STRING:
                            if (Character.getType(character) == Character.LINE_SEPARATOR) {
                                throw new IllegalArgumentException(ErrorMessages.STRING_VALUE);
                            }
                            value.append(character);
                            break;
                        case STRING_ESCAPE:
                            if (character == 'n') {
                                value.append("\n");
                            } else if (character == 'r') {
                                value.append("\r");
                            } else if (character == 't') {
                                value.append("\t");
                            } else {
                                throw new IllegalArgumentException(ErrorMessages.AFTER_ESCAPE);
                            }
                            stateStack.removeLast();
                            stateStack.add(State.STRING);
                            break;
                        case LITERAL:
                            if (!Character.isSpaceChar(character)) {
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
                                        jsonValue = new JsonNumber(fraction ? Double.valueOf(value.toString()) : Long.valueOf(value.toString()), fraction);
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException(ErrorMessages.LITERAL);
                                    }
                                }
                                stateStack.removeLast();
                                if (objectStack.getLast() instanceof JsonObject) {
                                    JsonObject object = (JsonObject) objectStack.getLast();
                                    object.put(propertyStack.removeLast(), jsonValue);
                                    stateStack.add(State.PROPERTY_VALUE_END);
                                } else if (objectStack.getLast() instanceof JsonArray) {
                                    JsonArray array = (JsonArray) objectStack.getLast();
                                    array.add(jsonValue);
                                    stateStack.add(State.ELEMENT_VALUE_END);
                                } else {
                                    throw new IllegalStateException("Attempt to set a property value or add an element in non JsonObject and JsonArray");
                                }
                            }
                            break;
                        case PROPERTY_VALUE_END:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.AFTER_PROPERTY);
                            }
                            break;
                        case PROPERTY_NEXT:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.COMMA);
                            }
                            break;
                        case ELEMENT_VALUE_END:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.AFTER_ELEMENT);
                            }
                            break;
                        case ELEMENT_NEXT:
                            if (!Character.isSpaceChar(character)) {
                                throw new IllegalArgumentException(ErrorMessages.COMMA);
                            }
                            break;
                    }
                    break;
            }
        }
        return root;
    }

    public enum State {
        START, OBJECT, ARRAY, PROPERTY_NAME, PROPERTY_NAME_END, PROPERTY_VALUE, STRING, STRING_ESCAPE, LITERAL, PROPERTY_VALUE_END, PROPERTY_NEXT, ELEMENT_VALUE_END, ELEMENT_NEXT
    }

    public interface ErrorMessages {
        String FIRST_IN_STRING = "First character of json string must be { or [";
        String FIRST_IN_OBJECT = "First character inside object must be double quote";
        String PROPERTY_NAME = "Property can contain only word characters";
        String PROPERTY_NAME_END = "After property name only semicolumn is allowed";
        String PROPERTY_VALUE = "Property value must start with an object opening, array opening, number, literals first or double quotes characters";
        String STRING_VALUE = "String values can't contain line separators characters";
        String AFTER_ESCAPE = "Only slash, double quotes, n, r, t are allowed after escape slash in strings";
        String LITERAL = "Literals must be null, true, false or number";
        String AFTER_PROPERTY = "After property only comma and object closing characters are allowed";
        String PROPERTY_NEXT = "After comma only double quote is allowed";
        String AFTER_ELEMENT = "After element only comma or array closing characters are allowed";
        String FIRST_IN_ARRAY = "First character inside array must be object opening, array opening, double quote, digit or literal first characters";
        String COMMA = "After comma only another property or element are allowed";
    }
}
