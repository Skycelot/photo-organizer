package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.filesystem.FileMetadata;

import java.util.UUID;

public class FileMetadataToFileEntityConverter {

    public FileEntity convert(FileMetadata fileMetadata, String mainDirPath) {
        FileEntity result = new FileEntity(fileMetadata.path, fileMetadata.size, fileMetadata.createdOn, fileMetadata.modifiedOn);
        result.uuid = UUID.randomUUID();
        result.path = fileMetadata.path;
        return result;
    }
}
