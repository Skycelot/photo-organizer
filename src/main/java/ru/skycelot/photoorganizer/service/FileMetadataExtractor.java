package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.FileMetadataJsonConverter;
import ru.skycelot.photoorganizer.filesystem.FileMetadata;
import ru.skycelot.photoorganizer.filesystem.FileMetadataVisitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileMetadataExtractor {

    private final FileMetadataVisitor fileVisitor;
    private final FileMetadataJsonConverter converter;

    public FileMetadataExtractor(FileMetadataVisitor fileVisitor, FileMetadataJsonConverter converter) {
        this.fileVisitor = fileVisitor;
        this.converter = converter;
    }

    public void extractFileMetadata(Path rootDirectory, Path db) {
        try {
            System.out.print("Walking through file system...");
            FileMetadataVisitor fileVisitor = new FileMetadataVisitor(rootDirectory);
            Files.walkFileTree(rootDirectory, fileVisitor);
            List<FileMetadata> files = fileVisitor.files;
            System.out.println("done!");

            System.out.print("Saving files' metadata to database...");
            byte[] json = converter.marshall(files).getBytes(StandardCharsets.UTF_8);
            Files.write(db, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
