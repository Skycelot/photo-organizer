package ru.skycelot.photoorganizer.domain;

import java.util.List;
import java.util.UUID;

public class Duplicates {
    public List<UUID> filesIds;

    public Duplicates(List<UUID> filesIds) {
        this.filesIds = filesIds;
    }
}
