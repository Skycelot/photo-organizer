package ru.skycelot.photoorganizer.conversion.json;

import ru.skycelot.photoorganizer.domain.Duplicates;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonArray;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonObject;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonString;

import java.util.List;

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
        return null;
    }
}