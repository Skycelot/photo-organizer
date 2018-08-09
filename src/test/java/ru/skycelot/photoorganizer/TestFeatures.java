package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.jpeg.Segment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException, IOException {
        Path jsonFile = Paths.get(ClassLoader.getSystemResource("files.json").toURI());
        ByteArrayOutputStream jsonBytes = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteChannel fileChannel = Files.newByteChannel(jsonFile, StandardOpenOption.READ);
        while (fileChannel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                jsonBytes.write(buffer.get());
            }
            buffer.clear();
        }
        String json = new String(jsonBytes.toByteArray(), StandardCharsets.UTF_8);
        FileEntityJsonConverter marshaller = new FileEntityJsonConverter(new JsonHelper());
        List<FileEntity> files = marshaller.unmarshall(json);

        int i = 0;
    }

    @Test
    public void exif() throws IOException {
        Path jpeg = Paths.get("/media/sf_Mount/_DSC0309.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteChannel channel = Files.newByteChannel(jpeg);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                baos.write(buffer.get());
            }
            buffer.clear();
        }
        byte[] content = baos.toByteArray();
        List<Segment> segments = new LinkedList<>();
        int blockStart = 0;
        while (blockStart < content.length) {
            Segment segment = new Segment((short) (((content[blockStart] & 0xFF) << 8) | (content[blockStart + 1] & 0xFF)));
            if (segment.hasLength()) {
                int length = (((content[blockStart + 2] & 0xFF) << 8) | (content[blockStart + 3] & 0xFF)) - 2;
                blockStart += 4;
                if (length > 0) {
                    segment.setContent(Arrays.copyOfRange(content, blockStart, blockStart + length));
                    blockStart += length;
                }
            } else {
                blockStart += 2;
            }
            segments.add(segment);
        }

        int z = 0;
    }
}
