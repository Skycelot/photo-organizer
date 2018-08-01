package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.filesystem.FileMetadata;

import javax.xml.bind.DatatypeConverter;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileCsvConverter {

    private final CsvHelper csvHelper;

    public FileCsvConverter(CsvHelper csvHelper) {
        this.csvHelper = csvHelper;
    }

    public String toCsv(FileMetadata fileMetadata) {
        List<String> fields = new ArrayList<>(3);
        fields.add(fileMetadata.path != null ? fileMetadata.path.toString() : "");
        fields.add(Long.toString(fileMetadata.size));
        fields.add(fileMetadata.createdOn != null ? Long.toString(fileMetadata.createdOn.toEpochMilli()) : "");
        fields.add(fileMetadata.modifiedOn != null ? Long.toString(fileMetadata.modifiedOn.toEpochMilli()) : "");
        fields.add(fileMetadata.magicBytes != null ? DatatypeConverter.printHexBinary(fileMetadata.magicBytes): "");
        fields.add(fileMetadata.hash != null ? DatatypeConverter.printHexBinary(fileMetadata.hash): "");
        return csvHelper.encodeFields(fields);
    }

    public FileMetadata fromCsv(String csvLine) {
        List<String> fields = csvHelper.decodeFields(csvLine);
        if (fields.size() == 4) {
            FileMetadata fileMetadata = new FileMetadata();
            int index = 0;
            for (String field: fields) {
                if (index == 0) {
                    fileMetadata.path = Paths.get(field);
                } else if (index == 1) {
                    fileMetadata.size = Long.valueOf(field);
                } else if (index == 2) {
                    fileMetadata.createdOn = field.isEmpty() ? null : Instant.ofEpochMilli(Long.valueOf(field));
                } else if (index == 3) {
                    fileMetadata.modifiedOn = field.isEmpty() ? null : Instant.ofEpochMilli(Long.valueOf(field));
                } else if (index == 4) {
                    fileMetadata.magicBytes = field.isEmpty() ? null : DatatypeConverter.parseHexBinary(field);
                } else if (index == 5) {
                    fileMetadata.hash = field.isEmpty() ? null : DatatypeConverter.parseHexBinary(field);
                }
                index++;
            }
            return fileMetadata;
        } else {
            throw new IllegalArgumentException("csv line should have exactly 4 fields, but was " + csvLine);
        }
    }
}
