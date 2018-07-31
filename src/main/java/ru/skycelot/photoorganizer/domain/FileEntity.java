package ru.skycelot.photoorganizer.domain;

import java.time.Instant;
import java.util.Arrays;

public class FileEntity {
    public String uuid;
    public String path;
    public String name;
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
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                ", hash=" + Arrays.toString(hash) +
                ", magicBytes=" + Arrays.toString(magicBytes) +
                '}';
    }
}
