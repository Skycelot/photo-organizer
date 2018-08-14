package ru.skycelot.photoorganizer.jpeg;

public class SegmentSketch {
    private final int offset;
    private final SegmentType type;
    private int size;

    public SegmentSketch(int offset, SegmentType type) {
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
}
