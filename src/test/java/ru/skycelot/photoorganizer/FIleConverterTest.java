package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skycelot.photoorganizer.filesystem.File;
import ru.skycelot.photoorganizer.service.CsvHelper;
import ru.skycelot.photoorganizer.service.FileCsvConverter;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FIleConverterTest {

    private FileCsvConverter marshaller;

    @Mock
    private CsvHelper csvHelper;

    @BeforeEach
    public void init() {
        marshaller = new FileCsvConverter(csvHelper);
    }

    @Test
    public void testFileToCsvConverter() {

        File file = new File();
        file.path = Paths.get("test this \"file\"");
        file.size = 12L;
        file.createdOn = Instant.ofEpochMilli(15L);
        file.modifiedOn = Instant.ofEpochMilli(20L);

        marshaller.toCsv(file);

        ArgumentCaptor<List<String>> fieldsCaptor = ArgumentCaptor.forClass(List.class);
        verify(csvHelper).encodeFields(fieldsCaptor.capture());
        List<String> actualFields = fieldsCaptor.getValue();
        assertEquals(Arrays.asList("test this \"file\"", "12", "15", "20"), actualFields);
    }

    @Test
    public void testCsvToFileConverter() {

        when(csvHelper.decodeFields(ArgumentMatchers.anyString())).thenReturn(Arrays.asList("test this \"file\"", "12", "15", "20"));

        File file = marshaller.fromCsv("");

        assertEquals(Paths.get("test this \"file\""), file.path);
        assertEquals(12L, file.size);
        assertEquals(Instant.ofEpochMilli(15L), file.createdOn);
        assertEquals(Instant.ofEpochMilli(20L), file.modifiedOn);
    }
}
