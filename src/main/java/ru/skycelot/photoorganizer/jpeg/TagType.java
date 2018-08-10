package ru.skycelot.photoorganizer.jpeg;

public enum TagType {
    Make(0x010f), Model(0x0110), DateTime(0x0132), ExifOffset(0x8769), DateTimeOriginal(0x9003);

    public final int code;

    TagType(int code) {
        this.code = code;
    }

    public static TagType findByCode(int code) {
        for (TagType tagType : values()) {
            if (tagType.code == code) {
                return tagType;
            }
        }
        return null;
    }
}
