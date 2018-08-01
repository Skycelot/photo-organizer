package ru.skycelot.photoorganizer.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SimpleFileContentAnalyzer {

    public byte[] getMagicBytes(Path path, long size) {

        if (size > 0) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            try {
                ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ);
                int readBytesCount = file.read(buffer);
                buffer.flip();
                int magicBytesCount = readBytesCount < 4 ? readBytesCount : 4;
                byte[] result = new byte[magicBytesCount];
                for (int i = 0; i < magicBytesCount; i++) {
                    result[i] = buffer.get();
                }
                buffer.clear();
                return result;

            } catch (IOException e) {
                throw new IllegalArgumentException("Couldn't read a file " + path + ", reason: " + e);
            }
        } else {
            return null;
        }
    }
}
