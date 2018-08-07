package ru.skycelot.photoorganizer.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileContentHelper {

    private final int bufferLength;
    private final ByteBuffer buffer;

    public FileContentHelper(int bufferLength) {
        this.bufferLength = bufferLength;
        this.buffer = ByteBuffer.allocate(bufferLength);
    }

    public byte[] readMagicBytes(Path path, int count) {
        try (ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ)) {
            buffer.clear();
            buffer.limit(count);
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
        try (ByteChannel file = Files.newByteChannel(path, StandardOpenOption.READ)) {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            buffer.clear();
            buffer.limit(bufferLength);
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
}
