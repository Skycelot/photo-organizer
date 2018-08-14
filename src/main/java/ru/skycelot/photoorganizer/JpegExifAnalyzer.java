package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.filesystem.DirectoryScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpegExifAnalyzer {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("You should specify a directory to scan!");
        }
        Path rootDirectory = Paths.get(args[0]).normalize();

        System.out.print("Walking through file system...");
        DirectoryScanner fileVisitor = new DirectoryScanner(rootDirectory);
        Files.walkFileTree(rootDirectory, fileVisitor);
        Map<UUID, FileEntity> filesMap = fileVisitor.files.stream().
                map(fileMeta -> new FileEntity(fileMeta.path, fileMeta.size, fileMeta.createdOn, fileMeta.modifiedOn)).
                collect(Collectors.toMap(file -> file.uuid, file -> file));
        System.out.println("done!");

        System.out.println("Gathering jpeg exif statistics...");

        System.out.println("done!");
    }
}
