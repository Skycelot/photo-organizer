package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.jpeg.SegmentSketch;
import ru.skycelot.photoorganizer.jpeg.SegmentType;

import javax.xml.bind.DatatypeConverter;
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

    public List<SegmentSketch> slice(Path file) {
        List<SegmentSketch> result = new LinkedList<>();
        try (ByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            State currentState = State.NEXT;
            byte firstByte = 0;
            SegmentSketch currentSegment = null;
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
                            } else {
                                throw new IllegalArgumentException(currentState + ": " + DatatypeConverter.printHexBinary(new byte[]{currentByte}));
                            }
                            break;
                        case MARK:
                            int mark = arithmetics.convertBytesToShort(firstByte, currentByte);
                            SegmentType type = SegmentType.findByMark(mark);
                            currentSegment = new SegmentSketch(offset - 1, type);
                            result.add(currentSegment);
                            currentState = type.hasLength ? State.LENGTH_1 : State.NEXT;
                            break;
                        case LENGTH_1:
                            firstByte = currentByte;
                            currentState = State.LENGTH_2;
                            break;
                        case LENGTH_2:
                            contentLength = arithmetics.convertBytesToShort(firstByte, currentByte) - 2;
                            currentSegment.setSize(contentLength);
                            currentState = contentLength == 0 ? State.NEXT: State.CONTENT;
                            break;
                        case CONTENT:
                            contentLength--;
                            if (contentLength == 0) {
                                currentState = currentSegment.getType() == SegmentType.sos ? State.IMAGE : State.NEXT;
                            }
                            break;
                        case IMAGE:
                            if (currentByte == ((byte) 0xFF)) {
                                firstByte = currentByte;
                                currentState = State.ESCAPE;
                            }
                            break;
                        case ESCAPE:
                            if (currentByte == ((byte) 0xFF)) {
                                currentState = State.IMAGE;
                            } else {
                                mark = arithmetics.convertBytesToShort(firstByte, currentByte);
                                type = SegmentType.findByMark(mark);
                                currentSegment = new SegmentSketch(offset - 1, type);
                                result.add(currentSegment);
                                currentState = type.hasLength ? State.LENGTH_1 : State.IMAGE;
                            }
                            break;
                    }
                }
                buffer.clear();
                offset++;
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private enum State {
        NEXT, MARK, LENGTH_1, LENGTH_2, CONTENT, IMAGE, ESCAPE
    }
}
