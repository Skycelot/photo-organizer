package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.filesystem.File;
import ru.skycelot.photoorganizer.service.CsvHelper;
import ru.skycelot.photoorganizer.service.FileCsvConverter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException, IOException {
        Path csvFile = Paths.get(ClassLoader.getSystemResource("db.cvs").toURI());
        List<String> csvLines = Files.readAllLines(csvFile);
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        List<File> files = csvLines.stream().map(line -> marshaller.fromCsv(line)).collect(Collectors.toList());

        Map<Long, List<File>> sameSizeFiles = files.stream().collect(Collectors.groupingBy(file -> file.size));

        Map<Long, List<File>> differentSizeFiles = sameSizeFiles.entrySet().stream().
                filter(entry -> entry.getValue().size() > 1).
                collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        int i = 0;
    }
}
