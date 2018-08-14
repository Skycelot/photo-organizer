package ru.skycelot.photoorganizer.service;

import ru.skycelot.photoorganizer.jpeg.DataFormat;
import ru.skycelot.photoorganizer.jpeg.TagType;
import ru.skycelot.photoorganizer.jpeg.TiffTag;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TiffBlockParser {
    private final Arithmetics arithmetics;

    public TiffBlockParser(Arithmetics arithmetics) {
        this.arithmetics = arithmetics;
    }

    public boolean validTiffBlock(byte[] content) {
        ByteOrder byteOrder;
        int byteOrderSignature = arithmetics.convertBytesToShort(content[0], content[1]);
        if (byteOrderSignature == 0x4949) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrderSignature == 0x4D4D) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            return false;
        }
        int tiffSignature = arithmetics.convertBytesToShort(content, 2, byteOrder);
        return tiffSignature == 0x002A;
    }

    public List<TiffTag> extractEntries(byte[] tiffBlock) {
        ByteOrder byteOrder;
        int byteOrderSignature = arithmetics.convertBytesToShort(tiffBlock[0], tiffBlock[1]);
        if (byteOrderSignature == 0x4949) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrderSignature == 0x4D4D) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            throw new IllegalArgumentException("Unknown byte order signature " + byteOrderSignature);
        }
        int tiffSignature = arithmetics.convertBytesToShort(tiffBlock, 2, byteOrder);
        if (tiffSignature != 0x002A) {
            throw new IllegalArgumentException("Unknown tiff signature " + tiffSignature);
        }

        int firstDirOffset = (int) arithmetics.convertBytesToInt(tiffBlock, 4, byteOrder);
        List<TiffTag> result = new LinkedList<>();
        result.addAll(processDirectory(tiffBlock, firstDirOffset, byteOrder));

        int exifOffset = -1;
        for (TiffTag entry : result) {
            if (entry.tagType == TagType.ExifOffset) {
                exifOffset = (int) arithmetics.convertBytesToInt(entry.value, 0, byteOrder);
                break;
            }
        }
        if (exifOffset > -1) {
            result.addAll(processDirectory(tiffBlock, exifOffset, byteOrder));
        }

        return result;
    }

    private List<TiffTag> processDirectory(byte[] content, int dirOffset, ByteOrder byteOrder) {
        List<TiffTag> result = new LinkedList<>();

        int dirEntriesNumber = arithmetics.convertBytesToShort(content, dirOffset, byteOrder);
        for (int i = 0; i < dirEntriesNumber; i++) {
            int offset = dirOffset + 2 + i * 12;
            int tagCode = arithmetics.convertBytesToShort(content, offset, byteOrder);
            TagType tagType = TagType.findByCode(tagCode);
            if (tagType != null) {
                TiffTag tiffTag = new TiffTag();
                tiffTag.tagType = tagType;
                int dataFormatCode = arithmetics.convertBytesToShort(content, offset + 2, byteOrder);
                tiffTag.dataFormat = DataFormat.getByCode(dataFormatCode);
                long numberOfComponents = arithmetics.convertBytesToInt(content, offset + 4, byteOrder);
                int valueSize = ((int) numberOfComponents) * tiffTag.dataFormat.size;
                if (valueSize <= 4) {
                    tiffTag.value = Arrays.copyOfRange(content, offset + 8, offset + 12);
                } else {
                    int valueOffset = (int) arithmetics.convertBytesToInt(content, offset + 8, byteOrder);
                    tiffTag.value = Arrays.copyOfRange(content, valueOffset, valueOffset + valueSize);
                }
                result.add(tiffTag);
            }
        }
        return result;
    }
}
