package ru.skycelot.photoorganizer.jpeg;

public class Segment {
    private final SegmentType type;
    private final byte[] content;

    public Segment(SegmentType type, byte[] content) {
        this.type = type;
        this.content = content;
    }

    public SegmentType getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }
}
