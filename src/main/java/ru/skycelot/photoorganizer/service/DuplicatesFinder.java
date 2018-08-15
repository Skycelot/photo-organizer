package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.domain.Duplicates;
import ru.skycelot.photoorganizer.domain.Extension;
import ru.skycelot.photoorganizer.domain.FileEntity;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class DuplicatesFinder {

    private final FileEntityJsonConverter fileEntityJsonConverter;
    private final DuplicatesJsonConverter duplicatesJsonConverter;
    private final FileContentHelper fileContentHelper;

    public DuplicatesFinder(FileEntityJsonConverter fileEntityJsonConverter, DuplicatesJsonConverter duplicatesJsonConverter, FileContentHelper fileContentHelper) {
        this.fileEntityJsonConverter = fileEntityJsonConverter;
        this.duplicatesJsonConverter = duplicatesJsonConverter;
        this.fileContentHelper = fileContentHelper;
    }

    public void findDuplicates(Path filesDb, Path duplicatesDb, Path rootDirectory) {
        try {
            System.out.print("Reading files from database...");
            byte[] fileMetadataContent = Files.readAllBytes(filesDb);
            List<FileEntity> files = fileEntityJsonConverter.unmarshall(new String(fileMetadataContent, StandardCharsets.UTF_8));
            System.out.println("done!");

            System.out.print("Searching for duplicates...");
            List<FileEntity> filesToCalculateHash = files.stream().
                    filter(file -> file.extension != Extension.NOT_AN_IMAGE).
                    collect(Collectors.groupingBy(file -> file.size)).values().stream().
                    filter(fileList -> fileList.size() > 1).
                    flatMap(fileList -> fileList.stream()).collect(Collectors.toList());
            filesToCalculateHash.forEach(file -> file.hash = fileContentHelper.calculateHash(rootDirectory.resolve(file.path)));
            List<Duplicates> duplicates = filesToCalculateHash.stream().
                    collect(Collectors.groupingBy(file -> DatatypeConverter.printHexBinary(file.hash))).values().stream().
                    filter(fileList -> fileList.size() > 1).
                    map(fileList -> new Duplicates(fileList.stream().map(file -> file.uuid).collect(Collectors.toList()))).collect(Collectors.toList());
            System.out.println("done!");

            System.out.print("Saving duplicates to database...");
            byte[] json = duplicatesJsonConverter.marshall(duplicates).getBytes(StandardCharsets.UTF_8);
            Files.write(duplicatesDb, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
