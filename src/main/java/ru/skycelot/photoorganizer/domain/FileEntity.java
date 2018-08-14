package ru.skycelot.photoorganizer.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class FileEntity {
    public UUID uuid;
    public Path path;
    public Extension extension;
    public long size;
    public Instant createdOn;
    public Instant modifiedOn;
    public Integer tiffBlockOffset;
    public Integer tiffBlockLength;
    public String exifCamera;
    public Instant exifDate;
    public byte[] magicNumber;
    public byte[] hash;

    public FileEntity() {
    }

    public FileEntity(Path path, long size, Instant createdOn, Instant modifiedOn) {
        this.uuid = UUID.randomUUID();
        this.path = path;
        this.size = size;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "path='" + path + '\'' +
                ", size=" + size +
                '}';
    }
}
