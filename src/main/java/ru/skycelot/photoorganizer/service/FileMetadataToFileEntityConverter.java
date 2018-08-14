package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileMetadataJsonConverter;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.filesystem.FileMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class FileMetadataToFileEntityConverter {

    private final FileMetadataJsonConverter fileMetadataJsonConverter;
    private final FileEntityJsonConverter fileEntityJsonConverter;

    public FileMetadataToFileEntityConverter(FileMetadataJsonConverter fileMetadataJsonConverter, FileEntityJsonConverter fileEntityJsonConverter) {
        this.fileMetadataJsonConverter = fileMetadataJsonConverter;
        this.fileEntityJsonConverter = fileEntityJsonConverter;
    }

    public void persistEntities(Path fileMetadataDb, Path fileEntityDb) {
        try {
            System.out.print("Reading files' metadata from database...");
            byte[] fileMetadataContent = Files.readAllBytes(fileMetadataDb);
            List<FileMetadata> fileMetadataList = fileMetadataJsonConverter.unmarshall(new String(fileMetadataContent, StandardCharsets.UTF_8));
            System.out.println("done!");

            System.out.print("Converting files' metadata to entities...");
            List<FileEntity> fileEntities = fileMetadataList.stream().
                    map(fileMetadata -> new FileEntity(fileMetadata.path, fileMetadata.size, fileMetadata.createdOn, fileMetadata.modifiedOn)).
                    collect(Collectors.toList());
            System.out.println("done!");

            System.out.print("Saving file entities to database...");
            byte[] json = fileEntityJsonConverter.marshall(fileEntities).getBytes(StandardCharsets.UTF_8);
            Files.write(fileEntityDb, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
