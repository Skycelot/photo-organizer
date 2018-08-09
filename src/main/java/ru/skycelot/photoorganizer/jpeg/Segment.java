package ru.skycelot.photoorganizer.jpeg;

public class Segment {
    private final SegmentType type;
    private byte[] content;

    public Segment(short id) {
        this.type = SegmentType.findById(id);
    }

    public SegmentType getType() {
        return type;
    }

    public boolean hasLength() {
        return type.hasLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public enum SegmentType {
        soi((short) 0xFFD8, false),
        sof0((short) 0xFFC0, true),
        sof2((short) 0xFFC2, true),
        dht((short) 0xFFC4, true),
        dqt((short) 0xFFDB, true),
        dri((short) 0xFFDD, true),
        sos((short) 0xFFDA, true),
        rst0((short) 0xFFD0, false), rst1((short) 0xFFD1, false), rst2((short) 0xFFD2, false), rst3((short) 0xFFD3, false), rst4((short) 0xFFD4, false), rst5((short) 0xFFD5, false), rst6((short) 0xFFD6, false), rst7((short) 0xFFD7, false),
        app0((short) 0xFFE0, true), app1((short) 0xFFE1, true), app2((short) 0xFFE2, true), app3((short) 0xFFE3, true), app4((short) 0xFFE4, true), app5((short) 0xFFE5, true), app6((short) 0xFFE6, true), app7((short) 0xFFE7, true),
        app8((short) 0xFFE8, true), app9((short) 0xFFE9, true), appA((short) 0xFFEA, true), appB((short) 0xFFEB, true), appC((short) 0xFFEC, true), appD((short) 0xFFED, true), appE((short) 0xFFEE, true), appF((short) 0xFFEF, true),
        com((short) 0xFFFE, true),
        eoi((short) 0xFFD9, false),
        unknown((short) 0, true);

        public final short id;
        public final boolean hasLength;

        SegmentType(short id, boolean hasLength) {
            this.id = id;
            this.hasLength = hasLength;
        }

        public static SegmentType findById(short id) {
            for (SegmentType type: values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return unknown;
        }
    }
}
