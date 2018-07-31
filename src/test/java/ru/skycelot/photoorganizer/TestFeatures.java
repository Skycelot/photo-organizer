package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.filesystem.FileMetadata;
import ru.skycelot.photoorganizer.service.CsvHelper;
import ru.skycelot.photoorganizer.service.FileCsvConverter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException, IOException {
        Path csvFile = Paths.get(ClassLoader.getSystemResource("db.cvs").toURI());
        List<String> csvLines = Files.readAllLines(csvFile);
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        List<FileMetadata> files = csvLines.stream().map(line -> marshaller.fromCsv(line)).collect(Collectors.toList());

        Map<Long, List<FileMetadata>> filesGroupedBySize = files.stream().collect(Collectors.groupingBy(file -> file.size));

        List<Long> sortedFileSizes = filesGroupedBySize.keySet().stream().sorted().collect(Collectors.toList());

        Map<Long, Integer> fileNumbersBySize = sortedFileSizes.stream().sequential().collect(
                LinkedHashMap::new,
                (map, size) -> map.put(size, filesGroupedBySize.get(size).size()),
                (map1, map2) -> new IllegalStateException()
        );

        int i = 0;
    }
}
