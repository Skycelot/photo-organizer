package ru.skycelot.photoorganizer.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class FileEntity {
    public UUID uuid;
    public Path path;
    public Extension extension;
    public long size;
    public Instant createdOn;
    public Instant modifiedOn;
    public Instant exifDate;
    public byte[] hash;
    public byte[] magicNumber;
    public Integer exifOffset;

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
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                ", hash=" + Arrays.toString(hash) +
                ", magicNumber=" + Arrays.toString(magicNumber) +
                '}';
    }
}
