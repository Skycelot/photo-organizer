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

    public ContentAnalysis analyze(Path path, long size) {
        ContentAnalysis result = new ContentAnalysis();

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        try {
            ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ);
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            int readBytes;
            boolean firstBuffer = true;
            while ((readBytes = file.read(buffer)) > 0) {
                buffer.flip();
                byte[] chunk = new byte[readBytes];
                buffer.get(chunk);
                if (firstBuffer && readBytes >= 4) {
                    firstBuffer = false;
                    result.magicBytes = Arrays.copyOfRange(chunk, 0, 4);
                }
                hash.update(chunk);
                buffer.clear();
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
