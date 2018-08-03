package ru.skycelot.photoorganizer.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class FileEntity {
    public UUID uuid;
    public Path path;
    public String extension;
    public long size;
    public Instant createdOn;
    public Instant modifiedOn;
    public byte[] hash;
    public byte[] magicBytes;

    @Override
    public String toString() {
        return "FileEntity{" +
                "path='" + path + '\'' +
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                ", hash=" + Arrays.toString(hash) +
                ", magicBytes=" + Arrays.toString(magicBytes) +
                '}';
    }
}
