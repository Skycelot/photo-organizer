package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.filesystem.File;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileCsvConverter {

    private final CsvHelper csvHelper;

    public FileCsvConverter(CsvHelper csvHelper) {
        this.csvHelper = csvHelper;
    }

    public String toCsv(File file) {
        List<String> fields = new ArrayList<>(3);
        fields.add(file.path != null ? file.path.toString() : "");
        fields.add(Long.toString(file.size));
        fields.add(file.createdOn != null ? Long.toString(file.createdOn.toEpochMilli()) : "");
        fields.add(file.modifiedOn != null ? Long.toString(file.modifiedOn.toEpochMilli()) : "");
        return csvHelper.encodeFields(fields);
    }

    public File fromCsv(String csvLine) {
        List<String> fields = csvHelper.decodeFields(csvLine);
        if (fields.size() == 4) {
            File file = new File();
            int index = 0;
            for (String field: fields) {
                if (index == 0) {
                    file.path = Paths.get(field);
                } else if (index == 1) {
                    file.size = Long.valueOf(field);
                } else if (index == 2) {
                    file.createdOn = Instant.ofEpochMilli(Long.valueOf(field));
                } else if (index == 3) {
                    file.modifiedOn = Instant.ofEpochMilli(Long.valueOf(field));
                }
                index++;
            }
            return file;
        } else {
            throw new IllegalArgumentException("csv line should have exactly 4 fields, but was " + csvLine);
        }
    }
}
