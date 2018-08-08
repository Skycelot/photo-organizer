package ru.skycelot.photoorganizer.domain;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Extension {

    JPG("ffd8ff"),
    PNG("89504E47"),
    BMP("424D"),
    TIF("49492A00", "4D4D002A"),
    GIF("474946383761", "474946383961"),
    NOT_AN_IMAGE;

    private final List<byte[]> magicNumbers;

    Extension(String... magicNumbers) {
        this.magicNumbers = Arrays.stream(magicNumbers).map(magicNumber -> DatatypeConverter.parseHexBinary(magicNumber)).collect(Collectors.toList());
    }

    public static Extension findByMagicNumber(byte[] magicNumber) {
        if (magicNumber != null) {
            for (Extension extension : values()) {
                if (startsWith(magicNumber, extension.magicNumbers)) {
                    return extension;
                }
            }
        }
        return NOT_AN_IMAGE;
    }

    private static boolean startsWith(byte[] actual, List<byte[]> magicNumbers) {
        boolean found = false;
        for (byte[] magicNumber : magicNumbers) {
            if (magicNumber.length > actual.length) continue;
            boolean equals = true;
            for (int i = 0; i < magicNumber.length; i++) {
                if (magicNumber[i] != actual[i]) {
                    equals = false;
                    break;
                }
            }
            if (equals) {
                found = true;
                break;
            }
        }
        return found;
    }
}
