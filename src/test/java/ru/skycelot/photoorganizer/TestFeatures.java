package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.filesystem.FileMetadata;
import ru.skycelot.photoorganizer.conversion.csv.CsvHelper;
import ru.skycelot.photoorganizer.conversion.csv.FileCsvConverter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException, IOException {
        Path csvFile = Paths.get(ClassLoader.getSystemResource("db.csv").toURI());
        List<String> csvLines = Files.readAllLines(csvFile);
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        List<FileMetadata> files = csvLines.stream().map(line -> marshaller.fromCsv(line)).collect(Collectors.toList());

        int i = 0;
    }
}
