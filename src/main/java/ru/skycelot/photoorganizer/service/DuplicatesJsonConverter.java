package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.domain.Duplicates;
import ru.skycelot.photoorganizer.json.JsonArray;
import ru.skycelot.photoorganizer.json.JsonObject;
import ru.skycelot.photoorganizer.json.JsonString;

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
        return jsonHelper.stringify(duplicatesArray);
    }

    public List<Duplicates> unmarshall(String json) {
        return null;
    }
}
