package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.jpeg.Segment;
import ru.skycelot.photoorganizer.jpeg.SegmentType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class SegmentExtractor {

    private final Arithmetics arithmetics;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);

    public SegmentExtractor(Arithmetics arithmetics) {
        this.arithmetics = arithmetics;
    }

    public Segment findSegment(SegmentType type, Path file) {
        try {
            Segment result = null;
            ByteChannel channel = Files.newByteChannel(file);
            State currentState = State.SEARCH;
            byte firstBlockLengthByte = 0;
            byte[] content = null;
            int offset = 0;
            while (currentState != State.COMPLETE) {
                buffer.clear();
                channel.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte currentByte = buffer.get();
                    switch (currentState) {
                        case SEARCH:
                            if (currentByte == type.idBytes[0]) {
                                currentState = State.BLOCK_START;
                            }
                            break;
                        case BLOCK_START:
                            if (currentByte == type.idBytes[1]) {
                                if (type.hasLength) {
                                    currentState = State.TYPE_MATCH;
                                } else {
                                    result = new Segment(type, null);
                                    currentState = State.COMPLETE;
                                }
                            } else {
                                currentState = State.SEARCH;
                            }
                            break;
                        case TYPE_MATCH:
                            firstBlockLengthByte = currentByte;
                            currentState = State.FIRST_LENGTH_BYTE;
                            break;
                        case FIRST_LENGTH_BYTE:
                            int length = arithmetics.convertBytesToShort(firstBlockLengthByte, currentByte);
                            if (length > 2) {
                                content = new byte[length - 2];
                                currentState = State.CONTENT;
                            } else {
                                result = new Segment(type, null);
                                currentState = State.COMPLETE;
                            }
                            break;
                        case CONTENT:
                            content[offset++] = currentByte;
                            if (offset >= content.length) {
                                result = new Segment(type, content);
                                currentState = State.COMPLETE;
                            }
                            break;
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private enum State {
        SEARCH, BLOCK_START, TYPE_MATCH, FIRST_LENGTH_BYTE, CONTENT, COMPLETE
    }
}
