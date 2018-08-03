package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.filesystem.FileMetadata;
import ru.skycelot.photoorganizer.json.*;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileJsonConverter {
    private final JsonHelper jsonHelper;

    public FileJsonConverter(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public String marshall(List<FileMetadata> files) {
        JsonArray fileArray = new JsonArray();
        for (FileMetadata file : files) {
            fileArray.add(marshallFile(file));
        }
        JsonObject root = new JsonObject();
        root.put("files", fileArray);
        return jsonHelper.stringify(root);
    }

    private JsonObject marshallFile(FileMetadata file) {
        JsonObject jsonObject = new JsonObject();
        JsonArray path = new JsonArray();
        for (int i = 0; i < file.path.getNameCount(); i++) {
            path.add(new JsonString(file.path.getName(i).toString()));
        }
        jsonObject.put("path", path);
        jsonObject.put("size", new JsonNumber(file.size, false));
        jsonObject.put("createdOn", new JsonNumber(file.createdOn.toEpochMilli(), false));
        jsonObject.put("modifiedOn", new JsonNumber(file.modifiedOn.toEpochMilli(), false));
        return jsonObject;
    }

    public List<FileMetadata> unmarshall(String json) {
        List<FileMetadata> files = new LinkedList<>();
        if (json != null && !json.trim().isEmpty()) {
            JsonNode root = jsonHelper.parse(json);
            if (root instanceof JsonObject) {
                JsonElement filesElement = ((JsonObject) root).get("files");
                if (filesElement instanceof JsonArray) {
                    JsonArray fileArray = (JsonArray) filesElement;
                    for (JsonElement fileElement : fileArray) {
                        if (fileElement instanceof JsonObject) {
                            files.add(unmarshallFile((JsonObject) fileElement));
                        } else {
                            throw new IllegalArgumentException("File element must be an object, but was " + fileElement != null ? fileElement.getClass().getCanonicalName() : "null");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Files element value must be an array, but was " + filesElement != null ? filesElement.getClass().getCanonicalName() : "null");
                }
            } else {
                throw new IllegalArgumentException("Root object must be an object, but was " + root != null ? root.getClass().getCanonicalName(): "null");
            }
        } else {
            throw new IllegalArgumentException("Json string can't be null or empty");
        }
        return files;
    }

    private FileMetadata unmarshallFile(JsonObject jsonObject) {
        FileMetadata file = new FileMetadata();
        JsonElement pathElement = jsonObject.get("path");
        if (pathElement instanceof JsonArray) {
            JsonArray path = (JsonArray) pathElement;
            String[] pathNames = new String[path.size()];
            int i = 0;
            for (JsonElement pathNameElement : path) {
                if (pathNameElement instanceof JsonString) {
                    JsonString pathNameValue = (JsonString) pathNameElement;
                    pathNames[i++] = pathNameValue.getValue();
                } else {
                    throw new IllegalArgumentException();
                }
            }
            file.path = Paths.get(pathNames[0], Arrays.copyOfRange(pathNames, 1, pathNames.length));
        } else {
            throw new IllegalArgumentException("Path value must be an array");
        }

        JsonElement sizeElement = jsonObject.get("size");
        if (sizeElement instanceof JsonNumber) {
            JsonNumber size = (JsonNumber) sizeElement;
            file.size = size.getValue().intValue();
        } else {
            throw new IllegalArgumentException("Size value must be a number");
        }

        JsonElement createdOnElement = jsonObject.get("createdOn");
        if (createdOnElement instanceof JsonNumber) {
            JsonNumber createdOn = (JsonNumber) createdOnElement;
            file.createdOn = Instant.ofEpochMilli(createdOn.getValue().longValue());
        } else {
            throw new IllegalArgumentException("CreatedOn value must be a number");
        }

        JsonElement modifiedOnElement = jsonObject.get("modifiedOn");
        if (modifiedOnElement instanceof JsonNumber) {
            JsonNumber modifiedOn = (JsonNumber) modifiedOnElement;
            file.modifiedOn = Instant.ofEpochMilli(modifiedOn.getValue().longValue());
        } else {
            throw new IllegalArgumentException("ModifiedOn value must be a number");
        }

        return file;
    }
}
