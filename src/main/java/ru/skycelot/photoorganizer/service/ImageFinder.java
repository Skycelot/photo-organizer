package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.domain.FileEntity;

public class ImageFinder {

    public static final byte[] JPEG_MAGIC_NUMBER = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};

    public boolean isImage(FileEntity file) {
        return startsWith(JPEG_MAGIC_NUMBER, file.magicBytes);
    }

    private boolean startsWith(byte[] expected, byte[] actual) {
        if (actual.length < expected.length) return false;
        boolean equals = true;
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                equals = false;
                break;
            }
        }
        return equals;
    }
}
