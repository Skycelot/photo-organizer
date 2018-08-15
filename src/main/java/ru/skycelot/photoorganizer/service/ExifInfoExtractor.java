package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.jpeg.TagType;
import ru.skycelot.photoorganizer.jpeg.TiffTag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ExifInfoExtractor {

    private final FileEntityJsonConverter fileEntityJsonConverter;
    private final DuplicatesJsonConverter duplicatesJsonConverter;
    private final TiffBlockParser tiffBlockParser;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    public ExifInfoExtractor(FileEntityJsonConverter fileEntityJsonConverter, DuplicatesJsonConverter duplicatesJsonConverter, TiffBlockParser tiffBlockParser) {
        this.fileEntityJsonConverter = fileEntityJsonConverter;
        this.duplicatesJsonConverter = duplicatesJsonConverter;
        this.tiffBlockParser = tiffBlockParser;
    }

    public void extractExifInfo(Path filesDb, Path duplicatesDb, Path rootDirectory) {
        try {
            System.out.print("Reading files from database...");
            byte[] fileContent = Files.readAllBytes(filesDb);
            Map<UUID, FileEntity> files = fileEntityJsonConverter.unmarshall(new String(fileContent, StandardCharsets.UTF_8)).stream().collect(Collectors.toMap(file -> file.uuid, file -> file));
            fileContent = Files.readAllBytes(duplicatesDb);
            Map<UUID, Set<UUID>> duplicates = new HashMap<>();
            duplicatesJsonConverter.unmarshall(new String(fileContent, StandardCharsets.UTF_8)).forEach(duplication -> {
                for (UUID uuid : duplication.filesIds) {
                    duplicates.put(uuid, new HashSet<>(duplication.filesIds));
                }
            });
            System.out.println("done!");

            System.out.print("Searching for exif information...");
            Set<UUID> processedUuids = new HashSet<>();
            files.values().stream().filter(file -> file.tiffBlockOffset != null).forEach(file -> {
                UUID processedUuid = null;
                if (duplicates.containsKey(file.uuid)) {
                    Set<UUID> intersection = new HashSet<>(duplicates.get(file.uuid));
                    intersection.retainAll(processedUuids);
                    if (!intersection.isEmpty()) {
                        intersection.iterator().next();
                    }
                }
                if (processedUuid == null) {
                    try (SeekableByteChannel channel = Files.newByteChannel(rootDirectory.resolve(file.path))) {
                        ByteBuffer buffer = ByteBuffer.allocate(file.tiffBlockLength);
                        channel.position(file.tiffBlockOffset);
                        channel.read(buffer);
                        buffer.flip();
                        byte[] tiffBlock = new byte[file.tiffBlockLength];
                        int offset = 0;
                        while (buffer.hasRemaining()) {
                            tiffBlock[offset++] = buffer.get();
                        }
                        Map<TagType, TiffTag> tags = tiffBlockParser.extractEntries(tiffBlock).stream().collect(Collectors.toMap(tag -> tag.tagType, tag -> tag));
                        if (tags.containsKey(TagType.Make)) {
                            TiffTag tag = tags.get(TagType.Make);
                            if (tag.value[tag.value.length - 1] == 0) {
                                file.exifCamera = new String(Arrays.copyOfRange(tag.value, 0, tag.value.length - 1), StandardCharsets.US_ASCII);
                            }
                        }
                        if (tags.containsKey(TagType.Model)) {
                            TiffTag tag = tags.get(TagType.Model);
                            if (tag.value[tag.value.length - 1] == 0) {
                                String model = new String(Arrays.copyOfRange(tag.value, 0, tag.value.length - 1), StandardCharsets.US_ASCII);
                                file.exifCamera = file.exifCamera != null ? file.exifCamera + ": " + model : model;
                            }
                        }
                        if (tags.containsKey(TagType.DateTimeOriginal)) {
                            TiffTag tag = tags.get(TagType.DateTimeOriginal);
                            if (tag.value.length == 20 && tag.value[tag.value.length - 1] == 0) {
                                String date = new String(Arrays.copyOfRange(tag.value, 0, 19), StandardCharsets.US_ASCII);
                                if (!date.startsWith("0000:00")) {
                                    try {
                                        file.exifDate = LocalDateTime.parse(date, dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant();
                                    } catch (DateTimeParseException e) {
                                        try {
                                            String dateWithoutEvenBytes = new String(new byte[]{tag.value[0], tag.value[2], tag.value[4], tag.value[6], tag.value[8], tag.value[10], tag.value[12], tag.value[14], tag.value[16], tag.value[18]}, StandardCharsets.US_ASCII);
                                            file.exifDate = LocalDate.parse(dateWithoutEvenBytes, dateFormatter).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                                        } catch (DateTimeParseException o) {
                                            System.out.println("Error parsing exif date " + date + " for " + file.path.getFileName());
                                        }
                                    }
                                }
                            }
                        }
                        if (file.exifDate == null && tags.containsKey(TagType.DateTime)) {
                            TiffTag tag = tags.get(TagType.DateTime);
                            if (tag.value.length == 20 && tag.value[tag.value.length - 1] == 0) {
                                String date = new String(Arrays.copyOfRange(tag.value, 0, 19), StandardCharsets.US_ASCII);
                                if (!date.startsWith("0000:00")) {
                                    try {
                                        file.exifDate = LocalDateTime.parse(date, dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant();
                                    } catch (DateTimeParseException e) {
                                        try {
                                            String dateWithoutEvenBytes = new String(new byte[]{tag.value[0], tag.value[2], tag.value[4], tag.value[6], tag.value[8], tag.value[10], tag.value[12], tag.value[14], tag.value[16], tag.value[18]}, StandardCharsets.US_ASCII);
                                            file.exifDate = LocalDate.parse(dateWithoutEvenBytes, dateFormatter).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                                        } catch (DateTimeParseException o) {
                                            System.out.println("Error parsing exif date " + date + " for " + file.path.getFileName());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error during processing file " + file.path);
                        throw new RuntimeException(e);
                    }
                } else {
                    FileEntity processedFile = files.get(processedUuid);
                    file.exifCamera = processedFile.exifCamera;
                    file.exifDate = processedFile.exifDate;
                }
            });
            System.out.println("done!");

            System.out.print("Saving files to database...");
            byte[] json = fileEntityJsonConverter.marshall(new ArrayList<>(files.values())).getBytes(StandardCharsets.UTF_8);
            Files.write(filesDb, json, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
