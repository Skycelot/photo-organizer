package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.json.*;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

public class FileEntityJsonConverter {

    private final JsonHelper jsonHelper;

    public FileEntityJsonConverter(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public String marshall(List<FileEntity> files) {
        JsonObject root = new JsonObject();
        JsonArray filesArray = new JsonArray();
        root.put("files", filesArray);
        files.forEach(file -> filesArray.add(marshallFile(file)));
        return jsonHelper.stringify(root);
    }

    private JsonObject marshallFile(FileEntity file) {
        JsonObject object = new JsonObject();
        object.put("uuid", new JsonString(file.uuid.toString()));
        JsonArray pathArray = new JsonArray();
        file.path.iterator().forEachRemaining(path -> pathArray.add(new JsonString(path.toString())));
        object.put("path", pathArray);
        object.put("extension", file.extension != null ? new JsonString(file.extension) : JsonNull.getInstance());
        object.put("size", new JsonNumber(file.size, false));
        object.put("createdOn", new JsonNumber(file.createdOn.toEpochMilli(), false));
        object.put("modifiedOn", new JsonNumber(file.modifiedOn.toEpochMilli(), false));
        object.put("magicBytes", file.magicBytes != null ? new JsonString(DatatypeConverter.printHexBinary(file.magicBytes)) : JsonNull.getInstance());
        return object;
    }

    public List<FileEntity> unmarshall(String json) {
        return null;
    }
}
