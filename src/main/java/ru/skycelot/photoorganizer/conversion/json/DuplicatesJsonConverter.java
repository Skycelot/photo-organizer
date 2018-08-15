package ru.skycelot.photoorganizer.conversion.json;

import ru.skycelot.photoorganizer.conversion.json.elements.JsonArray;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonElement;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonObject;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonString;
import ru.skycelot.photoorganizer.domain.Duplicates;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DuplicatesJsonConverter {

    private final JsonHelper jsonHelper;

    public DuplicatesJsonConverter(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public String marshall(List<Duplicates> duplicates) {
        JsonObject root = new JsonObject();
        JsonArray duplicatesArray = new JsonArray();
        root.put("duplicates", duplicatesArray);
        duplicates.forEach(ids -> {
            JsonObject duplicatesObject = new JsonObject();
            JsonArray duplicatesUuids = new JsonArray();
            ids.filesIds.stream().forEach(uuid -> duplicatesUuids.add(new JsonString(uuid.toString())));
            duplicatesObject.put("uuids", duplicatesUuids);
            duplicatesArray.add(duplicatesObject);
        });
        return jsonHelper.stringify(root);
    }

    public List<Duplicates> unmarshall(String json) {
        List<Duplicates> result = new LinkedList<>();
        JsonElement rootElement = jsonHelper.parse(json);
        if (rootElement instanceof JsonObject) {
            JsonObject root = (JsonObject) rootElement;
            JsonElement duplicatesElement = root.get("duplicates");
            if (duplicatesElement instanceof JsonArray) {
                JsonArray duplicates = (JsonArray) duplicatesElement;
                for (JsonElement duplicateElement: duplicates) {
                    if (duplicateElement instanceof JsonObject) {
                        JsonObject duplicate = (JsonObject) duplicateElement;
                        JsonElement uuidsElement = duplicate.get("uuids");
                        if (uuidsElement instanceof JsonArray) {
                            JsonArray uuids = (JsonArray) uuidsElement;
                            List<UUID> uuidList = new ArrayList<>(uuids.size());
                            result.add(new Duplicates(uuidList));
                            for (JsonElement uuidElement: uuids) {
                                if (uuidElement instanceof JsonString) {
                                    JsonString uuid = (JsonString) uuidElement;
                                    uuidList.add(UUID.fromString(uuid.getValue()));
                                } else {
                                    throw new IllegalArgumentException("Uuids must be string values");
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Duplicate object must contain uuids array property");
                        }
                    } else {
                        throw new IllegalArgumentException("Duplicates array must contain only objects");
                    }
                }
            } else {
                throw new IllegalArgumentException("Root object must have duplicates array property");
            }
        } else {
            throw new IllegalArgumentException("Root element must be object");
        }
        return result;
    }
}
