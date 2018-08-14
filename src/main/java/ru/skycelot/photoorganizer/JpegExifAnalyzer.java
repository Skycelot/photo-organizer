package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.conversion.json.SegmentsJsonConverter;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.filesystem.FileMetadataVisitor;
import ru.skycelot.photoorganizer.jpeg.JpegFile;
import ru.skycelot.photoorganizer.service.Arithmetics;
import ru.skycelot.photoorganizer.service.FileContentHelper;
import ru.skycelot.photoorganizer.service.JpegSlicer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
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
        FileMetadataVisitor fileVisitor = new FileMetadataVisitor(rootDirectory);
        Files.walkFileTree(rootDirectory, fileVisitor);
        Map<UUID, FileEntity> filesMap = fileVisitor.files.stream().
                map(fileMeta -> new FileEntity(fileMeta.path, fileMeta.size, fileMeta.createdOn, fileMeta.modifiedOn)).
                collect(Collectors.toMap(file -> file.uuid, file -> file));
        System.out.println("done!");

        System.out.println("Gathering magic numbers...");
        FileContentHelper contentHelper = new FileContentHelper(2);
        filesMap.values().stream().filter(file -> file.size > 1).forEach(file -> file.magicNumber = contentHelper.readMagicBytes(rootDirectory.resolve(file.path), 2));
        System.out.println("done!");

        System.out.println("Gathering jpeg exif statistics...");
        Arithmetics arithmetics = new Arithmetics();
        JpegSlicer slicer = new JpegSlicer(arithmetics);
        List<JpegFile> slicedJpegFiles = filesMap.values().stream().
                filter(file -> file.size > 1 && arithmetics.convertBytesToShort(file.magicNumber[0], file.magicNumber[1]) == 0xFFD8).
                map(file -> new JpegFile(file.path.toString(), slicer.slice(rootDirectory.resolve(file.path)))).collect(Collectors.toList());
        System.out.println("done!");

        System.out.println("Writing segments to db...");
        JsonHelper jsonHelper = new JsonHelper();
        SegmentsJsonConverter converter = new SegmentsJsonConverter(jsonHelper);
        byte[] content = converter.marshall(slicedJpegFiles).getBytes(StandardCharsets.UTF_8);

        Path db = Paths.get("segments.json");
        ByteBuffer buffer = ByteBuffer.allocate(512);
        int offset = 0;
        try (ByteChannel channel = Files.newByteChannel(db, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            while (offset < content.length) {
                while (buffer.remaining() > 0 && offset < content.length) {
                    buffer.put(content[offset++]);
                }
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
        }
        System.out.println("done!");
    }
}
