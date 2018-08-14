package ru.skycelot.photoorganizer;

import org.junit.jupiter.api.Test;
import ru.skycelot.photoorganizer.service.Arithmetics;
import ru.skycelot.photoorganizer.service.JpegSlicer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFeatures {

    @Test
    public void test() throws URISyntaxException {
        Path jpegFile = Paths.get(ClassLoader.getSystemResource("photo.jpg").toURI());

        JpegSlicer slicer = new JpegSlicer(new Arithmetics());

        int i = 0;
    }
}
