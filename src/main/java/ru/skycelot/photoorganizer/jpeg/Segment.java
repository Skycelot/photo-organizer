package ru.skycelot.photoorganizer.jpeg;

public class Segment {
    private final int offset;
    private final SegmentType type;
    private int size;
    private byte[] content;

    public Segment(int offset, SegmentType type) {
        this.offset = offset;
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public SegmentType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
