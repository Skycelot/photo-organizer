package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.domain.FileEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class MagicNumbersProcessor {

    private final FileEntityJsonConverter converter;
    private final FileContentHelper fileContentHelper;

    public MagicNumbersProcessor(FileEntityJsonConverter converter, FileContentHelper fileContentHelper) {
        this.converter = converter;
        this.fileContentHelper = fileContentHelper;
    }

    public void addMagicNumbers(Path db, Path rootFolder) {
        try {
            System.out.print("Reading files from database...");
            byte[] fileMetadataContent = Files.readAllBytes(db);
            List<FileEntity> files = converter.unmarshall(new String(fileMetadataContent, StandardCharsets.UTF_8));
            System.out.println("done!");

            System.out.print("Reading magic numbers...");
            files.stream().filter(file -> file.size > 0).forEach(file -> file.magicNumber = fileContentHelper.readMagicBytes(rootFolder.resolve(file.path), 6));
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
