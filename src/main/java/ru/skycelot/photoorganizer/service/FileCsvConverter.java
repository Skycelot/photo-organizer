package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.filesystem.FileMetadata;

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
                    fileMetadata.createdOn = Instant.ofEpochMilli(Long.valueOf(field));
                } else if (index == 3) {
                    fileMetadata.modifiedOn = Instant.ofEpochMilli(Long.valueOf(field));
                }
                index++;
            }
            return fileMetadata;
        } else {
            throw new IllegalArgumentException("csv line should have exactly 4 fields, but was " + csvLine);
        }
    }
}
