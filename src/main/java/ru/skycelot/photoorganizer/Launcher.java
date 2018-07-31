package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.filesystem.File;
import ru.skycelot.photoorganizer.service.CsvHelper;
import ru.skycelot.photoorganizer.service.FileCsvConverter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("You should specify a directory to scan!");
        }
        Path rootDirectory = Paths.get(args[0]);
        MyFileVisitor fileVisitor = new MyFileVisitor();
        Files.walkFileTree(rootDirectory, fileVisitor);
        System.out.println("Number of files: " + fileVisitor.files.size());
        System.out.println("Files size: " + fileVisitor.files.stream().map(file -> file.size).reduce((i, k) -> i + k).orElse(0L));
        System.out.println("Earliest file created on " + fileVisitor.files.stream().map(file -> file.createdOn).sorted().limit(1).findAny().orElse(null));
        System.out.println("Earliest file modified on " + fileVisitor.files.stream().map(file -> file.modifiedOn).sorted().limit(1).findAny().orElse(null));
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        Path cvs = Paths.get("db.cvs");
        Files.write(cvs, fileVisitor.files.stream().map(file -> marshaller.toCsv(file)).collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        ;
    }

    public static class MyFileVisitor extends SimpleFileVisitor<Path> {
        private List<File> files = new LinkedList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            File fileDescription = new File();
            fileDescription.path = file;
            fileDescription.size = attrs.size();
            fileDescription.createdOn = attrs.creationTime().toInstant();
            fileDescription.modifiedOn = attrs.lastModifiedTime().toInstant();
            files.add(fileDescription);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public String toString() {
            return "MyFileVisitor{" +
                    "files=" + files +
                    '}';
        }
    }
}
