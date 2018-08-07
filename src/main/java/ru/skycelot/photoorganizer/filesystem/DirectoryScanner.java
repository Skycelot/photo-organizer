package ru.skycelot.photoorganizer.filesystem;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public class DirectoryScanner extends SimpleFileVisitor<Path> {

    public final Path rootDirectory;
    public List<FileMetadata> files = new LinkedList<>();

    public DirectoryScanner(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        FileMetadata fileDescription = new FileMetadata();
        fileDescription.path = file.subpath(rootDirectory.getNameCount(), file.getNameCount());
        fileDescription.size = attrs.size();
        fileDescription.createdOn = attrs.creationTime().toInstant();
        fileDescription.modifiedOn = attrs.lastModifiedTime().toInstant();
        files.add(fileDescription);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        return "DirectoryScanner{" +
                "files=" + files +
                '}';
    }
}
