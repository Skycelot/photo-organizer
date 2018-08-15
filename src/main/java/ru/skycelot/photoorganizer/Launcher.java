package ru.skycelot.photoorganizer;

import ru.skycelot.photoorganizer.conversion.json.DuplicatesJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileEntityJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.FileMetadataJsonConverter;
import ru.skycelot.photoorganizer.conversion.json.JsonHelper;
import ru.skycelot.photoorganizer.domain.Command;
import ru.skycelot.photoorganizer.filesystem.FileMetadataVisitor;
import ru.skycelot.photoorganizer.service.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java -jar photo-organizer.jar {command} {root-dir-path}");
        }
        Command command = Command.valueOf(args[0].toUpperCase());
        Path rootDirectory = Paths.get(args[1]);
        if (!Files.isDirectory(rootDirectory)) {
            throw new IllegalArgumentException("Not a directory " + args[1]);
        }

        JsonHelper jsonHelper = new JsonHelper();
        FileMetadataJsonConverter fileMetadataJsonConverter = new FileMetadataJsonConverter(jsonHelper);
        FileEntityJsonConverter fileEntityJsonConverter = new FileEntityJsonConverter(jsonHelper);
        DuplicatesJsonConverter duplicatesJsonConverter = new DuplicatesJsonConverter(jsonHelper);
        FileContentHelper fileContentHelper = new FileContentHelper(1024 * 1024);
        Arithmetics arithmetics = new Arithmetics();
        JpegSlicer jpegSlicer = new JpegSlicer(arithmetics);
        TiffBlockParser tiffBlockParser = new TiffBlockParser(arithmetics);

        Path fileMetadataDb = Paths.get("file-metadata.json");
        Path fileEntityDb = Paths.get("files.json");
        Path duplicatesDb = Paths.get("duplicates.json");

        switch (command) {
            case WALK:
                FileMetadataExtractor extractor = new FileMetadataExtractor(new FileMetadataVisitor(rootDirectory), fileMetadataJsonConverter);
                extractor.extractFileMetadata(rootDirectory, fileMetadataDb);
                break;
            case ENTITIES:
                FileMetadataToFileEntityConverter converter = new FileMetadataToFileEntityConverter(fileMetadataJsonConverter, fileEntityJsonConverter);
                converter.persistEntities(fileMetadataDb, fileEntityDb);
                break;
            case MAGIC_NUMBERS:
                MagicNumbersProcessor magicNumbersProcessor = new MagicNumbersProcessor(fileEntityJsonConverter, fileContentHelper);
                magicNumbersProcessor.addMagicNumbers(fileEntityDb, rootDirectory);
                break;
            case EXTENSIONS:
                ExtensionExtractor extensionExtractor = new ExtensionExtractor(fileEntityJsonConverter);
                extensionExtractor.extractExtensions(fileEntityDb);
                break;
            case DUPLICATES:
                DuplicatesFinder duplicatesFinder = new DuplicatesFinder(fileEntityJsonConverter, duplicatesJsonConverter, fileContentHelper);
                duplicatesFinder.findDuplicates(fileEntityDb, duplicatesDb, rootDirectory);
                break;
            case FIND_EXIF:
                ExifBlockSaver exifBlockSaver = new ExifBlockSaver(fileEntityJsonConverter, duplicatesJsonConverter, jpegSlicer, tiffBlockParser);
                exifBlockSaver.extractExifInformation(fileEntityDb, duplicatesDb, rootDirectory);
                break;
            case EXTRACT_EXIF:
                break;
        }
    }

}
