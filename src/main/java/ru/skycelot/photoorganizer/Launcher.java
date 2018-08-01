package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.filesystem.FileMetadata;
import ru.skycelot.photoorganizer.service.CsvHelper;
import ru.skycelot.photoorganizer.service.FileContentAnalyzer;
import ru.skycelot.photoorganizer.service.FileCsvConverter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("You should specify a directory to scan!");
        }
        Path rootDirectory = Paths.get(args[0]);
        MyFileVisitor fileVisitor = new MyFileVisitor(new FileContentAnalyzer());
        Files.walkFileTree(rootDirectory, fileVisitor);
        System.out.println("Number of files: " + fileVisitor.files.size());
        System.out.println("Files size: " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.size).reduce((i, k) -> i + k).orElse(0L));
        System.out.println("Earliest file created on " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.createdOn).sorted().limit(1).findAny().orElse(null));
        System.out.println("Earliest file modified on " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.modifiedOn).sorted().limit(1).findAny().orElse(null));
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        Path cvs = Paths.get("db.csv");
        Files.write(cvs, fileVisitor.files.stream().map(fileMetadata -> marshaller.toCsv(fileMetadata)).collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static class MyFileVisitor extends SimpleFileVisitor<Path> {
        private final FileContentAnalyzer analyzer;
        List<FileMetadata> files = new LinkedList<>();

        public MyFileVisitor(FileContentAnalyzer analyzer) {
            this.analyzer = analyzer;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            FileMetadata fileDescription = new FileMetadata();
            fileDescription.path = file;
            fileDescription.size = attrs.size();
            fileDescription.createdOn = attrs.creationTime().toInstant();
            fileDescription.modifiedOn = attrs.lastModifiedTime().toInstant();
            FileContentAnalyzer.ContentAnalysis analysis = analyzer.analyze(file, attrs.size());
            fileDescription.magicBytes = analysis.magicBytes;
            fileDescription.hash = analysis.hash;
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
