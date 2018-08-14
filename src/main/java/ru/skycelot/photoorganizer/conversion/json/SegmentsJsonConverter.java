package ru.skycelot.photoorganizer.conversion.json;

import ru.skycelot.photoorganizer.conversion.json.elements.JsonArray;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonNumber;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonObject;
import ru.skycelot.photoorganizer.conversion.json.elements.JsonString;
import ru.skycelot.photoorganizer.jpeg.JpegFile;
import ru.skycelot.photoorganizer.jpeg.Segment;

import java.util.List;

public class SegmentsJsonConverter {

    private final JsonHelper jsonHelper;

    public SegmentsJsonConverter(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public String marshall(List<JpegFile> jpegFiles) {
        JsonArray filesArray = new JsonArray();
        for (JpegFile file : jpegFiles) {
            JsonObject fileObject = new JsonObject();
            filesArray.add(fileObject);
            fileObject.put("path", new JsonString(file.path));
            JsonArray segmentsArray = new JsonArray();
            fileObject.put("segments", segmentsArray);
            for (Segment segment : file.segments) {
                JsonObject segmentObject = new JsonObject();
                segmentsArray.add(segmentObject);
                segmentObject.put("offset", new JsonNumber(segment.getOffset(), false));
                segmentObject.put("type", new JsonString(segment.getType().name()));
                segmentObject.put("length", new JsonNumber(segment.getSize(), false));
            }
        }
        return jsonHelper.stringify(filesArray);
    }
}
