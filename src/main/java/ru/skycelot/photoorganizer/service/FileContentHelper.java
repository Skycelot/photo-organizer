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

public class FileContentHelper {

    public ContentAnalysis analyze(Path path, long size) {
        ContentAnalysis result = new ContentAnalysis();
        if (size > 0) {
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
                    if (firstBuffer) {
                        firstBuffer = false;
                        result.magicBytes = Arrays.copyOfRange(chunk, 0, readBytes < 4 ? readBytes : 4);
                    }
                    hash.update(chunk);
                    buffer.clear();
                }
                result.hash = hash.digest();
            } catch (IOException e) {
                throw new IllegalArgumentException("Couldn't read a file " + path + ", reason: " + e);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Couldn't find sha-256 hash provider");
            }
        }
        return result;
    }

    public byte[] readMagicBytes(Path path) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        try (ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ)) {
            int readBytes = file.read(buffer);
            byte[] magicBytes = new byte[readBytes];
            buffer.flip();
            buffer.get(magicBytes);
            return magicBytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read a file " + path + ", reason: " + e);
        }
    }

    public byte[] calculateHash(Path path) {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        try (ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ)) {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            int readBytes;
            while((readBytes = file.read(buffer)) > 0) {
                buffer.flip();
                for(int i = 0; i < readBytes; i++) {
                    hash.update(buffer.get());
                }
                buffer.clear();
            }
            return hash.digest();
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