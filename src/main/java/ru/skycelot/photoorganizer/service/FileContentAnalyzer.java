package ru.skycelot.photoorganizer.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileContentAnalyzer {

    public ContentAnalysis analyze(Path path) {
        ContentAnalysis result = new ContentAnalysis();

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        try {
            ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ);
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            boolean firstBuffer = true;
            while(file.read(buffer) > -1) {
                buffer.rewind();
                if (firstBuffer) {
                    firstBuffer = false;
                    result.magicBytes = Arrays.copyOfRange(buffer.array(), 0, 4);
                }
                hash.update(buffer.array());
                buffer.flip();
            }
            result.hash = hash.digest();
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read a file " + path + ", reason: " + e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Couldn't find sha-256 hash provider");
        }
    }

    public static class ContentAnalysis {
        public byte[] hash;
        public byte[] magicBytes;
    }
}
