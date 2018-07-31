package ru.skycelot.photoorganizer.filesystem;

import java.nio.file.Path;
import java.time.Instant;

public class File {
    public Path path;
    public long size;
    public Instant createdOn;
    public Instant modifiedOn;

    @Override
    public String toString() {
        return "File{" +
                "path=" + path +
                ", size=" + size +
                ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                '}';
    }
}
