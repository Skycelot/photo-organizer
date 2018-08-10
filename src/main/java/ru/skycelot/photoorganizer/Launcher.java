package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.domain.Duplicates;
import ru.skycelot.photoorganizer.domain.Extension;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.filesystem.DirectoryScanner;
import ru.skycelot.photoorganizer.service.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Launcher {

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

        System.out.print("Gathering magic numbers...");
        FileContentHelper contentHelper = new FileContentHelper(1024 * 1024);
        filesMap.values().stream().filter(file -> file.size > 0).forEach(file -> {
            file.magicNumber = contentHelper.readMagicBytes(rootDirectory.resolve(file.path), 6);
        });
        System.out.println("done!");

        System.out.print("Finding duplicates...");
        List<List<FileEntity>> sameSizeFiles = filesMap.values().stream().
                collect(Collectors.groupingBy(file -> file.size)).entrySet().stream().
                filter(entry -> entry.getKey() > 0 && entry.getValue().size() > 1).
                map(entry -> entry.getValue()).
                collect(Collectors.toList());
        sameSizeFiles.stream().flatMap(files -> files.stream()).forEach(file -> {
            file.hash = contentHelper.calculateHash(rootDirectory.resolve(file.path));
        });
        Map<String, List<FileEntity>> sameHashFiles = filesMap.values().stream().
                filter(file -> file.hash != null).
                collect(Collectors.groupingBy(file -> DatatypeConverter.printHexBinary(file.hash)));
        List<Duplicates> duplicates = sameHashFiles.values().stream().filter(list -> list.size() > 1).
                map(list -> new Duplicates(list.stream().map(file -> file.uuid).collect(Collectors.toList()))).
                collect(Collectors.toList());
        System.out.println("done!");

        System.out.print("Detecting images...");
        filesMap.values().stream().forEach(file -> file.extension = Extension.findByMagicNumber(file.magicNumber));
        System.out.println("done!");

        System.out.print("Extracting EXIF dates...");
        Arithmetics arithmetics = new Arithmetics();
        SegmentExtractor segmentExtractor = new SegmentExtractor(arithmetics);
        TiffBlockParser tiffBlockParser = new TiffBlockParser(arithmetics);
        ExifDateExtractor exifDateExtractor = new ExifDateExtractor(segmentExtractor, tiffBlockParser);
        filesMap.values().stream().filter(file -> file.extension == Extension.JPG).forEach(file -> exifDateExtractor.extractDate(rootDirectory.resolve(file.path)));
        System.out.println("done!");

        System.out.println("Number of files: " + filesMap.values().size());
        System.out.println("Files size: " + fileVisitor.files.stream().map(file -> file.size).reduce((i, k) -> i + k).orElse(0L));
        System.out.println("Earliest file created on " + fileVisitor.files.stream().map(file -> file.createdOn).sorted().limit(1).findAny().orElse(null));
        System.out.println("Earliest file modified on " + fileVisitor.files.stream().map(file -> file.modifiedOn).sorted().limit(1).findAny().orElse(null));
        List<FileEntity> images = filesMap.values().stream().filter(file -> file.extension != Extension.NOT_AN_IMAGE).collect(Collectors.toList());
        System.out.println("Number of images: " + images.size());
        System.out.println("Images size: " + images.stream().map(file -> file.size).reduce((i, k) -> i + k).orElse(0L));
        System.out.println("Earliest image created on " + images.stream().map(file -> file.createdOn).sorted().limit(1).findAny().orElse(null));
        System.out.println("Earliest image modified on " + images.stream().map(file -> file.modifiedOn).sorted().limit(1).findAny().orElse(null));

        JsonHelper jsonHelper = new JsonHelper();

        System.out.print("Saving files' database...");
        Path db = Paths.get("files.json");
        ByteChannel channel = Files.newByteChannel(db, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        byte[] json = new FileEntityJsonConverter(jsonHelper).marshall(new ArrayList<>(filesMap.values())).getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(json.length);
        buffer.put(json);
        buffer.flip();
        channel.write(buffer);
        channel.close();
        System.out.println("done!");

        System.out.print("Saving duplicates' database...");
        db = Paths.get("duplicates.json");
        channel = Files.newByteChannel(db, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        json = new DuplicatesJsonConverter(jsonHelper).marshall(duplicates).getBytes(StandardCharsets.UTF_8);
        buffer = ByteBuffer.allocate(json.length);
        buffer.put(json);
        buffer.flip();
        channel.write(buffer);
        channel.close();
        System.out.println("done!");
    }

}
