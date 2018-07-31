package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.service.CsvHelper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvHelperTest {

    private CsvHelper csvHelper;
    private static final List<String> referenceFields =
            Arrays.asList("test,comma", "test\"double quote", "", "test\"double quote with,comma", "test without escapes", "", "");
    private static final String referenceCsvLine =
            "\"test,comma\",\"test\"\"double quote\",,\"test\"\"double quote with,comma\",test without escapes,,";

    @BeforeEach
    public void init() {
        csvHelper = new CsvHelper();
    }

    @Test
    public void testEncoding() {
        String encoded = csvHelper.encodeFields(referenceFields);
        assertEquals(referenceCsvLine, encoded);
    }

    @Test
    public void testDecoding() {
        List<String> decoded = csvHelper.decodeFields(referenceCsvLine);
        assertEquals(referenceFields, decoded);
    }
}
