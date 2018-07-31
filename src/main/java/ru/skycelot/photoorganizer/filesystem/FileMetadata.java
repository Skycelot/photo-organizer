package ru.skycelot.photoorganizer.filesystem;

import java.nio.file.Path;
import java.time.Instant;

public class FileMetadata {
    public Path path;
    public long size;
    public Instant createdOn;
    public Instant modifiedOn;
    public byte[] hash;
    public byte[] magicBytes;
}
