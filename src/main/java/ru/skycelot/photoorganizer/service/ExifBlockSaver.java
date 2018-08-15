package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.domain.Extension;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.jpeg.Segment;
import ru.skycelot.photoorganizer.jpeg.SegmentType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ExifBlockSaver {

    private final FileEntityJsonConverter fileEntityJsonConverter;
    private final DuplicatesJsonConverter duplicatesJsonConverter;
    private final JpegSlicer jpegSlicer;
    private final TiffBlockParser tiffBlockParser;
    private final byte[] exifMarker = new byte[]{'E', 'x', 'i', 'f', 0, 0};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public ExifBlockSaver(FileEntityJsonConverter fileEntityJsonConverter, DuplicatesJsonConverter duplicatesJsonConverter, JpegSlicer jpegSlicer, TiffBlockParser tiffBlockParser) {
        this.fileEntityJsonConverter = fileEntityJsonConverter;
        this.duplicatesJsonConverter = duplicatesJsonConverter;
        this.jpegSlicer = jpegSlicer;
        this.tiffBlockParser = tiffBlockParser;
    }

    public void extractExifInformation(Path filesDb, Path duplicatesDb, Path rootDirectory) {
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
            files.values().stream().filter(file -> file.extension == Extension.JPG).forEach(file -> {
                UUID processedUuid = duplicates.containsKey(file.uuid) ? setsIntersection(duplicates.get(file.uuid),processedUuids) : null;
                if (processedUuid ==null) {
                    List<Segment> segments = jpegSlicer.slice(rootDirectory.resolve(file.path));
                    for (Segment segment : segments) {
                        if (segment.getType() == SegmentType.app1 && segment.getContent().length > 6 &&
                                Arrays.equals(Arrays.copyOfRange(segment.getContent(), 0, 6), exifMarker)) {
                            byte[] tiffBlock = Arrays.copyOfRange(segment.getContent(), 6, segment.getContent().length);
                            if (tiffBlockParser.validTiffBlock(tiffBlock)) {
                                file.tiffBlockOffset = segment.getOffset() + 6;
                                file.tiffBlockLength = segment.getSize() - 6;
                                processedUuids.add(file.uuid);
                                break;
                            }
                        }
                    }
                } else {
                    FileEntity processedFile = files.get(processedUuid);
                    file.tiffBlockOffset = processedFile.tiffBlockOffset;
                    file.tiffBlockLength = processedFile.tiffBlockLength;
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

    private UUID setsIntersection(Set<UUID> setA, Set<UUID> setB) {
        Set<UUID> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        return intersection.isEmpty() ? null : intersection.iterator().next();
    }
}
