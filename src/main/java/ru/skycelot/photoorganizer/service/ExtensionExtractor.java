package ru.skycelot.photoorganizer.service;

import java.util.regex.Pattern;

public class ExtensionExtractor {
    private final Pattern extensionPattern = Pattern.compile("^[^_]{1,5}$");

    public String extractExtension(String fileName) {
        String[] fileNameParts = fileName.split("\\.");
        boolean extensionPresent = fileNameParts.length > 1 && extensionPattern.matcher(fileNameParts[fileNameParts.length - 1]).matches();
        return extensionPresent ? fileNameParts[fileNameParts.length - 1] : null;
    }
}
