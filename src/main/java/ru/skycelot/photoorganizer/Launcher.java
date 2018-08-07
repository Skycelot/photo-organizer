package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.service.DirectoryScanner;
import ru.skycelot.photoorganizer.service.FileJsonConverter;
import ru.skycelot.photoorganizer.service.JsonHelper;

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

public class Launcher {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("You should specify a directory to scan!");
        }
        Path rootDirectory = Paths.get(args[0]).normalize();
        DirectoryScanner fileVisitor = new DirectoryScanner(rootDirectory);
        Files.walkFileTree(rootDirectory, fileVisitor);

        Map<UUID, FileEntity> fileMap = fileVisitor.files.stream().
                map(fileMeta -> new FileEntity(fileMeta.path, fileMeta.size, fileMeta.createdOn, fileMeta.modifiedOn)).
                collect(Collectors.toMap(file -> file.uuid, file -> file));

        Map<Long, List<FileEntity>> filesGroupedBySize = fileMap.values().stream().collect(Collectors.groupingBy(file -> file.size));
        filesGroupedBySize.entrySet().stream().filter(entry -> entry.getKey() > 0 && entry.getValue().size() > 1).forEach(entry -> {
            List<FileEntity> files = entry.getValue();
        });


        System.out.println("Number of files: " + fileVisitor.files.size());
        System.out.println("Files size: " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.size).reduce((i, k) -> i + k).orElse(0L));
        System.out.println("Earliest file created on " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.createdOn).sorted().limit(1).findAny().orElse(null));
        System.out.println("Earliest file modified on " + fileVisitor.files.stream().map(fileMetadata -> fileMetadata.modifiedOn).sorted().limit(1).findAny().orElse(null));

        Path db = Paths.get("db.json");
        ByteChannel channel = Files.newByteChannel(db, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        byte[] json = new FileJsonConverter(new JsonHelper()).marshall(fileVisitor.files).getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(json.length);
        buffer.put(json);
        buffer.flip();
        channel.write(buffer);
        channel.close();
    }

}
