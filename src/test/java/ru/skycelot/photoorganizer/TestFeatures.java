package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.domain.FileEntity;
import ru.skycelot.photoorganizer.service.Arithmetics;
import ru.skycelot.photoorganizer.service.ExifDateExtractor;
import ru.skycelot.photoorganizer.service.SegmentExtractor;
import ru.skycelot.photoorganizer.service.TiffBlockParser;

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
import java.time.Instant;
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
    public void exif() throws IOException, URISyntaxException {
        Path jpeg = Paths.get(ClassLoader.getSystemResource("photo.jpg").toURI());

        Arithmetics arithmetics = new Arithmetics();
        ExifDateExtractor extractor = new ExifDateExtractor(new SegmentExtractor(arithmetics), new TiffBlockParser(arithmetics));
        Instant date = extractor.extractDate(jpeg);

        int i = 0;
    }
}
