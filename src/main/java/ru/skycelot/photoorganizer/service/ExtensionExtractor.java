package ru.skycelot.photoorganizer.service;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtensionExtractor {
    private final Pattern extensionPattern = Pattern.compile("^[^_]{1,5}$");

    public String[] splitNameAndExtension(String fileName) {
        String[] fileNameParts = fileName.split("\\.");
        boolean extensionPresent = fileNameParts.length > 1 && extensionPattern.matcher(fileNameParts[fileNameParts.length - 1]).matches();
        String name, extension;
        if (extensionPresent) {
            name = Arrays.asList(fileNameParts).stream().limit(fileNameParts.length - 1).collect(Collectors.joining("."));
            extension = fileNameParts[fileNameParts.length - 1];
        } else {
            name = fileName;
            extension = "";
        }
        return new String[]{name, extension};
    }
}
