package ru.skycelot.photoorganizer.service;

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
import java.util.Arrays;
import java.util.List;

public class ExifBlockSaver {

    private final FileEntityJsonConverter converter;
    private final JpegSlicer jpegSlicer;
    private final TiffBlockParser tiffBlockParser;
    private final byte[] exifMarker = new byte[]{'E', 'x', 'i', 'f', 0, 0};
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public ExifBlockSaver(FileEntityJsonConverter converter, JpegSlicer jpegSlicer, TiffBlockParser tiffBlockParser) {
        this.converter = converter;
        this.jpegSlicer = jpegSlicer;
        this.tiffBlockParser = tiffBlockParser;
    }

    public void extractExifInformation(Path db, Path rootDirectory) {
        try {
            System.out.print("Reading files from database...");
            byte[] fileMetadataContent = Files.readAllBytes(db);
            List<FileEntity> files = converter.unmarshall(new String(fileMetadataContent, StandardCharsets.UTF_8));
            System.out.println("done!");

            System.out.print("Searching for exif information...");
            files.stream().filter(file -> file.extension == Extension.JPG).forEach(file -> {
                List<Segment> segments = jpegSlicer.slice(rootDirectory.resolve(file.path));
                for (Segment segment : segments) {
                    if (segment.getType() == SegmentType.app1 && segment.getContent().length > 6 &&
                            Arrays.equals(Arrays.copyOfRange(segment.getContent(), 0, 6), exifMarker)) {
                        byte[] tiffBlock = Arrays.copyOfRange(segment.getContent(), 6, segment.getContent().length);
                        if (tiffBlockParser.validTiffBlock(tiffBlock)) {
                            file.tiffBlockOffset = segment.getOffset() + 6;
                            file.tiffBlockLength = segment.getSize() - 6;
                            break;
                        }
                    }
                }
            });
            System.out.println("done!");

            System.out.print("Saving files to database...");
            byte[] json = converter.marshall(files).getBytes(StandardCharsets.UTF_8);
            Files.write(db, json, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
