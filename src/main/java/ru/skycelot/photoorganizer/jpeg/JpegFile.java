package ru.skycelot.photoorganizer.jpeg;

import java.util.List;

public class JpegFile {
    public final String path;
    public final List<Segment> segments;

    public JpegFile(String path, List<Segment> segments) {
        this.path = path;
        this.segments = segments;
    }
}
