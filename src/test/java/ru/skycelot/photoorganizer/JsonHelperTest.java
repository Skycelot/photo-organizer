package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.json.*;
import ru.skycelot.photoorganizer.service.JsonHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonHelperTest {

    private JsonHelper converter;

    private final String referenceJson = "{\"couple\":[{\"firstName\":\"Mikhail\",\"age\":21,\"male\":true,\"melon\":null,\"address\":{\"town\":\"Saint-Petersburg\"}},{\"firstName\":\"Elisa\",\"age\":17,\"male\":false,\"melon\":null,\"address\":{\"town\":\"Saint-Petersburg\"}}]}";
    private final JsonObject referenceRoot;

    {
        referenceRoot = new JsonObject();
        JsonObject address = new JsonObject();
        address.put("town", new JsonString("Saint-Petersburg"));
        JsonArray elements = new JsonArray();
        JsonObject element = new JsonObject();
        element.put("firstName", new JsonString("Mikhail"));
        element.put("age", new JsonNumber(21, false));
        element.put("male", JsonBoolean.getInstance(true));
        element.put("melon", JsonNull.getInstance());
        element.put("address", address);
        elements.add(element);
        element = new JsonObject();
        element.put("firstName", new JsonString("Elisa"));
        element.put("age", new JsonNumber(17, false));
        element.put("male", JsonBoolean.getInstance(false));
        element.put("melon", JsonNull.getInstance());
        element.put("address", address);
        elements.add(element);
        referenceRoot.put("couple", elements);
    }

    @BeforeEach
    public void init() {
        converter = new JsonHelper();
    }

    @Test
    public void testMarshalling() {
        String result = converter.stringify(referenceRoot);
        assertEquals(referenceJson, result);
    }

    @Test
    public void testUnmarshalling() {
        JsonNode root = converter.parse(referenceJson);
        assertTrue(root instanceof JsonObject);
        JsonObject rootObject = (JsonObject) root;
        assertEquals(1, rootObject.size());
        JsonElement couple = rootObject.get("couple");
        assertTrue(couple instanceof JsonArray);
        for (JsonElement element : (JsonArray) couple) {
            assertTrue(element instanceof JsonObject);
            JsonObject object = (JsonObject) element;
            assertTrue(object.get("firstName") instanceof JsonString);
            assertTrue(object.get("age") instanceof JsonNumber);
            assertTrue(object.get("male") instanceof JsonBoolean);
            assertTrue(object.get("melon") instanceof JsonNull);
            assertTrue(object.get("address") instanceof JsonObject);
            JsonObject address = (JsonObject) object.get("address");
            assertTrue(address.get("town") instanceof JsonString);
        }
    }
}
