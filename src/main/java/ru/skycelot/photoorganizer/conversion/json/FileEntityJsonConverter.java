package ru.skycelot.photoorganizer.conversion.json;

import ru.skycelot.photoorganizer.conversion.json.elements.*;
import ru.skycelot.photoorganizer.domain.Extension;
import ru.skycelot.photoorganizer.domain.FileEntity;

import javax.xml.bind.DatatypeConverter;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
        object.put("extension", file.extension != null ? new JsonString(file.extension.name()) : JsonNull.getInstance());
        object.put("size", new JsonNumber(file.size, false));
        object.put("createdOn", new JsonNumber(file.createdOn.toEpochMilli(), false));
        object.put("modifiedOn", new JsonNumber(file.modifiedOn.toEpochMilli(), false));
        object.put("tiffBlockOffset", file.tiffBlockOffset != null ? new JsonNumber(file.tiffBlockOffset, false) : JsonNull.getInstance());
        object.put("tiffBlockLength", file.tiffBlockLength != null ? new JsonNumber(file.tiffBlockLength, false) : JsonNull.getInstance());
        object.put("exifCamera", file.exifCamera != null ? new JsonString(file.exifCamera.toString()) : JsonNull.getInstance());
        object.put("exifDate", file.exifDate != null ? new JsonNumber(file.exifDate.toEpochMilli(), false) : JsonNull.getInstance());
        object.put("magicNumber", file.magicNumber != null ? new JsonString(DatatypeConverter.printHexBinary(file.magicNumber)) : JsonNull.getInstance());
        object.put("hash", file.hash != null ? new JsonString(DatatypeConverter.printHexBinary(file.hash)) : JsonNull.getInstance());
        return object;
    }

    public List<FileEntity> unmarshall(String json) {
        List<FileEntity> result = new LinkedList<>();
        JsonElement root = jsonHelper.parse(json);
        if (root instanceof JsonObject) {
            JsonElement array = ((JsonObject) root).get("files");
            if (array instanceof JsonArray) {
                for (JsonElement element : (JsonArray) array) {
                    if (element instanceof JsonObject) {
                        result.add(unmarshallFile((JsonObject) element));
                    }
                }
            }
        }
        return result;
    }

    private FileEntity unmarshallFile(JsonObject file) {
        FileEntity result = new FileEntity();

        JsonElement uuid = file.get("uuid");
        if (uuid instanceof JsonString) {
            result.uuid = UUID.fromString(((JsonString) uuid).getValue());
        }
        JsonElement path = file.get("path");
        if (path instanceof JsonArray) {
            String[] pathElements = new String[((JsonArray) path).size()];
            int i = 0;
            for (JsonElement element : (JsonArray) path) {
                pathElements[i] = ((JsonString) element).getValue();
                i++;
            }
            result.path = pathElements.length > 1 ? Paths.get(pathElements[0], Arrays.copyOfRange(pathElements, 1, pathElements.length)) : Paths.get(pathElements[0]);
        }
        JsonElement extension = file.get("extension");
        if (extension instanceof JsonString) {
            result.extension = Extension.valueOf(((JsonString) extension).getValue());
        }
        JsonElement size = file.get("size");
        if (size instanceof JsonNumber) {
            result.size = ((JsonNumber) size).getValue().longValue();
        }
        JsonElement createdOn = file.get("createdOn");
        if (createdOn instanceof JsonNumber) {
            result.createdOn = Instant.ofEpochMilli(((JsonNumber) createdOn).getValue().longValue());
        }
        JsonElement modifiedOn = file.get("modifiedOn");
        if (modifiedOn instanceof JsonNumber) {
            result.modifiedOn = Instant.ofEpochMilli(((JsonNumber) modifiedOn).getValue().longValue());
        }
        JsonElement tiffBlockOffset = file.get("tiffBlockOffset");
        if (tiffBlockOffset instanceof JsonNumber) {
            result.tiffBlockOffset = ((JsonNumber) tiffBlockOffset).getValue().intValue();
        }
        JsonElement tiffBlockLength = file.get("tiffBlockLength");
        if (tiffBlockLength instanceof JsonNumber) {
            result.tiffBlockLength = ((JsonNumber) tiffBlockLength).getValue().intValue();
        }
        JsonElement exifCamera = file.get("exifCamera");
        if (exifCamera instanceof JsonString) {
            result.exifCamera = ((JsonString) exifCamera).getValue();
        }
        JsonElement exifDate = file.get("exifDate");
        if (exifDate instanceof JsonNumber) {
            result.exifDate = Instant.ofEpochMilli(((JsonNumber) exifDate).getValue().longValue());
        }
        JsonElement magicNumber = file.get("magicNumber");
        if (magicNumber instanceof JsonString) {
            result.magicNumber = DatatypeConverter.parseHexBinary(((JsonString) magicNumber).getValue());
        }
        JsonElement hash = file.get("hash");
        if (hash instanceof JsonString) {
            result.hash = DatatypeConverter.parseHexBinary(((JsonString) hash).getValue());
        }
        return result;
    }
}
