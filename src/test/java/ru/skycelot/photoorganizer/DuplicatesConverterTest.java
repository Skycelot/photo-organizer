package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.domain.Duplicates;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DuplicatesConverterTest {

    @Test
    public void testJsonUnmarshall() throws URISyntaxException, IOException {
        Path duplicatesDb = Paths.get(ClassLoader.getSystemResource("duplicates.json").toURI());
        DuplicatesJsonConverter converter = new DuplicatesJsonConverter(new JsonHelper());

        byte[] duplicatesContent = Files.readAllBytes(duplicatesDb);
        List<Duplicates> duplicates = converter.unmarshall(new String(duplicatesContent, StandardCharsets.UTF_8));

        int i = 0;
    }
}
