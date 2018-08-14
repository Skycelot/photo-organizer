package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.domain.Extension;
import ru.skycelot.photoorganizer.domain.FileEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ExtensionExtractor {

    private final FileEntityJsonConverter converter;

    public ExtensionExtractor(FileEntityJsonConverter converter) {
        this.converter = converter;
    }

    public void extractExtensions(Path db) {
        try {
            System.out.print("Reading files from database...");
            byte[] fileMetadataContent = Files.readAllBytes(db);
            List<FileEntity> files = converter.unmarshall(new String(fileMetadataContent, StandardCharsets.UTF_8));
            System.out.println("done!");

            System.out.print("Extracting extensions...");
            files.stream().
                    filter(file -> file.size > 0 && file.magicNumber != null).
                    forEach(file -> file.extension = Extension.findByMagicNumber(file.magicNumber));
            System.out.println("done!");

            System.out.print("Saving files to database...");
            byte[] json = converter.marshall(files).getBytes(StandardCharsets.UTF_8);
            Files.write(db, json, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
