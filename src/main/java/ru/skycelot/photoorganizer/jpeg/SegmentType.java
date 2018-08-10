package ru.skycelot.photoorganizer.jpeg;

public enum SegmentType {
    soi(0xFFD8, false),
    sof0(0xFFC0, true),
    sof2(0xFFC2, true),
    dht(0xFFC4, true),
    dqt(0xFFDB, true),
    dri(0xFFDD, true),
    sos(0xFFDA, true),
    rst0(0xFFD0, false), rst1(0xFFD1, false), rst2(0xFFD2, false), rst3(0xFFD3, false), rst4(0xFFD4, false), rst5(0xFFD5, false), rst6(0xFFD6, false), rst7(0xFFD7, false),
    app0(0xFFE0, true), app1(0xFFE1, true), app2(0xFFE2, true), app3(0xFFE3, true), app4(0xFFE4, true), app5(0xFFE5, true), app6(0xFFE6, true), app7(0xFFE7, true),
    app8(0xFFE8, true), app9(0xFFE9, true), appA(0xFFEA, true), appB(0xFFEB, true), appC(0xFFEC, true), appD(0xFFED, true), appE(0xFFEE, true), appF(0xFFEF, true),
    com(0xFFFE, true),
    eoi(0xFFD9, false);

    public final int id;
    public final byte[] idBytes;
    public final boolean hasLength;

    SegmentType(int id, boolean hasLength) {
        this.id = id;
        this.idBytes = new byte[] {(byte) ((id & 0xFF00) >>> 8), (byte) (id & 0xFF)};
        this.hasLength = hasLength;
    }
}
