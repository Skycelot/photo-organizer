package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");
        final String dateAsString = "2 0 1 0 : 0 1 : 13";
        String[] withoutSpaces = dateAsString.split("\\s");
        String reformattedDateAsString = Arrays.asList(withoutSpaces).stream().collect(Collectors.joining());
        LocalDate date = LocalDate.parse(reformattedDateAsString, dateFormatter);
        System.out.println(date);

        int i = 0;
    }
}
