package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.jpeg.Segment;
import ru.skycelot.photoorganizer.jpeg.SegmentType;
import ru.skycelot.photoorganizer.jpeg.TagType;
import ru.skycelot.photoorganizer.jpeg.TiffTag;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ExifDateExtractor {

    private final SegmentExtractor segmentExtractor;
    private final TiffBlockParser tiffBlockParser;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public ExifDateExtractor(SegmentExtractor segmentExtractor, TiffBlockParser tiffBlockParser) {
        this.segmentExtractor = segmentExtractor;
        this.tiffBlockParser = tiffBlockParser;
    }

    public Instant extractDate(Path filePath) {
        Instant result = null;
        Segment exifSegment = segmentExtractor.findSegment(SegmentType.app1, filePath);
        if (exifSegment != null &&
                Arrays.equals(Arrays.copyOfRange(exifSegment.getContent(), 0, 6), new byte[]{(byte) 0x45, (byte) 0x78, (byte) 0x69, (byte) 0x66, (byte) 0x00, (byte) 0x00})) {
            byte[] tiffBlock = Arrays.copyOfRange(exifSegment.getContent(), 6, exifSegment.getContent().length);
            Map<TagType, TiffTag> tags = tiffBlockParser.extractEntries(tiffBlock).stream().collect(Collectors.toMap(tag -> tag.tagType, tag -> tag));
            if (tags.containsKey(TagType.DateTimeOriginal)) {
                String dateString = new String(Arrays.copyOfRange(tags.get(TagType.DateTimeOriginal).value, 0, 19), StandardCharsets.US_ASCII);
                LocalDateTime date = LocalDateTime.parse(dateString, dateTimeFormatter);
                ZonedDateTime zonedDate = ZonedDateTime.ofLocal(date, ZoneId.systemDefault(), null);
                return zonedDate.toInstant();
            } else if (tags.containsKey(TagType.DateTime)) {
                String dateString = new String(Arrays.copyOfRange(tags.get(TagType.DateTime).value, 0, 19), StandardCharsets.US_ASCII);
                LocalDateTime date = LocalDateTime.parse(dateString, dateTimeFormatter);
                ZonedDateTime zonedDate = ZonedDateTime.ofLocal(date, ZoneId.systemDefault(), null);
                return zonedDate.toInstant();
            }
        }
        return result;
    }
}
