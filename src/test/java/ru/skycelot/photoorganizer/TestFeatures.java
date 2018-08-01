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
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException, IOException {
        Path csvFile = Paths.get(ClassLoader.getSystemResource("db.csv").toURI());
        List<String> csvLines = Files.readAllLines(csvFile);
        FileCsvConverter marshaller = new FileCsvConverter(new CsvHelper());
        List<FileMetadata> files = csvLines.stream().map(line -> marshaller.fromCsv(line)).collect(Collectors.toList());

        Map<Long, List<FileMetadata>> filesGroupedBySize = files.stream().collect(Collectors.groupingBy(file -> file.size));

        SortedSet<Long> sortedCountsHavingMultipleFiles = filesGroupedBySize.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry -> entry.getKey()).collect(TreeSet::new, (set, number) -> set.add(number), (set1, set2) -> set1.addAll(set2));

        int i = 0;
    }
}
