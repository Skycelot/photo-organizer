package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.jpeg.Segment;
import ru.skycelot.photoorganizer.jpeg.SegmentType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class JpegSlicer {
    private final Arithmetics arithmetics;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

    public JpegSlicer(Arithmetics arithmetics) {
        this.arithmetics = arithmetics;
    }

    public List<Segment> slice(Path file) {
        List<Segment> result = new LinkedList<>();
        try (ByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            State currentState = State.NEXT;
            byte firstByte = 0;
            Segment currentSegment = null;
            int contentLength = 0;
            buffer.clear();
            int offset = 0;
            while (channel.read(buffer) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte currentByte = buffer.get();
                    switch (currentState) {
                        case NEXT:
                            if (currentByte == ((byte) 0xFF)) {
                                firstByte = currentByte;
                                currentState = State.MARK;
                            }
                            break;
                        case MARK:
                            int mark = arithmetics.convertBytesToShort(firstByte, currentByte);
                            SegmentType type = SegmentType.findByMark(mark);
                            if (type != null) {
                                currentSegment = new Segment(offset - 1, type);
                                result.add(currentSegment);
                                currentState = type.hasLength ? State.LENGTH_1 : State.NEXT;
                            } else {
                                currentState = State.NEXT;
                            }
                            break;
                        case LENGTH_1:
                            firstByte = currentByte;
                            currentState = State.LENGTH_2;
                            break;
                        case LENGTH_2:
                            contentLength = arithmetics.convertBytesToShort(firstByte, currentByte) - 2;
                            currentSegment.setSize(contentLength);
                            if (contentLength > 0) {
                                currentSegment.setContent(new byte[contentLength]);
                                currentState = State.CONTENT;
                            } else {
                                currentState = State.NEXT;
                            }
                            break;
                        case CONTENT:
                            currentSegment.getContent()[currentSegment.getContent().length - contentLength] = currentByte;
                            contentLength--;
                            if (contentLength == 0) {
                                currentState = State.NEXT;
                            }
                            break;
                    }
                    offset++;
                }
                buffer.clear();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private enum State {
        NEXT, MARK, LENGTH_1, LENGTH_2, CONTENT
    }
}
