package ru.skycelot.photoorganizer.jpeg;

public enum DataFormat {
    U_BYTE(1, 1), STRING(2, 1), U_SHORT(3, 2), U_LONG(4, 4), U_RATIONAL(5, 8), BYTE(6, 1), UNDEFINED(7, 1), SHORT(8, 2), LONG(9, 4), RATIONAL(10, 8), FLOAT(11, 4), DOUBLE(12, 8);

    public final int code;
    public final int size;

    DataFormat(int code, int size) {
        this.code = code;
        this.size = size;
    }

    public static DataFormat getByCode(int code) {
        for (DataFormat dataFormat: values()) {
            if (dataFormat.code == code) {
                return dataFormat;
            }
        }
        throw new IllegalArgumentException("Unkown data format code " + code);
    }
}
