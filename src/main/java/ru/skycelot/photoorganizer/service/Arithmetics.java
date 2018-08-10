package ru.skycelot.photoorganizer.service;

import java.nio.ByteOrder;

public class Arithmetics {

    public int convertBytesToShort(byte high, byte low) {
        return ((high & 0xFF) << 8) | (low & 0xFF);
    }

    public int convertBytesToShort(byte[] from, int offset, ByteOrder byteOrder) {
        return byteOrder == ByteOrder.BIG_ENDIAN ?
                ((from[offset + 1] & 0xFF) << 8) | (from[offset] & 0xFF) :
                ((from[offset] & 0xFF) << 8) | (from[offset + 1] & 0xFF);
    }

    public long convertBytesToInt(byte first, byte second, byte third, byte forth) {
        return ((first & 0xFFL) << 24) | ((second & 0xFFL) << 16) | ((third & 0xFFL) << 8) | (forth & 0xFFL);
    }

    public long convertBytesToInt(byte[] from, int offset, ByteOrder byteOrder) {
        return byteOrder == ByteOrder.BIG_ENDIAN ?
                ((from[offset + 3] & 0xFFL) << 24) | ((from[offset + 2] & 0xFFL) << 16) | ((from[offset + 1] & 0xFFL) << 8) | (from[offset] & 0xFFL) :
                ((from[offset] & 0xFFL) << 24) | ((from[offset + 1] & 0xFFL) << 16) | ((from[offset + 2] & 0xFFL) << 8) | (from[offset + 3] & 0xFFL);
    }
}
