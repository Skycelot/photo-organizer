package ru.skycelot.photoorganizer.filesystem;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Folder {
    public Path path;
    public List<File> files = new LinkedList<>();

    @Override
    public String toString() {
        return "Folder{" +
                "path=" + path +
                ", files=" + files +
                '}';
    }
}
